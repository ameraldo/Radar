package com.ameraldo.radar.ui.screens.radar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ameraldo.radar.data.DistanceUnits
import com.ameraldo.radar.viewmodel.SettingsViewModel

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