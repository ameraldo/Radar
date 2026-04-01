package com.ameraldo.radar.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ameraldo.radar.viewmodel.LocationError
import com.ameraldo.radar.viewmodel.LocationState

@Composable
fun CurrentLocationCard(
    locationState: LocationState,
    isRecording: Boolean,
    onRetryGrantPermissions: () -> Unit
) {
    val context  = LocalContext.current
    val activity = context as? Activity

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (locationState.error != LocationError.NONE)
                MaterialTheme.colorScheme.errorContainer
            else
                CardDefaults.cardColors().containerColor
        )
    ) {
        when (val error = locationState.error) {
            is LocationError.PermissionDenied -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (error.permanentlyDenied) {
                        Text("Please enable location in app settings", color = MaterialTheme.colorScheme.error)
                        Button(
                            onClick = {
                                activity?.let {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.fromParts("package", it.packageName, null))
                                    it.startActivity(intent)
                                }
                            }
                        ) {
                            Text("Open Settings")
                        }
                    } else {
                        Text("Location permission needed", color = MaterialTheme.colorScheme.error)
                        Button(onClick = onRetryGrantPermissions) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
            is LocationError.ServiceUnavailable -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Location services unavailable", color = MaterialTheme.colorScheme.error)
                    Text(error.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = {
                        activity?.let {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            it.startActivity(intent)
                        }
                    }) {
                        Text("Open Location Settings")
                    }
                }
            }
            is LocationError.OtherError -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${error.message}", color = MaterialTheme.colorScheme.error)
                    // Could add retry button
                }
            }
            LocationError.NONE -> {
                when {
                    locationState.isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Acquiring location...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,  // pushes items to each end
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text       = "Current Location",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )


                            if (isRecording) {
                                RecordingIndicator()
                            }
                        }
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(20.dp,0.dp,20.dp,12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            CoordinateChip(label = "Latitude", value = locationState.latitude?.let  { "%.5f°".format(it) } ?: "—")
                            CoordinateChip(label = "Longitude", value = locationState.longitude?.let { "%.5f°".format(it) } ?: "—")
                            CoordinateChip(label = "Accuracy", value = locationState.accuracy?.let  { "±%.1fm".format(it) } ?: "—")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoordinateChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RecordingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 0.6f,
        targetValue   = 1.3f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(scale)
                .background(
                    color  = MaterialTheme.colorScheme.error,
                    shape  = CircleShape
                )
        )
        Text(
            text       = "Recording",
            style      = MaterialTheme.typography.labelMedium,
            color      = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold
        )
    }
}
