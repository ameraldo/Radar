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
import com.ameraldo.radar.utils.formatDistance
/**
 * Segmented button row for selecting radar range.
 *
 * Displays available range options (e.g., 250m, 500m, 750m, 1000m for metric).
 * The selected range controls the scale of the radar display.
 *
 * @param selectedIndex Currently selected range position (0 = 25%, 3 = 100%)
 * @param rangeList List of available range options
 * @param distanceUnits Current distance units (affects label formatting)
 * @param onRangeChange Callback when user selects a new range position
 * @param modifier Modifier for styling
 */
@Composable
fun RangeSelector(
    selectedIndex: Int,
    rangeList: List<Float>,
    distanceUnits: DistanceUnits,
    onRangeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        rangeList.forEachIndexed { index, range ->
            SegmentedButton(
                selected = selectedIndex == index,
                onClick = { onRangeChange(index) },
                shape = SegmentedButtonDefaults.itemShape(index, rangeList.size)
            ) {
                Text(distanceUnits.formatDistance(range))
            }
        }
    }
}