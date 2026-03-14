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

@Composable
fun RangeSelector(
    rangeMeters: Float,
    onRangeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        listOf(10f, 50f, 100f, 500f, 1000f).forEachIndexed { index, range ->
            SegmentedButton(
                selected = rangeMeters == range,
                onClick = { onRangeChange(range) },
                shape = SegmentedButtonDefaults.itemShape(index, 5)
            ) {
                Text(if (range >= 1000f) "${range.toInt() / 1000}km" else "${range.toInt()}m")
            }
        }
    }
}