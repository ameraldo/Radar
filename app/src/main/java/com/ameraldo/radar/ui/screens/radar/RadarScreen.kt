package com.ameraldo.radar.ui.screens.radar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.ameraldo.radar.ui.components.CurrentLocationCard
import com.ameraldo.radar.ui.components.RadarView
import com.ameraldo.radar.utils.PolarPoint
import com.ameraldo.radar.viewmodel.LocationViewModel
import com.ameraldo.radar.viewmodel.SensorViewModel
import com.ameraldo.radar.viewmodel.SettingsViewModel
import kotlin.collections.emptyList

@Composable
fun RadarScreen(
    modifier: Modifier = Modifier,
    locationViewModel: LocationViewModel,
    sensorViewModel: SensorViewModel,
    settingsViewModel: SettingsViewModel,

    recordedPolarPoints: List<PolarPoint>,
    followingPolarPoints: List<PolarPoint>,

    onFollowingComplete: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val headingDegrees           by sensorViewModel.headingDegrees.collectAsState()

    val locationState            by locationViewModel.locationState.collectAsState()
    val isRecording              by locationViewModel.isRecording.collectAsState()
    val isFollowing              by locationViewModel.isFollowing.collectAsState()
    val followingRemainingPoints by locationViewModel.followingRemainingPoints.collectAsState()

    val radarDistanceUnits       by settingsViewModel.distanceUnits.collectAsState()
    val radarMaxRange            by settingsViewModel.maxRange.collectAsState()
    val selectedRange            by settingsViewModel.selectedRange.collectAsState()

    val radarRangeList = remember(radarMaxRange) { settingsViewModel.generateRangeList(radarMaxRange) }
    val radarRange = selectedRange.coerceIn(radarRangeList.min(), radarRangeList.max())

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
            radarRange = radarRange,
            radarDistanceUnits = radarDistanceUnits
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
                selectedRange = radarRange,
                rangeList = radarRangeList,
                distanceUnits = radarDistanceUnits,
                onRangeChange = { settingsViewModel.updateSelectedRange(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            RouteCard(locationViewModel, isRecording, isFollowing, onFollowingComplete)
        }
    }
}