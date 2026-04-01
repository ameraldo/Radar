package com.ameraldo.radar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ameraldo.radar.data.AppSettings
import com.ameraldo.radar.data.AppSettings.Companion.DEFAULT_DISTANCE_UNITS
import com.ameraldo.radar.data.AppSettings.Companion.DEFAULT_MAX_RANGE
import com.ameraldo.radar.data.AppSettings.Companion.DEFAULT_SELECTED_RANGE
import com.ameraldo.radar.data.DistanceUnits
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val appSettings: AppSettings) : ViewModel() {
    companion object {
        private val MAX_METRIC_RANGES = listOf(100, 500, 1000, 1500, 2000, 5000)
        private val MAX_IMPERIAL_RANGES = listOf(500, 2500, 5280, 7920, 10560, 26400)
    }
    val distanceUnits: StateFlow<DistanceUnits> = appSettings.distanceUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DistanceUnits.fromString(DEFAULT_DISTANCE_UNITS))
    val maxRange: StateFlow<Int> = appSettings.maxRange
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_MAX_RANGE)
    val selectedRange: StateFlow<Float> = appSettings.selectedRange
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_SELECTED_RANGE)

    val availableMaxRanges: StateFlow<List<Int>> = combine(
        appSettings.distanceUnits,
        appSettings.maxRange
    ) { units, currentMaxRange ->
        val ranges = when (units) {
            DistanceUnits.METRIC -> MAX_METRIC_RANGES
            DistanceUnits.IMPERIAL -> MAX_IMPERIAL_RANGES
        }
        // Ensure saved range is in the list, otherwise use default
        if (currentMaxRange in ranges) ranges
        else when (units) {
            DistanceUnits.METRIC -> MAX_METRIC_RANGES
            DistanceUnits.IMPERIAL -> MAX_IMPERIAL_RANGES
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MAX_METRIC_RANGES)

    fun updateMaxRange(range: Int) {
        viewModelScope.launch {
            appSettings.setMaxRange(range)
            // Reset selected range to second step of new range list
            val defaultRange = range / 4f * 2
            appSettings.setSelectedRange(defaultRange)
        }
    }
    fun updateSelectedRange(range: Float) {
        viewModelScope.launch { appSettings.setSelectedRange(range) }
    }
    fun updateDistanceUnits(distanceUnits: DistanceUnits) {
        viewModelScope.launch {
            appSettings.setDistanceUnits(distanceUnits)
            val defaultRange = when (distanceUnits) {
                DistanceUnits.METRIC -> 1000
                DistanceUnits.IMPERIAL -> 5280
            }
            appSettings.setMaxRange(defaultRange)
        }
    }
}