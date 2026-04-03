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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ameraldo.radar.data.AppSettings
import com.ameraldo.radar.navigation.AppDestinations
import com.ameraldo.radar.ui.components.RadarView
import com.ameraldo.radar.ui.screens.home.HomeScreen
import com.ameraldo.radar.ui.screens.radar.RadarScreen
import com.ameraldo.radar.ui.screens.routes.RoutesScreen
import com.ameraldo.radar.ui.screens.settings.SettingsScreen
import com.ameraldo.radar.utils.toPolarPoints
import com.ameraldo.radar.viewmodel.LocationViewModel
import com.ameraldo.radar.viewmodel.RouteViewModel
import com.ameraldo.radar.viewmodel.SensorViewModel
import com.ameraldo.radar.viewmodel.SettingsViewModel
import com.ameraldo.radar.viewmodel.UIStateViewModel

@Composable
fun RadarApp(
    locationViewModel: LocationViewModel,
    sensorViewModel: SensorViewModel,
    uiStateViewModel: UIStateViewModel,
    routesViewModel: RouteViewModel,
) {
    val headingDegrees     by sensorViewModel.headingDegrees.collectAsState()

    val locationState            by locationViewModel.locationState.collectAsState()
    val isRecording              by locationViewModel.isRecording.collectAsState()
    val isFollowing              by locationViewModel.isFollowing.collectAsState()
    val currentRoutePoints       by locationViewModel.currentRoutePoints.collectAsState()
    val followingRemainingPoints by locationViewModel.followingRemainingPoints.collectAsState()

    val currentDestination by uiStateViewModel.currentDestination.collectAsState()
    val isInPiPMode        by uiStateViewModel.isInPiPMode.collectAsState()

    val context        = LocalContext.current
    val activity       = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val appSettings = remember { AppSettings(context) }
    val settingsViewModel = remember { SettingsViewModel(appSettings) }


    val radarDistanceUnits       by settingsViewModel.distanceUnits.collectAsState()
    val radarMaxRange            by settingsViewModel.maxRange.collectAsState()
    val selectedRange            by settingsViewModel.selectedRange.collectAsState()

    val radarRangeList = remember(radarMaxRange) { settingsViewModel.generateRangeList(radarMaxRange) }
    val radarRange = selectedRange.coerceIn(radarRangeList.min(), radarRangeList.max())

    // Convert recorded points to polar coordinates relative to current position
    val recordedPolarPoints = remember(currentRoutePoints, locationState.latitude,
        locationState.longitude, radarRange, radarDistanceUnits) {
        val lat = locationState.latitude
        val lon = locationState.longitude
        if (lat != null && lon != null) {
            toPolarPoints(lat, lon, currentRoutePoints, radarRange, radarDistanceUnits)
        } else emptyList()
    }

    // Convert following points to polar coordinates relative to current position
    val followingPolarPoints = remember(followingRemainingPoints, locationState.latitude,
        locationState.longitude, radarRange, radarDistanceUnits) {
        val lat = locationState.latitude
        val lon = locationState.longitude
        if (lat != null && lon != null && followingRemainingPoints.isNotEmpty()) {
            toPolarPoints(lat, lon, followingRemainingPoints, radarRange, radarDistanceUnits)
        } else emptyList()
    }

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

    if (isInPiPMode) {
        RadarView(
            modifier = Modifier
                .fillMaxSize()
                .scale(1.5f), // Zoom
            headingDegrees = headingDegrees,
            recordedPoints = if (isFollowing) followingPolarPoints
            else if (isRecording) recordedPolarPoints
            else emptyList(),
            nextPointToFollow = if (isFollowing && followingPolarPoints.isNotEmpty())
                followingPolarPoints.firstOrNull()
            else null,
            radarRange = radarRange,
            radarDistanceUnits = radarDistanceUnits
        )
    } else {
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
                        settingsViewModel   = settingsViewModel,

                        recordedPolarPoints = recordedPolarPoints,
                        followingPolarPoints= followingPolarPoints,

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
                    AppDestinations.SETTINGS -> SettingsScreen(
                        modifier            = Modifier.padding(innerPadding),
                        settingsViewModel   = settingsViewModel
                    )
                }
            }
        }
    }
}