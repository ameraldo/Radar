package com.ameraldo.radar.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ameraldo.radar.data.DistanceUnits
import com.ameraldo.radar.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel
) {
    val distanceUnits by settingsViewModel.distanceUnits.collectAsState()
    val availableMaxRanges by settingsViewModel.availableMaxRanges.collectAsState()
    val maxRange by settingsViewModel.maxRange.collectAsState()
    val maxRangeIndex = availableMaxRanges.indexOf(maxRange).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        // Distance Units Setting
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Distance Units",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text =
                        if (distanceUnits == DistanceUnits.METRIC) "Metric (km, m)"
                        else "Imperial (mi, ft)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DistanceUnits.entries.forEachIndexed { index, unit ->
                        SegmentedButton(
                            selected = distanceUnits == unit,
                            onClick = { settingsViewModel.updateDistanceUnits(unit) },
                            shape = SegmentedButtonDefaults.itemShape(index, 2)
                        ) {
                            Text(unit.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Radar Range Setting
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Maximum Radar Range",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Slider(
                    value = maxRangeIndex.toFloat(),
                    onValueChange = { index ->
                        settingsViewModel.updateMaxRange(availableMaxRanges[index.toInt()])
                    },
                    valueRange = 0f..(availableMaxRanges.size - 1).toFloat(),
                    steps = availableMaxRanges.size - 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                // Display current value label
                Text(
                    text = when (distanceUnits) {
                        DistanceUnits.METRIC -> {
                            val range = maxRange
                            if (range >= 1000) "${"%.1f".format(range / 1000.0)}km"
                            else "${range}m"
                        }
                        DistanceUnits.IMPERIAL -> {
                            val range = maxRange
                            if (range >= 5280) "${"%.1f".format(range / 5280.0)}mi"
                            else "${range}ft"
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}