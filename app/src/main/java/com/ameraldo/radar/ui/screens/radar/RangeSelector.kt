package com.ameraldo.radar.ui.screens.radar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ameraldo.radar.data.DistanceUnits

/**
 * Segmented button row for selecting radar range.
 *
 * Displays available range options (e.g., 250m, 500m, 750m, 1000m for metric).
 * The selected range controls the scale of the radar display.
 *
 * @param selectedRange Currently selected range value
 * @param rangeList List of available range options
 * @param distanceUnits Current distance units (affects label formatting)
 * @param onRangeChange Callback when user selects a new range
 * @param modifier Modifier for styling
 */
@Composable
fun RangeSelector(
    selectedRange: Float,
    rangeList: List<Float>,
    distanceUnits: DistanceUnits,
    onRangeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        rangeList.forEachIndexed { index, range ->
            SegmentedButton(
                selected = selectedRange == range,
                onClick = { onRangeChange(range) },
                shape = SegmentedButtonDefaults.itemShape(index, rangeList.size)
            ) {
                Text(
                    if (distanceUnits == DistanceUnits.METRIC)
                        if (range >= 1000f)
                            "${"%.1f".format(range / 1000f)}km"
                        else
                            "${"%.1f".format(range)}m"
                    else
                        if (range >= 5280f)
                            "${"%.1f".format(range / 5280f)}mi"
                        else
                            "${"%.1f".format(range)}ft"
                )
            }
        }
    }
}