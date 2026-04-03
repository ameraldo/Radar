package com.ameraldo.radar

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ameraldo.radar.ui.RadarApp
import com.ameraldo.radar.ui.theme.RadarTheme
import com.ameraldo.radar.viewmodel.LocationViewModel
import com.ameraldo.radar.viewmodel.RouteViewModel
import com.ameraldo.radar.viewmodel.SensorViewModel
import com.ameraldo.radar.viewmodel.UIStateViewModel

class MainActivity : ComponentActivity() {
    private val locationViewModel: LocationViewModel by viewModels()
    private val sensorViewModel: SensorViewModel by viewModels()
    private val uiStateViewModel: UIStateViewModel by viewModels()
    private val routesViewModel: RouteViewModel by viewModels()

    private var isEnteringPiP = false

    private val appLifecycleObserver = object : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            // Start location and sensor updates when screen appears
            locationViewModel.startLocationUpdates()
            sensorViewModel.startListening()
        }

        override fun onStop(owner: LifecycleOwner) {
            // Stop location and sensor updates only when app is not following or recording
            if (!isEnteringPiP && !locationViewModel.isFollowing.value && !locationViewModel.isRecording.value) {
                locationViewModel.stopLocationUpdates()
                sensorViewModel.stopListening()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("PiP", "onCreate called, savedInstanceState: $savedInstanceState")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        // Observe lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
    }

    override fun onPause() {
        Log.d("PiP", "onPause called")
        super.onPause()
        if (!isEnteringPiP) {
            isEnteringPiP = true
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(1, 1))
                .build()
            val success = enterPictureInPictureMode(pipParams)
            Log.d("PiP", "PiP enter result: $success")
            if (!success) {
                Log.w("PiP", "PiP enter failed")
                isEnteringPiP = false
            }
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