package com.ameraldo.radar.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ameraldo.radar.data.DistanceUnits
import com.ameraldo.radar.utils.formatDistance
import com.ameraldo.radar.viewmodel.SettingsViewModel

/**
 * Settings screen composable.
 *
 * Allows users to configure:
 * - Distance units (Metric/Imperial)
 * - Maximum radar range
 * - Recording distance (minimum movement between recorded points)
 * - Following distance (proximity required to reach a waypoint)
 * - Adaptive following (skip missed waypoints when following a route)
 *
 * @param modifier Modifier for styling
 * @param settingsViewModel For reading/updating settings
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel
) {
    val distanceUnits by settingsViewModel.distanceUnits.collectAsState()
    val availableMaxRanges by settingsViewModel.availableMaxRanges.collectAsState()
    val maxRange by settingsViewModel.maxRange.collectAsState()
    val maxRangeIndex = availableMaxRanges.indexOf(maxRange).coerceAtLeast(0)
    val adaptiveFollowing by settingsViewModel.adaptiveFollowing.collectAsState()
    val availableDistances by settingsViewModel.availableDistances.collectAsState()
    val recordingDistance by settingsViewModel.recordingDistance.collectAsState()
    val followingDistance by settingsViewModel.followingDistance.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
            )
            Box(Modifier.size(48.dp)) // invisible placeholder
        }
        Spacer(modifier = Modifier.height(16.dp))
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
                    text = distanceUnits.formatDistance(maxRange.toFloat()),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recording Distance Setting
        DistanceSettingCard(
            title = "Recording Distance",
            description = "Minimum movement before a new GPS point is recorded",
            distance = recordingDistance,
            options = availableDistances,
            distanceUnits = distanceUnits,
            onDistanceChange = { settingsViewModel.updateRecordingDistance(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Following Distance Setting
        DistanceSettingCard(
            title = "Following Distance",
            description = "Proximity required to mark a followed waypoint as reached",
            distance = followingDistance,
            options = availableDistances,
            distanceUnits = distanceUnits,
            onDistanceChange = { settingsViewModel.updateFollowingDistance(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Adaptive following
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column (
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Adaptive Following",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "When following a route, automatically skip missed waypoints if you've moved ahead and are back on track further along the route",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f) // takes up remaining space, pushing switch right
                    )
                    Switch(
                        checked = adaptiveFollowing,
                        onCheckedChange = { enabled ->
                            settingsViewModel.updateAdaptiveFollowing(enabled)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Card with a discrete slider for a distance setting.
 *
 * @param title Setting name
 * @param description Short explanation of the setting
 * @param distance Currently selected distance
 * @param options Selectable distances for the current unit system
 * @param distanceUnits Current unit system (used for the value label)
 * @param onDistanceChange Callback when the user selects a new distance
 */
@Composable
private fun DistanceSettingCard(
    title: String,
    description: String,
    distance: Int,
    options: List<Int>,
    distanceUnits: DistanceUnits,
    onDistanceChange: (Int) -> Unit
) {
    val index = options.indexOf(distance).coerceAtLeast(0)

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
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Slider(
                value = index.toFloat(),
                onValueChange = { value ->
                    options.getOrNull(value.toInt())?.let(onDistanceChange)
                },
                valueRange = 0f..(options.size - 1).toFloat(),
                steps = (options.size - 2).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = distanceUnits.formatDistance(distance.toFloat()),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}