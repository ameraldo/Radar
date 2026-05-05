package com.ameraldo.radar.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ameraldo.radar.R
import com.ameraldo.radar.data.AppDatabase
import com.ameraldo.radar.data.LocationError
import com.ameraldo.radar.data.LocationState
import com.ameraldo.radar.data.RecordedPointEntity
import com.ameraldo.radar.data.SatelliteBlip
import com.ameraldo.radar.data.ServiceState
import com.ameraldo.radar.utils.calculatePointsDistance
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import android.location.GnssStatus
import android.os.Binder
import android.util.Log
import com.ameraldo.radar.MainActivity
import com.ameraldo.radar.data.RouteDao
import com.ameraldo.radar.data.RouteEntity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Foreground service for GPS tracking, route recording, and route following.
 *
 * This service is the **single source of truth** for:
 * - Current GPS location state (coordinates, accuracy, satellites)
 * - Recording state and route data
 * - Following state and remaining points
 *
 * The service runs as a foreground service to ensure reliable GPS tracking
 * even when the app is in the background or screen is locked.
 *
 * State persistence is handled via [ServiceState] (DataStore) to survive
 * service restarts. The service restores its state in [onStartCommand].
 *
 * ## Usage
 * ```
 * // Start recording
 * val intent = LocationService.createStartRecordingIntent(context, "My Route")
 * context.startForegroundService(intent)
 *
 * // Start following
 * val intent = LocationService.createStartFollowingIntent(context, routeId)
 * context.startForegroundService(intent)
 * ```
 *
 * ## State Properties
 * - [locationState]: Current GPS location, accuracy, satellites
 * - [isRecording]: Whether a route is being recorded
 * - [isFollowing]: Whether following a saved route
 * - [currentRouteId]: ID of current route (recording or following)
 * - [currentRouteName]: Name of current route
 * - [currentRoutePoints]: Points for current route (Room-backed Flow)
 * - [followingRemainingPoints]: Points yet to be reached while following (in-memory)
 */
class LocationService : Service() {
    private lateinit var database: AppDatabase
    private lateinit var routeDao: RouteDao
    private lateinit var serviceState: ServiceState
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    /**
     * Coroutine scope for service-level async operations.
     * Uses [SupervisorJob] for fault tolerance (child failure doesn't cancel siblings)
     * and [Dispatchers.Default] for database/computation work.
     */
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    /** Flag to track whether the service is bound to an activity */
    private var serviceBound = false

    // Core mutable state flows (initialized immediately; no lateinit needed)
    private val _locationState = MutableStateFlow(LocationState())
    private val _isRecording = MutableStateFlow(false)
    private val _isFollowing = MutableStateFlow(false)
    private val _currentRouteId = MutableStateFlow<Long?>(null)
    private val _currentRouteName = MutableStateFlow<String?>(null)
    private val _followingRemainingPoints = MutableStateFlow<List<RecordedPointEntity>>(emptyList())

    // Public read-only StateFlows exposed to ViewModel/binders
    val locationState: StateFlow<LocationState> = _locationState
    val isRecording: StateFlow<Boolean> = _isRecording
    val isFollowing: StateFlow<Boolean> = _isFollowing
    val currentRouteId: StateFlow<Long?> = _currentRouteId
    val currentRouteName: StateFlow<String?> = _currentRouteName

    /**
     * Room-backed flow that emits updated point list whenever the database changes.
     * Uses [flatMapLatest] to react to [currentRouteId] changes:
     * - When route ID changes, switches to observe the new route's points
     * - Returns empty list if no route is active
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentRoutePoints: StateFlow<List<RecordedPointEntity>> = currentRouteId
        .flatMapLatest { routeId ->
            if (routeId != null) routeDao.getPointsForRoute(routeId)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    /**
     * In-memory flow for points remaining to be reached during following.
     * Unlike [currentRoutePoints], this is NOT Room-backed because:
     * - Points are consumed (removed) as they are reached
     * - Order is reversed (start point first) for efficient [List.drop] operations
     */
    val followingRemainingPoints: StateFlow<List<RecordedPointEntity>> = _followingRemainingPoints.asStateFlow()
    /**
     * Local tracking variables for synchronous distance checks in location callback.
     * These are NOT duplicating Room data - they provide fast access for:
     * - [lastRecordedLat], [lastRecordedLon]: Last recorded coordinates for distance calculation
     * - [sequenceCounter]: Next sequence number for new points
     */
    private var lastRecordedLat: Double? = null
    private var lastRecordedLon: Double? = null
    private var sequenceCounter = 0

    /** Callback for receiving location updates from FusedLocationProviderClient */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { handleLocationUpdate(it) }
        }
    }
    /** Callback for receiving GNSS satellite status updates from LocationManager */
    private val gnssCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val blips = (0 until status.satelliteCount).map { i ->
                val elevation = status.getElevationDegrees(i).coerceIn(0f, 90f)
                val carrierFreq = if (status.hasCarrierFrequencyHz(i))
                    status.getCarrierFrequencyHz(i) / 1_000_000f else null

                SatelliteBlip(
                    angleDeg       = status.getAzimuthDegrees(i),
                    // 0° elevation = horizon = outer ring (1.0)
                    // 90° elevation = overhead = center (0.0)
                    radiusFraction = 1f - (elevation / 90f),
                    isLocked       = status.usedInFix(i),
                    signalStrength = status.getCn0DbHz(i),
                    svid           = status.getSvid(i),
                    constellation  = when (status.getConstellationType(i)) {
                        GnssStatus.CONSTELLATION_GPS     -> "GPS"
                        GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
                        GnssStatus.CONSTELLATION_GALILEO -> "Galileo"
                        GnssStatus.CONSTELLATION_BEIDOU  -> "BeiDou"
                        GnssStatus.CONSTELLATION_QZSS    -> "QZSS"
                        GnssStatus.CONSTELLATION_SBAS    -> "SBAS"
                        else                             -> "Unknown"
                    },
                    elevationDeg   = elevation,
                    hasAlmanac     = status.hasAlmanacData(i),
                    hasEphemeris   = status.hasEphemerisData(i),
                    carrierFrequencyMhz = carrierFreq
                )
            }
            coroutineScope.launch {
                _locationState.value = _locationState.value.copy(satellites = blips)
            }
        }
    }

    // Notification constants
    private val CHANNEL_ID = "location_tracking_channel"
    private val NOTIFICATION_ID = 1

    /**
     * Initializes service dependencies: database, DAOs, service state, and location clients.
     * Also creates the notification channel required for foreground service.
     */
    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(applicationContext)
        routeDao = database.routeDao()
        serviceState = ServiceState.getInstance(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
        locationManager = application.getSystemService(LOCATION_SERVICE) as LocationManager

        createNotificationChannel()
    }
    /**
     * Handles incoming intent actions.
     * Restores state from [ServiceState] and processes the intent action.
     *
     * @return [START_STICKY] to restart service if killed by system
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        coroutineScope.launch {
            restoreStateIfNeeded()
        }

        when (intent?.action) {
            ACTION_START_LOCATION_UPDATES -> startLocationUpdates()
            ACTION_STOP_LOCATION_UPDATES -> stopLocationUpdates()
            ACTION_START_RECORDING -> startRecording(intent)
            ACTION_STOP_RECORDING -> stopRecording()
            ACTION_START_FOLLOWING -> startFollowing(intent)
            ACTION_STOP_FOLLOWING -> stopFollowing()
        }
        return START_STICKY
    }
    /**
     * Called when an activity binds to this service.
     * Sets [serviceBound] flag to true and returns the binder.
     */
    override fun onBind(intent: Intent?): IBinder {
        serviceBound = true
        return binder
    }
    /**
     * Called when a client re-binds to the service after all clients unbound.
     * Sets [serviceBound] flag to true.
     */
    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        serviceBound = true
    }
    /**
     * Called when all clients unbind from the service.
     * Sets [serviceBound] to false and checks if service should stop.
     *
     * @return true to allow rebinding without re-creating the service
     */
    override fun onUnbind(intent: Intent?): Boolean {
        serviceBound = false
        checkAndStopIfIdle()
        return true  // Return true to allow to rebind
    }
    /**
     * Cleans up resources when service is destroyed.
     * Stops location updates and cancels the coroutine scope.
     */
    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        coroutineScope.cancel()
    }

    // Binder
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    /**
     * Stops recording via binder call from ViewModel.
     * Called when activity is bound to service (vs notification intent).
     *
     * @param stopService If true, stops the foreground service after stopping recording
     */
    fun stopRecordingViaBinder() {
        stopRecording(true)  // Stop service when called via binder
    }
    /**
     * Stops following via binder call from ViewModel.
     * Called when activity is bound to service (vs notification intent).
     *
     * @param stopService If true, stops the foreground service after stopping following
     */
    fun stopFollowingViaBinder() {
        stopFollowing(true)  // Stop service when called via binder
    }
    /**
     * Sets a permission error state (called from ViewModel).
     * Used when location permission is denied to update the UI state.
     *
     * @param error The permission error to set
     */
    fun setPermissionError(error: LocationError) {
        _locationState.value = _locationState.value.copy(error = error, isLoading = false)
    }
    /**
     * Deletes the current route and clears all related state.
     * Called when user chooses to discard (not save) a recorded route.
     * Clears recording/following state in [ServiceState] as well.
     */
    fun deleteCurrentRoute() {
        coroutineScope.launch {
            val routeId = _currentRouteId.value ?: return@launch
            routeDao.deleteRoute(routeId)
            _currentRouteId.value = null
            _currentRouteName.value = null
            _isRecording.value = false
            _isFollowing.value = false
            serviceState.clearRecordingState()
            serviceState.clearFollowingState()
        }
    }

    companion object {
        /** Intent action to start location updates */
        const val ACTION_START_LOCATION_UPDATES = "action_start_location_updates"
        /** Intent action to stop location updates */
        const val ACTION_STOP_LOCATION_UPDATES = "action_stop_location_updates"
        /** Intent action to start recording a new route */
        const val ACTION_START_RECORDING = "action_start_recording"
        /** Intent action to stop recording the current route */
        const val ACTION_STOP_RECORDING = "action_stop_recording"
        /** Intent action to start following a saved route */
        const val ACTION_START_FOLLOWING = "action_start_following"
        /** Intent action to stop following the current route */
        const val ACTION_STOP_FOLLOWING = "action_stop_following"
        /** Intent extra key for route name (String) */
        const val EXTRA_ROUTE_NAME = "extra_route_name"
        /** Intent extra key for route ID (Long) */
        const val EXTRA_ROUTE_ID = "extra_route_id"

        /**
         * Creates an intent to request to start location updates.
         *
         * @param context Application context
         * @return Intent with ACTION_START_LOCATION_UPDATES
         */
        fun requestStartLocationUpdates(context: Context): Intent {
            return Intent(context, LocationService::class.java).apply {
                action = ACTION_START_LOCATION_UPDATES
            }
        }
        /**
         * Creates an intent to request to stop location updates.
         *
         * @param context Application context
         * @return Intent with ACTION_STOP_LOCATION_UPDATES
         */
        fun requestStopLocationUpdates(context: Context): Intent {
            return Intent(context, LocationService::class.java).apply {
                action = ACTION_STOP_LOCATION_UPDATES
            }
        }
        /**
         * Creates an intent to start recording a new route.
         *
         * @param context Application context
         * @param routeName Name for the new route
         * @return Intent with ACTION_START_RECORDING and EXTRA_ROUTE_NAME
         */
        fun createStartRecordingIntent(context: Context, routeName: String): Intent {
            return Intent(context, LocationService::class.java).apply {
                action = ACTION_START_RECORDING
                putExtra(EXTRA_ROUTE_NAME, routeName)
            }
        }
        /**
         * Creates an intent to stop the current recording.
         *
         * @param context Application context
         * @return Intent with ACTION_STOP_RECORDING
         */
        fun createStopRecordingIntent(context: Context): Intent {
            return Intent(context, LocationService::class.java).apply {
                action = ACTION_STOP_RECORDING
            }
        }
        /**
         * Creates an intent to start following a saved route.
         *
         * @param context Application context
         * @param routeId ID of the route to follow
         * @return Intent with ACTION_START_FOLLOWING and EXTRA_ROUTE_ID
         */
        fun createStartFollowingIntent(context: Context, routeId: Long): Intent {
            return Intent(context, LocationService::class.java).apply {
                action = ACTION_START_FOLLOWING
                putExtra(EXTRA_ROUTE_ID, routeId)
            }
        }
        /**
         * Creates an intent to stop following the current route.
         *
         * @param context Application context
         * @return Intent with ACTION_STOP_FOLLOWING
         */
        fun createStopFollowingIntent(context: Context): Intent {
            return Intent(context, LocationService::class.java).apply {
                action = ACTION_STOP_FOLLOWING
            }
        }
    }

    /**
     * Restores service state from [ServiceState] (DataStore) after service restart.
     * Called in [onStartCommand] to handle service recreation by the system.
     *
     * Restores: recording state (with route ID, name, point tracking)
     * and following state (with remaining points loaded in reverse order).
     * If neither recording nor following is active and no clients are bound,
     * stops the service to avoid orphaned foreground services.
     */
    private suspend fun restoreStateIfNeeded() {
        val isRecording = serviceState.isRecording.first()
        val recordingRouteId = serviceState.recordingRouteId.first()
        val isFollowing = serviceState.isFollowing.first()
        val followingRouteId = serviceState.followingRouteId.first()

        if (isRecording && recordingRouteId != null) {
            _isRecording.value = true
            _currentRouteId.value = recordingRouteId
            val route = routeDao.getRouteById(recordingRouteId)
            _currentRouteName.value = route?.name
            startForeground(NOTIFICATION_ID, buildNotification("Recording route"))

            // Restore sync tracking from existing points
            coroutineScope.launch {
                val existingPoints = routeDao.getPointsForRoute(recordingRouteId).first()
                if (existingPoints.isNotEmpty()) {
                    val lastPoint = existingPoints.last()
                    lastRecordedLat = lastPoint.latitude
                    lastRecordedLon = lastPoint.longitude
                    sequenceCounter = existingPoints.size
                }
            }
        }

        if (isFollowing && followingRouteId != null) {
            _isFollowing.value = true
            _currentRouteId.value = followingRouteId
            loadRouteForFollowing(followingRouteId)
            startForeground(NOTIFICATION_ID, buildNotification("Following route"))
        }

        // If neither recording nor following, and no bound clients, stop
        if (!isRecording && !isFollowing && !serviceBound) {
            stopSelf()
        }
    }

    /**
     * Starts route recording.
     * Stops any active following first (mutually exclusive with recording).
     *
     * 1. Sets recording state to true
     * 2. Creates notification and starts foreground service
     * 3. Generates route name and saves route to database
     * 4. Starts location updates
     *
     * @param intent Contains [EXTRA_ROUTE_NAME] for the route name
     */
    private fun startRecording(intent: Intent) {
        if (_isFollowing.value) stopFollowing(false) // Stop following if active
        val routeName = intent.getStringExtra(EXTRA_ROUTE_NAME) ?: ""
        _isRecording.value = true
        try {
            startForeground(NOTIFICATION_ID, buildNotification("Recording route"))
        } catch (e: Exception) {
            Log.e("LocationService", "Failed to start foreground notification", e)
            stopSelf()
            return
        }
        generateRouteNameAndSave(routeName)
        startLocationUpdates()
    }
    /**
     * Stops route recording.
     *
     * Resets tracking variables, saves route metadata (end time, point count),
     * and clears recording state in [ServiceState].
     *
     * @param stopService If true, stops foreground service and location updates
     *                    (used when called from notification or ViewModel).
     *                    If false, keeps service running (used when switching to following).
     */
    private fun stopRecording(stopService: Boolean = true) {
        _isRecording.value = false
        // Reset tracking
        lastRecordedLat = null
        lastRecordedLon = null
        sequenceCounter = 0
        // Save route
        saveCurrentRoute()
        coroutineScope.launch {
            serviceState.clearRecordingState()
        }
        // Stop service
        if (stopService) {
            stopLocationUpdates()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }
    /**
     * Starts following a saved route.
     * Stops any active recording first (mutually exclusive with following).
     *
     * 1. Sets following state to true
     * 2. Creates notification and starts foreground service
     * 3. Loads route points (reversed for efficient consumption)
     * 4. Starts location updates
     * 5. Saves following state to [ServiceState]
     *
     * @param intent Contains [EXTRA_ROUTE_ID] for the route to follow
     */
    private fun startFollowing(intent: Intent) {
        if (_isRecording.value) stopRecording(false) // Stop recording if active
        val routeId = intent.getLongExtra(EXTRA_ROUTE_ID, -1)
        if (routeId.toInt() != -1) {
            _isFollowing.value = true
            _currentRouteId.value = routeId
            try {
                startForeground(NOTIFICATION_ID, buildNotification("Following route"))
            } catch (e: Exception) {
                Log.e("LocationService", "Failed to start foreground notification", e)
                stopSelf()
                return
            }
            loadRouteForFollowing(routeId)
            startLocationUpdates()
            coroutineScope.launch { serviceState.setFollowingState(true, routeId) }
        } else {
            stopSelf()
        }
    }
    /**
     * Stops following the current route.
     *
     * Clears remaining points and following state in [ServiceState].
     *
     * @param stopService If true, stops foreground service and location updates
     *                    (used when called from notification or ViewModel).
     *                    If false, keeps service running (used when switching to recording).
     */
    private fun stopFollowing(stopService: Boolean = true) {
        _isFollowing.value = false
        _followingRemainingPoints.value = emptyList()
        coroutineScope.launch {
            serviceState.clearFollowingState()
        }
        if (stopService) {
            stopLocationUpdates()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }
    /**
     * Generates a route name (if blank) and saves the new route to database.
     *
     * Creates a [RouteEntity] with timestamp-based name (format: "Route YYYY-MM-DD HH:MM"),
     * inserts it into Room, and initializes tracking variables.
     * Also saves the first point at current location if available.
     *
     * @param baseRouteName User-provided name, or blank to use auto-generated name
     */
    private fun generateRouteNameAndSave(baseRouteName: String) {
        coroutineScope.launch {
            try {
                val timestamp = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val routeName = baseRouteName.ifBlank { "(In Progress) Route ${dateFormat.format(Date(timestamp))}" }

                val newRoute = RouteEntity(
                    name = routeName,
                    startTime = timestamp
                )

                _currentRouteId.value = routeDao.insertRoute(newRoute)
                _currentRouteName.value = routeName

                serviceState.setRecordingState(true, _currentRouteId.value, routeName)

                val state = _locationState.value
                if (state.latitude != null && state.longitude != null) {
                    val point = RecordedPointEntity(
                        routeId = _currentRouteId.value,
                        latitude = state.latitude,
                        longitude = state.longitude,
                        timestamp = System.currentTimeMillis(),
                        sequenceNumber = 0
                    )
                    // Insert point (async operation)
                    routeDao.insertPoints(listOf(point))
                    // Initialize sync location tracking
                    lastRecordedLat = state.latitude
                    lastRecordedLon = state.longitude
                    sequenceCounter = 1
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf()
            }
        }
    }
    /**
     * Saves current route metadata to database.
     *
     * Updates the route's end time and point count.
     * Called when stopping recording to finalize the route.
     */
    private fun saveCurrentRoute() {
        coroutineScope.launch {
            val routeId = _currentRouteId.value ?: return@launch
            val route = routeDao.getRouteById(routeId) ?: return@launch
            val pointCount = routeDao.getPointsForRoute(routeId).first().size
            routeDao.updateRoute(route.copy(
                endTime = System.currentTimeMillis(),
                pointCount = pointCount
            ))
        }
    }
    /**
     * Loads route points for following and reverses the order.
     *
     * Points are reversed so the first point to reach is at index 0,
     * allowing efficient [List.drop] operations as points are reached.
     *
     * @param routeId ID of the route to load
     */
    private fun loadRouteForFollowing(routeId: Long) {
        coroutineScope.launch {
            val points = routeDao.getPointsForRoute(routeId).first()
            if (points.isNotEmpty()) {
                _followingRemainingPoints.value = points.reversed()
            }
        }
    }
    /**
     * Checks if the service is idle (not recording, not following, no bound clients).
     * If idle, stops location updates and the service itself.
     */
    private fun checkAndStopIfIdle() {
        if (!_isRecording.value && !_isFollowing.value && !serviceBound) {
            stopLocationUpdates()
            stopSelf()
        }
    }
    /**
     * Starts receiving location and GNSS status updates.
     *
     * Checks for Google Play Services availability first.
     * Requests location updates at HIGH_ACCURACY with 2000ms interval.
     * Registers GNSS callback for satellite tracking.
     */
    private fun startLocationUpdates() {
        val googlePlayServicesAvailable = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(applicationContext) == ConnectionResult.SUCCESS
        if (!googlePlayServicesAvailable) {
            stopSelf()
            return
        }
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L
        ).build()
        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, null)
            locationManager.registerGnssStatusCallback(gnssCallback, null)
        } catch (e: SecurityException) {
            Log.e("LocationService", "Unable to get location updates", e)
            stopSelf()
        } catch (e: IllegalArgumentException) {
            Log.e("LocationService", "Unable to get location updates", e)
            stopSelf()
        } catch (e: Exception) {
            Log.e("LocationService", "Unable to get location updates", e)
            stopSelf()
        }
    }
    /**
     * Stops location and GNSS status updates.
     * After stopping updates, checks if service should stop.
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationManager.unregisterGnssStatusCallback(gnssCallback)
        checkAndStopIfIdle()
    }
    /**
     * Handles a new location update from FusedLocationProviderClient.
     *
     * Updates the location state (coordinates, accuracy) and delegates to:
     * - [handleRecordingLocationUpdate] if currently recording
     * - [handleFollowingLocationUpdate] if currently following
     */
    private fun handleLocationUpdate(location: Location) {
        _locationState.value = _locationState.value.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            isLoading = false
        )
        if (_isRecording.value) {
            handleRecordingLocationUpdate(location)
        }
        if (_isFollowing.value) {
            handleFollowingLocationUpdate(location)
        }
    }
    /**
     * Handles a location update during active recording.
     *
     * Checks if the distance from the last recorded point is >= 5 meters.
     * If true, creates a new [RecordedPointEntity] and inserts it into Room.
     * Updates the local tracking variables for fast synchronous distance checks.
     *
     * @param location The new GPS location from FusedLocationProviderClient
     */
    private fun handleRecordingLocationUpdate(location: Location) {
        // Use tracked coordinates for distance check (synchronous)
        val distanceOk = if (lastRecordedLat == null || lastRecordedLon == null) true // First point
                         else calculatePointsDistance(
                                lastRecordedLat!!, lastRecordedLon!!,
                                location.latitude, location.longitude
                              ) >= 5.0

        if (distanceOk) {
            val point = RecordedPointEntity(
                routeId = _currentRouteId.value,
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = System.currentTimeMillis(),
                sequenceNumber = sequenceCounter
            )
            // Insert point into database (async)
            coroutineScope.launch {
                routeDao.insertPoints(listOf(point))
            }
            // Update location tracking variables
            lastRecordedLat = location.latitude
            lastRecordedLon = location.longitude
            sequenceCounter++
        }
    }
    /**
     * Handles a location update during active following.
     *
     * Checks if the next point to follow is within 5 meters of current location.
     * If within range, removes the point from [_followingRemainingPoints].
     * If no points remain, automatically stops following.
     *
     * @param location The new GPS location from FusedLocationProviderClient
     */
    private fun handleFollowingLocationUpdate(location: Location) {
        val nextPoint = _followingRemainingPoints.value.firstOrNull()
        if (nextPoint != null &&
            calculatePointsDistance(
                nextPoint.latitude, nextPoint.longitude,
                location.latitude, location.longitude
            ) <= 5.0
        ) {
            // Remove the point we've reached
            val remaining = _followingRemainingPoints.value.drop(1)
            _followingRemainingPoints.value = remaining

            // Check if following is complete
            if (remaining.isEmpty()) {
                stopFollowing(true)
            }
        }
    }
    /**
     * Builds a foreground service notification.
     *
     * Creates a low-priority, ongoing notification with a stop action.
     * The stop action varies based on current state (recording vs following).
     *
     * @param text The notification content text (e.g., "Recording route")
     * @return The built Notification object
     */
    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.action_stop),
                createPendingIntentForAction(
                    if (_isRecording.value) ACTION_STOP_RECORDING else ACTION_STOP_FOLLOWING
                )
            )
            .build()
    }
    /**
     * Creates a PendingIntent for notification actions (stop recording/following).
     *
     * The intent is routed to [MainActivity] which then delegates to the appropriate
     * ViewModel method based on the action type.
     *
     * @param actionType The action to perform (ACTION_STOP_RECORDING or ACTION_STOP_FOLLOWING)
     * @return The created PendingIntent
     */
    private fun createPendingIntentForAction(actionType: String): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = actionType
        }

        return PendingIntent.getActivity(
            this,
            actionType.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    /**
     * Creates the notification channel for the foreground service.
     *
     * Required for Android O+. The channel is set to LOW importance
     * to avoid making sound or popping up on the user's device.
     */
    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name_location_tracking)
        val descriptionText = getString(R.string.channel_description_location_tracking)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        Log.d("LocationService", "Notification channel created: ${CHANNEL_ID}, importance: $importance")
    }
}