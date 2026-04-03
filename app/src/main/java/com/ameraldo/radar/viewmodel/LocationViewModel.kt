package com.ameraldo.radar.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.location.LocationManager
import android.location.GnssStatus
import android.content.Context
import androidx.lifecycle.application
import androidx.sqlite.SQLiteException
import com.ameraldo.radar.data.AppDatabase
import com.ameraldo.radar.data.RecordedPointEntity
import com.ameraldo.radar.data.RouteEntity
import com.ameraldo.radar.utils.calculatePointsDistance
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.text.SimpleDateFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

data class SatelliteBlip(
    val angleDeg: Float,            // azimuth → radar angle
    val radiusFraction: Float,      // elevation → distance from center
    val isLocked: Boolean,          // usedInFix
    val signalStrength: Float,      // Cn0DbHz
    val svid: Int,                  // space vehicle number → satellite ID within its constellation
    val constellation: String,      // satellite system → GPS, GLONASS, Galileo, BeiDou etc.
    val elevationDeg: Float,        // angle above horizon → 0° = horizon, 90° = directly overhead
    val hasAlmanac: Boolean,        // satellite has broadcast rough orbital data → needed before ephemeris
    val hasEphemeris: Boolean,      // satellite has broadcast precise orbital data → required for positioning
    val carrierFrequencyMhz: Float? // signal frequency in MHz → e.g. L1 = 1575.42, L5 = 1176.45, null if unavailable
)

data class LocationState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val satellites: List<SatelliteBlip> = emptyList(),
    val isLoading: Boolean = true,
    val error: LocationError = LocationError.NONE
)

sealed class LocationError {
    object NONE : LocationError()
    data class PermissionDenied(val permanentlyDenied: Boolean = false) : LocationError()
    data class ServiceUnavailable(val message: String) : LocationError()
    data class OtherError(val message: String, val throwable: Throwable? = null) : LocationError()
}

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val routeDao = database.routeDao()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val locationManager =
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _locationState = MutableStateFlow(LocationState())
    private val _isRecording = MutableStateFlow(false)
    private val _isFollowing = MutableStateFlow(false)
    private val _currentRouteId = MutableStateFlow<Long?>(null)
    private val _currentRouteName = MutableStateFlow<String?>(null)
    private val _followingRemainingPoints =
        MutableStateFlow<List<RecordedPointEntity>>(emptyList())

    val locationState: StateFlow<LocationState> = _locationState
    val isRecording: StateFlow<Boolean> = _isRecording
    val isFollowing: StateFlow<Boolean> = _isFollowing
    val currentRouteId: StateFlow<Long?> = _currentRouteId
    val currentRouteName: StateFlow<String?> = _currentRouteName
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentRoutePoints: StateFlow<List<RecordedPointEntity>> = currentRouteId
        .flatMapLatest { routeId ->
            if (routeId != null) {
                routeDao.getPointsForRoute(routeId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val followingRemainingPoints: StateFlow<List<RecordedPointEntity>> = _followingRemainingPoints

    /* ****************************************************************************************** *\
     * ************************************  PUBLIC METHODS  ************************************ *
    \* ************************************ Location Updates ************************************ */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { updateLocation(it) }
        }
    }
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
            viewModelScope.launch {
                _locationState.value = _locationState.value.copy(satellites = blips)
            }
        }
    }
    fun startLocationUpdates() {
        // Check Google Play Services availability
        val googlePlayServicesAvailable = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(application) == ConnectionResult.SUCCESS

        if (!googlePlayServicesAvailable) {
            _locationState.value = LocationState(
                isLoading = false,
                error = LocationError.ServiceUnavailable("Google Play Services not available. Please update Google Play Services.")
            )
            return
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L
        ).build()

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, null)
            // Register GNSS satellite callback
            locationManager.registerGnssStatusCallback(gnssCallback, null)
            _locationState.value = _locationState.value.copy(isLoading = true, error = LocationError.NONE)
        } catch (e: SecurityException) {
            _locationState.value = LocationState(
                isLoading = false,
                error = LocationError.OtherError("Error: ${e.message}")
            )
        } catch (e: IllegalArgumentException) {
            _locationState.value = LocationState(
                isLoading = false,
                error = LocationError.OtherError("Error: ${e.message}")
            )
        } catch (e: Exception) {
            _locationState.value = LocationState(
                isLoading = false,
                error = LocationError.OtherError("Error: ${e.message}")
            )
        }
    }
    fun stopLocationUpdates() {

        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationManager.unregisterGnssStatusCallback(gnssCallback)
    }
    /* ************************************** Permissions *************************************** */
    fun onPermissionDenied() {
        _locationState.value =
            LocationState(isLoading = false, error = LocationError.PermissionDenied())
    }
    fun onPermissionPermanentlyDenied() {
        _locationState.value = LocationState(
            isLoading = false,
            error     = LocationError.PermissionDenied(permanentlyDenied = true)
        )
    }
    /* *************************************** Recording **************************************** */
    fun startRecording() {
        // Initialize recording
        val recordingStartTime = System.currentTimeMillis()
        // Auto-generate route name: "Route YYYY-MM-DD HH:MM"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val routeName = "Route ${dateFormat.format(Date(recordingStartTime))}"
        // Create new route in db
        viewModelScope.launch {
            try {
                val newRoute = RouteEntity(
                    name = routeName,
                    startTime = recordingStartTime
                )
                _currentRouteId.value = routeDao.insertRoute(newRoute)
                _currentRouteName.value = routeName
                // Add first point immediately if location is available
                val state = _locationState.value
                if (state.latitude != null && state.longitude != null) {
                    val point = RecordedPointEntity(
                        routeId = currentRouteId.value,
                        latitude = state.latitude,
                        longitude = state.longitude,
                        timestamp = System.currentTimeMillis(),
                        sequenceNumber = 0
                    )
                    routeDao.insertPoints(listOf(point))
                }
                _isRecording.value = true
            } catch (e: SQLiteException) {
                _locationState.value = _locationState.value.copy(
                    error = LocationError.OtherError("Database Error: ${e.message}")
                )
            }
        }
    }
    fun stopRecording() {
        viewModelScope.launch {
            currentRouteId.value?.let { routeId ->
                // Update route with end time and point count
                val route = routeDao.getRouteById(routeId)
                route?.let {
                    routeDao.updateRoute(it.copy(
                        endTime = System.currentTimeMillis(),
                        pointCount = currentRoutePoints.value.size
                    ))
                }
            }
            _isRecording.value = false
        }
    }
    fun saveRoute(newName: String? = null) {
        viewModelScope.launch {
            currentRouteId.value?.let { routeId ->
                val route = routeDao.getRouteById(routeId)
                route?.let {
                    routeDao.updateRoute(it.copy(
                        name = newName ?: it.name,
                        endTime = System.currentTimeMillis(),
                        pointCount = currentRoutePoints.value.size
                    ))
                }
            }
            clearState()
        }
    }
    fun deleteRoute() {
        viewModelScope.launch {
            currentRouteId.value?.let { routeId ->
                routeDao.deleteRoute(routeId)
            }
            clearState()
        }
    }
    /* *************************************** Following **************************************** */
    fun startFollowing(routeId: Long, onLoaded: () -> Unit) {
        viewModelScope.launch {
            _currentRouteId.value = routeId
            viewModelScope.launch {
                currentRoutePoints
                    .first{ it.isNotEmpty() }
                    .let { _followingRemainingPoints.value = it.reversed() }
                    .also { _isFollowing.value = true }
                    .also { onLoaded() }
            }
        }
    }
    fun stopFollowing() {
        viewModelScope.launch {
            _followingRemainingPoints.value = emptyList()
            clearState()
        }
    }
    /* ****************************************************************************************** *\
    \* ***********************************  PRIVATE METHODS  ************************************ */
    private fun updateLocation(location: Location) {
        viewModelScope.launch {
            _locationState.value = _locationState.value.copy(
                latitude  = location.latitude,
                longitude = location.longitude,
                accuracy  = location.accuracy,
                isLoading = false
            )
            // record point if location moved more than 5 meters
            if (_isRecording.value) {
                val points = currentRoutePoints.value
                val last   = points.lastOrNull()
                if (last == null || calculatePointsDistance(
                        last.latitude, last.longitude,
                        location.latitude, location.longitude
                    ) >= 5.0
                ) {
                    val point = RecordedPointEntity(
                        routeId = currentRouteId.value,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = System.currentTimeMillis(),
                        sequenceNumber = points.size + 1
                    )
                    routeDao.insertPoints(listOf(point))
                }
            }
            // remove point if location moved more than 5 meters in following mode
            if (_isFollowing.value) {
                val nextPointToFollow = followingRemainingPoints.value.firstOrNull()
                if (nextPointToFollow != null && calculatePointsDistance(
                    nextPointToFollow.latitude, nextPointToFollow.longitude,
                    location.latitude, location.longitude) <= 5.0
                ) {
                    _followingRemainingPoints.value = _followingRemainingPoints.value.drop(1)
                }
            }
        }
    }

    private fun clearState() {
        _currentRouteId.value = null
        _currentRouteName.value = null
        _isRecording.value = false
        _isFollowing.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}