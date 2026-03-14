package com.ameraldo.radar.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ameraldo.radar.ui.theme.green500Color
import com.ameraldo.radar.ui.theme.lightGreen500Color
import com.ameraldo.radar.ui.theme.orange500Color
import com.ameraldo.radar.ui.theme.red500Color
import com.ameraldo.radar.ui.theme.yellow500Color
import com.ameraldo.radar.viewmodel.SatelliteBlip

@Composable
fun SatellitesList(
    satellites: List<SatelliteBlip>,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text       = "Satellites",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = "${satellites.count { it.isLocked }} locked / ${satellites.size} in view",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector        = Icons.Default.Info,
                contentDescription = "View satellites"
            )
        }
    }

    if (showDialog) {
        SatelliteDialog(
            satellites = satellites,
            onDismiss  = { showDialog = false }
        )
    }
}

@Composable
private fun SatelliteDialog(
    satellites: List<SatelliteBlip>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier        = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.89f)
                    .padding(top = 16.dp),  // distance from top of screen
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Dialog header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Satellites",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${satellites.count { it.isLocked }} locked / ${satellites.size} in view",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }

                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    // Satellite list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (satellites.isEmpty()) {
                            item {
                                Text(
                                    text = "No satellites in view",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        } else {
                            items(satellites) { sat ->
                                SatelliteRow(sat)
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 8.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SatelliteRow(sat: SatelliteBlip) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

        // Title row: constellation + svid + lock badge
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.fillMaxWidth()
        ) {
            Text(
                text       = "${sat.constellation} #${sat.svid}",
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            LockBadge(sat.isLocked)
        }

        // Signal bar
        SignalBar(sat.signalStrength)

        // Detail grid
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                DetailItem("Azimuth",   "%.1f°".format(sat.angleDeg))
                DetailItem("Elevation", "%.1f°".format(sat.elevationDeg))
                DetailItem("Signal",    "%.1f dB-Hz".format(sat.signalStrength))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                DetailItem("Almanac",   if (sat.hasAlmanac) "Yes" else "No")
                DetailItem("Ephemeris", if (sat.hasEphemeris) "Yes" else "No")
                DetailItem("Frequency", sat.carrierFrequencyMhz?.let { "%.2f MHz".format(it) } ?: "—")
            }
        }
    }
}

@Composable
private fun LockBadge(isLocked: Boolean) {
    val text  = if (isLocked) "LOCKED" else "VISIBLE"
    val color = if (isLocked) green500Color else yellow500Color
    Text(
        text       = text,
        style      = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color      = color
    )
}

@Composable
private fun SignalBar(cn0: Float) {
    val fraction = (cn0 / 50f).coerceIn(0f, 1f)
    val barColor = when {
        cn0 >= 42f -> green500Color
        cn0 >= 35f -> lightGreen500Color
        cn0 >= 30f -> yellow500Color
        cn0 >= 20f -> orange500Color
        else       -> red500Color
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Signal strength", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                signalLabel(cn0),
                style = MaterialTheme.typography.labelSmall,
                color = barColor,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // Background track
                drawRoundRect(
                    color        = Color.LightGray.copy(alpha = 0.3f),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                // Filled portion
                drawRoundRect(
                    color        = barColor,
                    size         = Size(size.width * fraction, size.height),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
    }
}

private fun signalLabel(cn0: Float) = when {
    cn0 >= 42f -> "Excellent"
    cn0 >= 35f -> "Good"
    cn0 >= 30f -> "Usable"
    cn0 >= 20f -> "Poor"
    else       -> "Very Weak"
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text  = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}