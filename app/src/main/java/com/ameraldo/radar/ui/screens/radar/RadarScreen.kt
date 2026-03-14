package com.ameraldo.radar.ui.screens.radar

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.ameraldo.radar.ui.components.CurrentLocationCard
import com.ameraldo.radar.ui.components.RadarView
import com.ameraldo.radar.utils.toPolarPoints
import com.ameraldo.radar.viewmodel.LocationViewModel
import com.ameraldo.radar.viewmodel.SensorViewModel
import kotlin.collections.emptyList

import com.ameraldo.radar.viewmodel.UIStateViewModel


@Composable
fun RadarScreen(
    modifier: Modifier = Modifier,
    locationViewModel: LocationViewModel,
    sensorViewModel: SensorViewModel,
    uiStateViewModel: UIStateViewModel,
    onFollowingComplete: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val headingDegrees           by sensorViewModel.headingDegrees.collectAsState()
    val locationState            by locationViewModel.locationState.collectAsState()
    val isRecording              by locationViewModel.isRecording.collectAsState()
    val isFollowing              by locationViewModel.isFollowing.collectAsState()
    val currentRoutePoints       by locationViewModel.currentRoutePoints.collectAsState()
    val followingRemainingPoints by locationViewModel.followingRemainingPoints.collectAsState()
    val isInPiPMode              by uiStateViewModel.isInPiPMode.collectAsState()

    var radarRangeMeters by rememberSaveable { mutableFloatStateOf(100f) }

    // Convert recorded points to polar coordinates relative to current position
    val recordedPolarPoints = remember(currentRoutePoints, locationState.latitude,
        locationState.longitude, radarRangeMeters) {
        val lat = locationState.latitude
        val lon = locationState.longitude
        if (lat != null && lon != null) {
            toPolarPoints(lat, lon, currentRoutePoints, radarRangeMeters)
        } else emptyList()
    }

    // Convert following points to polar coordinates relative to current position
    val followingPolarPoints = remember(followingRemainingPoints, locationState.latitude,
                                        locationState.longitude, radarRangeMeters) {
        val lat = locationState.latitude
        val lon = locationState.longitude
        if (lat != null && lon != null && followingRemainingPoints.isNotEmpty()) {
            toPolarPoints(lat, lon, followingRemainingPoints, radarRangeMeters)
        } else emptyList()
    }

    if (isInPiPMode) {
        // PiP mode: Show only RadarView
        RadarView(
            modifier = Modifier.fillMaxSize(),
            headingDegrees = headingDegrees,
            satelliteBlips = locationState.satellites,
            recordedPoints = if (isFollowing) followingPolarPoints
            else if (isRecording) recordedPolarPoints
            else emptyList(),
            nextPointToFollow = if (isFollowing && followingPolarPoints.isNotEmpty())
                followingPolarPoints.firstOrNull()
            else null,
            radarRangeMeters = radarRangeMeters
        )
    } else {
        val windowInfo = LocalWindowInfo.current
        val radarHeight = with(LocalDensity.current) {
            (windowInfo.containerSize.height * 0.5f).toDp() // fixed 50% of screen
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .offset(y = (-36).dp), // pull the component up
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RadarView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(radarHeight),
                headingDegrees = headingDegrees,
                recordedPoints = if (isFollowing) followingPolarPoints
                else if (isRecording) recordedPolarPoints
                else emptyList(),
                nextPointToFollow = if (isFollowing && followingRemainingPoints.isNotEmpty())
                    followingPolarPoints.firstOrNull()
                else
                    null,
                radarRangeMeters = radarRangeMeters
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CurrentLocationCard(
                    locationState,
                    isRecording,
                    onRequestPermission
                )
                Spacer(modifier = Modifier.height(8.dp))
                RangeSelector(
                    rangeMeters = radarRangeMeters,
                    onRangeChange = { radarRangeMeters = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                RouteCard(locationViewModel, isRecording, isFollowing, onFollowingComplete)
            }
        }
    }
}