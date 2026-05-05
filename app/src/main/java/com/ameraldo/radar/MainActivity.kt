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
import com.ameraldo.radar.viewmodel.StopAction
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var locationService: LocationService? = null
    private var serviceBound = false

    private val locationViewModel: LocationViewModel by viewModels()
    private val sensorViewModel: SensorViewModel by viewModels()
    private val uiStateViewModel: UIStateViewModel by viewModels()
    private val routesViewModel: RouteViewModel by viewModels()

    private var isEnteringPiP = false

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

    private val appLifecycleObserver = object : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            sensorViewModel.startListening()
        }

        override fun onStop(owner: LifecycleOwner) {
            sensorViewModel.stopListening()
        }
    }

    private fun bindLocationService() {
        val intent = Intent(this, LocationService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        when (intent?.action) {
            LocationService.ACTION_STOP_RECORDING -> {
                uiStateViewModel.setPendingStopAction(StopAction.RECORDING)
                uiStateViewModel.updateDestination(AppDestinations.RADAR)
            }
            LocationService.ACTION_STOP_FOLLOWING -> {
                uiStateViewModel.setPendingStopAction(StopAction.FOLLOWING)
                uiStateViewModel.updateDestination(AppDestinations.RADAR)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bindLocationService()
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

        // Only observe lifecycle for sensors, not service management
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }
    override fun onNewIntent(intent: Intent) {
            super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }
    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val shouldEnterPiP = locationViewModel.isRecording.value || locationViewModel.isFollowing.value
        if (shouldEnterPiP && !isEnteringPiP) {
            isEnteringPiP = true
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(1, 1))
                .build()
            val success = enterPictureInPictureMode(pipParams)
            if (!success) {
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
            isEnteringPiP = false
        } else {
            isEnteringPiP = false
            sensorViewModel.startListening()
        }
    }
}