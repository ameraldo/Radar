package com.ameraldo.radar

import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ameraldo.radar.navigation.AppDestinations
import com.ameraldo.radar.ui.RadarApp
import com.ameraldo.radar.ui.theme.RadarTheme
import com.ameraldo.radar.viewmodel.LocationViewModel
import com.ameraldo.radar.viewmodel.RouteViewModel
import com.ameraldo.radar.viewmodel.SensorViewModel
import com.ameraldo.radar.viewmodel.UIStateViewModel
import com.ameraldo.radar.service.LocationService
import kotlinx.coroutines.launch

/**
 * Main entry point for the Radar application.
 *
 * Responsibilities:
 * - Binds to [LocationService] for GPS tracking
 * - Manages Picture-in-Picture (PiP) mode for background radar display
 * - Handles notification intents (stop recording/following from notification bar)
 * - Orchestrates lifecycle for sensors and service connection
 */
class MainActivity : ComponentActivity() {
    /** Reference to the bound LocationService (null when not bound) */
    private var locationService: LocationService? = null
    /** Tracks whether the service is currently bound */
    private var serviceBound = false

    /** ViewModel for GPS/location state (bound to service) */
    private val locationViewModel: LocationViewModel by viewModels()
    /** ViewModel for compass sensor data */
    private val sensorViewModel: SensorViewModel by viewModels()
    /** ViewModel for UI state (navigation, PiP, stop actions) */
    private val uiStateViewModel: UIStateViewModel by viewModels()
    /** ViewModel for saved routes */
    private val routesViewModel: RouteViewModel by viewModels()

    /** Flag to prevent multiple PiP entry attempts */
    private var isEnteringPiP = false

    /** Service connection for binding/unbinding from LocationService */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            serviceBound = true
            locationViewModel.setLocationService(locationService!!)
            Log.d("MainActivity", "Service bound successfully")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationService = null
            serviceBound = false
            locationViewModel.clearLocationService()
            Log.d("MainActivity", "Service unbound")
        }
    }

    /** Lifecycle observer for sensor management (start listening onStart, stop onStop) */
    private val appLifecycleObserver = object : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            sensorViewModel.startListening()
        }

        override fun onStop(owner: LifecycleOwner) {
            sensorViewModel.stopListening()
        }
    }

    /**
     * Binds to LocationService to enable communication.
     * Called in onCreate() to establish connection early.
     */
    private fun bindLocationService() {
        val intent = Intent(this, LocationService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * Handles intents from notification bar actions.
     *
     * Routes stop actions to the UI state:
     * - ACTION_STOP_RECORDING → Sets pending stop action for recording
     * - ACTION_STOP_FOLLOWING → Sets pending stop action for following
     * Also navigates to RADAR tab to show the dialog.
     *
     * @param intent The intent to handle (may be null)
     */
    private fun handleNotificationIntent(intent: Intent?) {
        when (intent?.action) {
            LocationService.ACTION_STOP_RECORDING -> {
                uiStateViewModel.setPendingStopAction(UIStateViewModel.StopAction.RECORDING)
                uiStateViewModel.updateDestination(AppDestinations.RADAR)
            }
            LocationService.ACTION_STOP_FOLLOWING -> {
                uiStateViewModel.setPendingStopAction(UIStateViewModel.StopAction.FOLLOWING)
                uiStateViewModel.updateDestination(AppDestinations.RADAR)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Bind to LocationService for GPS tracking
        bindLocationService()
        // Handle any notification intent that started this activity
        handleNotificationIntent(intent)

        setContent {
            RadarTheme {
                RadarApp(
                    locationViewModel = locationViewModel,
                    sensorViewModel = sensorViewModel,
                    uiStateViewModel = uiStateViewModel,
                    routesViewModel = routesViewModel
                )
            }
        }

        // Register lifecycle observer for sensor management (app-level, not activity)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }
    /**
     * Handles new intents (e.g., from notification bar when activity is already running).
     * Routes notification actions to the UI state.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }
    override fun onDestroy() {
        super.onDestroy()
        // Clean up lifecycle observer and service connection
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
    /**
     * Called when user leaves the app (e.g., Home button, Recent Apps).
     * Enters Picture-in-Picture mode if recording or following is active.
     *
     * Uses 1:1 aspect ratio for square radar display.
     * Sets [isEnteringPiP] flag to prevent multiple entry attempts.
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Enter PiP mode only when recording or following is active
        val shouldEnterPiP = locationViewModel.isRecording.value || locationViewModel.isFollowing.value
        if (shouldEnterPiP && !isEnteringPiP) {
            isEnteringPiP = true
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(1, 1)) // Square aspect ratio for radar
                .build()
            val success = enterPictureInPictureMode(pipParams)
            if (!success) {
                isEnteringPiP = false
            }
        }
    }
    /**
     * Called when PiP mode changes (entered or exited).
     *
     * - When entering PiP: Resets the entering flag
     * - When exiting PiP: Resets flag and restarts sensor listening
     *
     * @param isInPictureInPictureMode true if now in PiP mode
     * @param newConfig New configuration (includes screen info)
     */
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        uiStateViewModel.updateIsInPiPMode(isInPictureInPictureMode)

        if (isInPictureInPictureMode) {
            // In PiP mode: clear entering flag (transition complete)
            isEnteringPiP = false
        } else {
            // Exited PiP mode: restart sensors and clear flags
            isEnteringPiP = false
            sensorViewModel.startListening()
        }
    }
}