package com.ameraldo.radar.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore extension for app settings.
 * Creates a "settings" DataStore with preferences.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * DataStore-based preferences manager for app settings.
 *
 * Provides reactive access to user preferences stored in Android DataStore.
 * Settings include distance units, radar range, recording/following distance,
 * and adaptive following.
 *
 * All unit-dependent option lists and default values live in [DistanceUnits].
 *
 * ## Default Values
 * - maxRange: [DistanceUnits.defaultMaxRange]
 * - selectedRangeIndex: [DistanceUnits.DEFAULT_SELECTED_RANGE_INDEX] (50%)
 * - distanceUnits: METRIC
 * - adaptiveFollowing: false (disabled)
 * - recordingDistance: [DistanceUnits.defaultDistance]
 * - followingDistance: [DistanceUnits.defaultDistance]
 *
 * ## Usage
 * ```
 * val appSettings = AppSettings(context)
 * val maxRange = appSettings.maxRange // Flow<Int>
 *
 * // Update setting (must be called from coroutine)
 * appSettings.setMaxRange(2000)
 * ```
 */
class AppSettings(private val context: Context) {
    companion object {
        /** DataStore key for max range preference (in meters or feet depending on units) */
        val MAX_RANGE_KEY = intPreferencesKey("max_range")
        /** DataStore key for selected radar range position (0 = 25%, 3 = 100%) */
        val SELECTED_RANGE_INDEX_KEY = intPreferencesKey("selected_range_index")
        /** Legacy absolute selected range key, removed on the next write */
        private val LEGACY_SELECTED_RANGE_KEY = floatPreferencesKey("selected_range")
        /** DataStore key for distance units preference (metric/imperial) */
        val DISTANCE_UNITS_KEY = stringPreferencesKey("distance_units")
        /** DataStore key for adaptive following preference (enables skipping ahead past missed waypoints) */
        val ADAPTIVE_FOLLOWING_KEY = booleanPreferencesKey("adaptive_following")
        val RECORDING_DISTANCE_KEY = intPreferencesKey("recording_distance")
        val FOLLOWING_DISTANCE_KEY = intPreferencesKey("following_distance")

        /** Default distance units: metric */
        const val DEFAULT_DISTANCE_UNITS = "metric"
        /** Default adaptive following: disabled (strict single-point check) */
        const val DEFAULT_ADAPTIVE_FOLLOWING = false
    }

    /**
     * Returns the stored distance for [key], normalized to the current unit system.
     *
     * Legacy values that are not part of [DistanceUnits.distanceOptions] (for example
     * a metric value left over after switching to imperial) fall back to the default
     * for the current units.
     */
    private fun normalizedDistance(
        prefs: Preferences,
        key: Preferences.Key<Int>
    ): Int {
        val units = DistanceUnits.fromString(prefs[DISTANCE_UNITS_KEY] ?: DEFAULT_DISTANCE_UNITS)
        return units.normalizeDistance(prefs[key])
    }

    /** Flow of max radar range (in meters or feet depending on units) */
    val maxRange: Flow<Int> = context.dataStore.data.map { prefs ->
        val units = DistanceUnits.fromString(prefs[DISTANCE_UNITS_KEY] ?: DEFAULT_DISTANCE_UNITS)
        units.normalizeMaxRange(prefs[MAX_RANGE_KEY])
    }
    /** Flow of the selected radar range position (0 = 25%, 3 = 100%) */
    val selectedRangeIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_RANGE_INDEX_KEY]
            ?.coerceIn(0, DistanceUnits.RANGE_STEP_COUNT - 1)
            ?: DistanceUnits.DEFAULT_SELECTED_RANGE_INDEX
    }
    /** Flow of distance units preference */
    val distanceUnits: Flow<DistanceUnits> = context.dataStore.data.map { prefs ->
        DistanceUnits.fromString(prefs[DISTANCE_UNITS_KEY] ?: DEFAULT_DISTANCE_UNITS)
    }
    /** Flow of adaptive following preference (scans all points vs single-point check) */
    val adaptiveFollowing: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ADAPTIVE_FOLLOWING_KEY] ?: DEFAULT_ADAPTIVE_FOLLOWING
    }
    /** Flow of minimum movement (in meters or feet) before a new point is recorded */
    val recordingDistance: Flow<Int> = context.dataStore.data.map { prefs ->
        normalizedDistance(prefs, RECORDING_DISTANCE_KEY)
    }
    /** Flow of proximity (in meters or feet) required to mark a followed waypoint as reached */
    val followingDistance: Flow<Int> = context.dataStore.data.map { prefs ->
        normalizedDistance(prefs, FOLLOWING_DISTANCE_KEY)
    }

    /**
     * Updates the max radar range preference.
     *
     * @param range New max range value
     */
    suspend fun setMaxRange(range: Int) {
        context.dataStore.edit { prefs ->
            prefs[MAX_RANGE_KEY] = range
        }
    }
    /**
     * Updates the selected radar range position.
     *
     * @param index Position in the range list (0 = 25%, 3 = 100%)
     */
    suspend fun setSelectedRangeIndex(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_RANGE_INDEX_KEY] =
                index.coerceIn(0, DistanceUnits.RANGE_STEP_COUNT - 1)
            prefs.remove(LEGACY_SELECTED_RANGE_KEY)
        }
    }
    /**
     * Updates the distance units preference only.
     *
     * Use [updateUnitSettings] when the dependent range and distance settings
     * should be reset atomically for the new unit system.
     *
     * @param distanceUnits New distance units (METRIC or IMPERIAL)
     */
    suspend fun setDistanceUnits(distanceUnits: DistanceUnits) {
        context.dataStore.edit { prefs ->
            prefs[DISTANCE_UNITS_KEY] = distanceUnits.value
        }
    }
    /**
     * Atomically updates the unit system and all unit-dependent settings.
     *
     * Writing everything in one DataStore transaction avoids intermediate
     * emissions where, for example, imperial units are paired with metric values.
     *
     * @param distanceUnits New distance units (METRIC or IMPERIAL)
     * @param maxRange Maximum radar range in the new units
     * @param selectedRangeIndex Selected radar range position (0 = 25%, 3 = 100%)
     * @param recordingDistance Recording distance in the new units
     * @param followingDistance Following distance in the new units
     */
    suspend fun updateUnitSettings(
        distanceUnits: DistanceUnits,
        maxRange: Int,
        selectedRangeIndex: Int,
        recordingDistance: Int,
        followingDistance: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[DISTANCE_UNITS_KEY] = distanceUnits.value
            prefs[MAX_RANGE_KEY] = maxRange
            prefs[SELECTED_RANGE_INDEX_KEY] =
                selectedRangeIndex.coerceIn(0, DistanceUnits.RANGE_STEP_COUNT - 1)
            prefs[RECORDING_DISTANCE_KEY] = recordingDistance
            prefs[FOLLOWING_DISTANCE_KEY] = followingDistance
            prefs.remove(LEGACY_SELECTED_RANGE_KEY)
        }
    }
    /**
     * Updates the adaptive following preference.
     *
     * @param enabled True to enable adaptive following (skip ahead), false for strict single-point check
     */
    suspend fun setAdaptiveFollowing(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ADAPTIVE_FOLLOWING_KEY] = enabled
        }
    }
    /**
     * Updates the recording distance preference.
     *
     * @param distance New distance in the current unit system (meters or feet)
     */
    suspend fun setRecordingDistance(distance: Int) {
        context.dataStore.edit { prefs ->
            prefs[RECORDING_DISTANCE_KEY] = distance
        }
    }
    /**
     * Updates the following distance preference.
     *
     * @param distance New distance in the current unit system (meters or feet)
     */
    suspend fun setFollowingDistance(distance: Int) {
        context.dataStore.edit { prefs ->
            prefs[FOLLOWING_DISTANCE_KEY] = distance
        }
    }
}
