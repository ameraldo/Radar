package com.ameraldo.radar.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ameraldo.radar.navigation.AppDestinations
import com.ameraldo.radar.ui.screens.home.HomeScreen
import com.ameraldo.radar.ui.screens.radar.RadarScreen
import com.ameraldo.radar.ui.screens.routes.RoutesScreen
import com.ameraldo.radar.viewmodel.LocationViewModel
import com.ameraldo.radar.viewmodel.RouteViewModel
import com.ameraldo.radar.viewmodel.SensorViewModel
import com.ameraldo.radar.viewmodel.UIStateViewModel

@PreviewScreenSizes
@Composable
fun RadarApp(
    locationViewModel: LocationViewModel = viewModel(),
    sensorViewModel: SensorViewModel = viewModel(),
    routesViewModel: RouteViewModel = viewModel(),
    uiStateViewModel: UIStateViewModel = viewModel()
) {
    val isRecording by locationViewModel.isRecording.collectAsState()
    val currentDestination by uiStateViewModel.currentDestination.collectAsState()

    val context        = LocalContext.current
    val activity       = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationViewModel.startLocationUpdates()
        } else {
            val permanentlyDenied = activity?.let {
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } ?: false
            if (permanentlyDenied) locationViewModel.onPermissionPermanentlyDenied()
            else locationViewModel.onPermissionDenied()
        }
    }

    // resume check
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                if (granted) locationViewModel.startLocationUpdates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val onRequestPermission = {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            destination.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = destination == currentDestination,
                    onClick = { uiStateViewModel.updateDestination(destination) }
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(),
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(
                    modifier            = Modifier.padding(innerPadding),
                    locationViewModel   = locationViewModel,
                    sensorViewModel     = sensorViewModel,
                    onRequestPermission = onRequestPermission,
                    onRecord            = {
                        if (!isRecording) locationViewModel.startRecording()
                        uiStateViewModel.updateDestination(AppDestinations.RADAR)
                    }
                )
                AppDestinations.RADAR -> RadarScreen(
                    modifier            = Modifier.padding(innerPadding),
                    locationViewModel   = locationViewModel,
                    sensorViewModel     = sensorViewModel,
                    uiStateViewModel    = uiStateViewModel,
                    onFollowingComplete = { locationViewModel.stopFollowing() },
                    onRequestPermission = onRequestPermission
                )
                AppDestinations.ROUTES -> RoutesScreen(
                    modifier            = Modifier.padding(innerPadding),
                    routesViewModel     = routesViewModel,
                    onFollowRoute = {
                        routeId -> locationViewModel.startFollowing(routeId, onLoaded = {
                            uiStateViewModel.updateDestination(AppDestinations.RADAR)
                        })
                    }
                )
            }
        }
    }
}