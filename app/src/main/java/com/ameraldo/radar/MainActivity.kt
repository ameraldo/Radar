package com.ameraldo.radar

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ameraldo.radar.navigation.AppDestinations
import com.ameraldo.radar.ui.RadarApp
import com.ameraldo.radar.ui.theme.RadarTheme
import androidx.lifecycle.lifecycleScope
import com.ameraldo.radar.viewmodel.LocationViewModel
import com.ameraldo.radar.viewmodel.SensorViewModel
import com.ameraldo.radar.viewmodel.UIStateViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val locationViewModel: LocationViewModel by viewModels()
    private val sensorViewModel: SensorViewModel by viewModels()
    private val uiStateViewModel: UIStateViewModel by viewModels()

    private var isEnteringPiP = false
    private var isCurrentlyInRadarScreen = false

    private val appLifecycleObserver = object : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            // Start location and sensor updates when screen appears
            locationViewModel.startLocationUpdates()
            sensorViewModel.startListening()
        }

        override fun onStop(owner: LifecycleOwner) {
            if (!isEnteringPiP) {
                // Stop location and sensor updates when closing app
                locationViewModel.stopLocationUpdates()
                sensorViewModel.stopListening()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RadarTheme {
                RadarApp()
            }
        }
        // Observe UI state changes for PiP decisions
        lifecycleScope.launch {
            uiStateViewModel.currentDestination.collectLatest { destination ->
                isCurrentlyInRadarScreen = (destination == AppDestinations.RADAR)
            }
        }
        // Observe lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Only enter PiP (PictureInPicture) mode if currently on radar screen
        if (isCurrentlyInRadarScreen) {
            isEnteringPiP = true
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(1, 1))  // 1:1 for circular radar
                .setAutoEnterEnabled(true)
                .build()

            val success = enterPictureInPictureMode(pipParams)
            if (!success) { isEnteringPiP = false }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        uiStateViewModel.updateIsInPiPMode(isInPictureInPictureMode)

        if (isInPictureInPictureMode) {
            // Successfully entered PiP - sensors keep running
            isEnteringPiP = false
        } else {
            // PiP denied or exited - restore normal behavior
            isEnteringPiP = false
            locationViewModel.startLocationUpdates()
            sensorViewModel.startListening()
        }
    }
}