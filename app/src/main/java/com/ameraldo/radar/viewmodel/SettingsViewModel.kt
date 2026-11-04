package com.ameraldo.radar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ameraldo.radar.data.AppSettings
import com.ameraldo.radar.data.DistanceUnits
import com.ameraldo.radar.data.rangeSteps
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel that manages app settings (distance units, radar range, adaptive following).
 *
 * Provides reactive state for user preferences stored in DataStore. All unit-dependent
 * option lists and defaults come from [DistanceUnits]; this class only exposes state
 * and orchestrates updates.
 *
 * ## State Properties
 * - [distanceUnits]: Current distance units (METRIC or IMPERIAL)
 * - [maxRange]: Maximum radar range in meters/feet
 * - [selectedRangeIndex]: Selected radar range position (0 = 25%, 3 = 100%)
 * - [selectedRange]: Currently selected radar range for display
 * - [radarRangeList]: Available radar range values derived from [maxRange]
 * - [availableMaxRanges]: Available max range options based on units
 * - [recordingDistance]: Minimum movement before a GPS point is recorded
 * - [followingDistance]: Proximity required to mark a followed waypoint as reached
 * - [availableDistances]: Available recording/following distance options based on units
 * - [adaptiveFollowing]: Whether adaptive following is enabled
 *
 * ## Usage
 * ```
 * val settingsViewModel: SettingsViewModel = viewModel()
 * val distanceUnits by settingsViewModel.distanceUnits.collectAsState()
 *
 * // Change settings
 * settingsViewModel.updateDistanceUnits(DistanceUnits.IMPERIAL)
 * settingsViewModel.updateMaxRange(5280)
 * settingsViewModel.updateAdaptiveFollowing(true)
 * ```
 */
class SettingsViewModel(private val appSettings: AppSettings) : ViewModel() {

    /** Current distance units (METRIC or IMPERIAL) */
    val distanceUnits: StateFlow<DistanceUnits> = appSettings.distanceUnits
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DistanceUnits.METRIC
        )

    /** Maximum radar range in current units */
    val maxRange: StateFlow<Int> = appSettings.maxRange
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DistanceUnits.METRIC.defaultMaxRange
        )

    /** Selected radar range position (0 = 25%, 3 = 100%) */
    val selectedRangeIndex: StateFlow<Int> = appSettings.selectedRangeIndex
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DistanceUnits.DEFAULT_SELECTED_RANGE_INDEX
        )

    /** Available max range options based on current units */
    val availableMaxRanges: StateFlow<List<Int>> = appSettings.distanceUnits
        .map { it.maxRangeOptions }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DistanceUnits.METRIC.maxRangeOptions
        )

    /** Whether adaptive following is enabled (scans all remaining points vs single-point check) */
    val adaptiveFollowing: StateFlow<Boolean> = appSettings.adaptiveFollowing
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AppSettings.DEFAULT_ADAPTIVE_FOLLOWING
        )

    /** Minimum movement (in current units) before a new GPS point is recorded */
    val recordingDistance: StateFlow<Int> = appSettings.recordingDistance
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DistanceUnits.METRIC.defaultDistance
        )

    /** Proximity (in current units) required to mark a followed waypoint as reached */
    val followingDistance: StateFlow<Int> = appSettings.followingDistance
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DistanceUnits.METRIC.defaultDistance
        )

    /** Available recording/following distance options based on current units */
    val availableDistances: StateFlow<List<Int>> = appSettings.distanceUnits
        .map { it.distanceOptions }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DistanceUnits.METRIC.distanceOptions
        )

    /** The four radar range options derived from [maxRange] */
    val radarRangeList: StateFlow<List<Float>> = appSettings.maxRange
        .map(::rangeSteps)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            rangeSteps(DistanceUnits.METRIC.defaultMaxRange)
        )

    /** Currently selected radar range for display */
    val selectedRange: StateFlow<Float> = combine(
        appSettings.maxRange,
        appSettings.selectedRangeIndex
    ) { maxRange, index ->
        val steps = rangeSteps(maxRange)
        steps[index.coerceIn(steps.indices)]
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        rangeSteps(DistanceUnits.METRIC.defaultMaxRange)[
            DistanceUnits.DEFAULT_SELECTED_RANGE_INDEX
        ]
    )

    /**
     * Updates the maximum radar range.
     *
     * The selected range is stored as a position, so 25/50/75/100% is preserved
     * when the maximum range changes.
     *
     * @param range New max range value in meters or feet
     */
    fun updateMaxRange(range: Int) {
        viewModelScope.launch { appSettings.setMaxRange(range) }
    }

    /**
     * Updates the selected radar range position.
     *
     * @param index Position in the range list (0 = 25%, 3 = 100%)
     */
    fun updateSelectedRangeIndex(index: Int) {
        viewModelScope.launch { appSettings.setSelectedRangeIndex(index) }
    }

    /**
     * Updates the recording distance.
     *
     * @param distance New distance in the current unit system (meters or feet)
     */
    fun updateRecordingDistance(distance: Int) {
        viewModelScope.launch { appSettings.setRecordingDistance(distance) }
    }

    /**
     * Updates the following distance.
     *
     * @param distance New distance in the current unit system (meters or feet)
     */
    fun updateFollowingDistance(distance: Int) {
        viewModelScope.launch { appSettings.setFollowingDistance(distance) }
    }

    /**
     * Changes the distance units and resets all unit-dependent settings atomically.
     *
     * Resets max range, selected range position, recording distance, and following
     * distance to the defaults for the new unit system.
     *
     * @param distanceUnits New distance units (METRIC or IMPERIAL)
     */
    fun updateDistanceUnits(distanceUnits: DistanceUnits) {
        viewModelScope.launch {
            val maxRange = distanceUnits.defaultMaxRange
            val distance = distanceUnits.defaultDistance
            appSettings.updateUnitSettings(
                distanceUnits = distanceUnits,
                maxRange = maxRange,
                selectedRangeIndex = DistanceUnits.DEFAULT_SELECTED_RANGE_INDEX,
                recordingDistance = distance,
                followingDistance = distance
            )
        }
    }

    /**
     * Updates the adaptive following preference.
     *
     * When enabled, the following algorithm scans all remaining waypoints
     * and skips ahead to the furthest point within the configured following distance.
     * When disabled, only the single nearest waypoint is checked (strict mode).
     *
     * @param enabled True to enable adaptive following, false for strict mode
     */
    fun updateAdaptiveFollowing(enabled: Boolean) {
        viewModelScope.launch { appSettings.setAdaptiveFollowing(enabled) }
    }
}
