package com.ameraldo.radar.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AssistantDirection
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ameraldo.radar.ui.components.CurrentLocationCard
import com.ameraldo.radar.ui.components.RadarView
import com.ameraldo.radar.viewmodel.LocationViewModel
import com.ameraldo.radar.viewmodel.SensorViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    locationViewModel: LocationViewModel,
    sensorViewModel: SensorViewModel,
    onRequestPermission: () -> Unit,
    onRecord: () -> Unit = {}
) {
    val locationState  by locationViewModel.locationState.collectAsState()
    val isRecording    by locationViewModel.isRecording.collectAsState()
    val headingDegrees by sensorViewModel.headingDegrees.collectAsState()

    // Request permissions when screen appears
    LaunchedEffect(Unit) {
        onRequestPermission()
    }

    val windowInfo = LocalWindowInfo.current
    val radarHeight  = with(LocalDensity.current) {
        (windowInfo.containerSize.height * 0.5f).toDp() // fixed 50% of screen
    }

    Box(Modifier.fillMaxSize()) {
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
                satelliteBlips = locationState.satellites
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
                SatellitesList(satellites = locationState.satellites)
            }
        }
        FloatingActionButton(
            modifier       = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            onClick        = { onRecord() },
            containerColor = if (isRecording)
                MaterialTheme.colorScheme.surfaceVariant  // dimmed when recording
            else
                MaterialTheme.colorScheme.primaryContainer,
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.AssistantDirection,
                contentDescription = "Go to Routes",
                tint               = if (isRecording)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}