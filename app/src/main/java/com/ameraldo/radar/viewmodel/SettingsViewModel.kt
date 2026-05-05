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

/**
 * ViewModel that manages app settings (distance units, radar range).
 *
 * Provides reactive state for user preferences stored in DataStore.
 *
 * ## State Properties
 * - [distanceUnits]: Current distance units (METRIC or IMPERIAL)
 * - [maxRange]: Maximum radar range in meters/feet
 * - [selectedRange]: Currently selected radar range for display
 * - [availableMaxRanges]: Available max range options based on units
 *
 * ## Usage
 * ```
 * val settingsViewModel: SettingsViewModel = viewModel()
 * val distanceUnits by settingsViewModel.distanceUnits.collectAsState()
 *
 * // Change settings
 * settingsViewModel.updateDistanceUnits(DistanceUnits.IMPERIAL)
 * settingsViewModel.updateMaxRange(5280)
 * ```
 */
class SettingsViewModel(private val appSettings: AppSettings) : ViewModel() {
    companion object {
        /** Available max ranges in meters (metric) */
        private val MAX_METRIC_RANGES = listOf(100, 500, 1000, 1500, 2000, 5000)
        /** Available max ranges in feet (imperial) */
        private val MAX_IMPERIAL_RANGES = listOf(500, 2500, 5280, 7920, 10560, 26400)
    }

    /** Current distance units (METRIC or IMPERIAL) */
    val distanceUnits: StateFlow<DistanceUnits> = appSettings.distanceUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DistanceUnits.fromString(DEFAULT_DISTANCE_UNITS))

    /** Maximum radar range in current units */
    val maxRange: StateFlow<Int> = appSettings.maxRange
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_MAX_RANGE)

    /** Currently selected radar range for display */
    val selectedRange: StateFlow<Float> = appSettings.selectedRange
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_SELECTED_RANGE)

    /** Available max range options based on current units */
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

    /**
     * Updates the maximum radar range.
     *
     * Also resets selected range to 50% of the new max range.
     *
     * @param range New max range value in meters or feet
     */
    fun updateMaxRange(range: Int) {
        viewModelScope.launch {
            appSettings.setMaxRange(range)
            // Reset selected range to second step of new range list
            val defaultRange = range / 4f * 2
            appSettings.setSelectedRange(defaultRange)
        }
    }

    /**
     * Updates the selected radar range for display.
     *
     * @param range New selected range value
     */
    fun updateSelectedRange(range: Float) {
        viewModelScope.launch { appSettings.setSelectedRange(range) }
    }

    /**
     * Changes the distance units and resets max range to default.
     *
     * @param distanceUnits New distance units (METRIC or IMPERIAL)
     */
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

    /**
     * Generates a list of 4 range values (25%, 50%, 75%, 100% of max).
     *
     * @param maximum Maximum range value
     * @return List of 4 range values
     */
    fun generateRangeList(maximum: Int): List<Float> {
        val length = 4
        val divider = 4
        val step = maximum / divider
        return (1..length).map { i -> step * i * 1f }
    }
}