package com.ameraldo.radar.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
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
 * Settings include distance units and radar range preferences.
 *
 * ## Default Values
 * - maxRange: 1000 meters (metric) or 5280 feet (imperial)
 * - selectedRange: 500 meters (metric) or 2640 feet (imperial)
 * - distanceUnits: METRIC
 * - adaptiveFollowing: false (disabled)
 * - recordingDistance: 5 meters (metric) or 30 feet (imperial)
 * - followingDistance: 5 meters (metric) or 30 feet (imperial)
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
        /** DataStore key for selected range preference (displayed on radar) */
        val SELECTED_RANGE_KEY = floatPreferencesKey("selected_range")
        /** DataStore key for distance units preference (metric/imperial) */
        val DISTANCE_UNITS_KEY = stringPreferencesKey("distance_units")
        /** DataStore key for adaptive following preference (enables skipping ahead past missed waypoints) */
        val ADAPTIVE_FOLLOWING_KEY = booleanPreferencesKey("adaptive_following")
        val RECORDING_DISTANCE_KEY = intPreferencesKey("recording_distance")
        val FOLLOWING_DISTANCE_KEY = intPreferencesKey("following_distance")

        /** Default max range: 1000 meters or 1000 feet */
        const val DEFAULT_MAX_RANGE = 1000
        /** Default selected range: 500 meters or 500 feet */
        const val DEFAULT_SELECTED_RANGE = 500f
        /** Default distance units: metric */
        const val DEFAULT_DISTANCE_UNITS = "metric"
        /** Default adaptive following: disabled (strict single-point check) */
        const val DEFAULT_ADAPTIVE_FOLLOWING = false

        /** Available recording/following distances in meters (metric) */
        val METRIC_DISTANCES = listOf(1, 5, 10, 25, 50, 100)
        /** Available recording/following distances in feet (imperial) */
        val IMPERIAL_DISTANCES = listOf(3, 30, 80, 160, 320)
        /** Default recording/following distance in meters (metric) */
        const val DEFAULT_METRIC_DISTANCE = 5
        /** Default recording/following distance in feet (imperial) */
        const val DEFAULT_IMPERIAL_DISTANCE = 30

        /** Default max radar range in meters (metric) */
        const val DEFAULT_METRIC_MAX_RANGE = 1000
        /** Default max radar range in feet (imperial) */
        const val DEFAULT_IMPERIAL_MAX_RANGE = 5280
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
        prefs[MAX_RANGE_KEY] ?: DEFAULT_MAX_RANGE
    }
    /** Flow of selected radar range for display */
    val selectedRange: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_RANGE_KEY] ?: DEFAULT_SELECTED_RANGE
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
     * Updates the selected radar range preference.
     *
     * @param range New selected range value
     */
    suspend fun setSelectedRange(range: Float) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_RANGE_KEY] = range
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
     * @param selectedRange Selected radar range in the new units
     * @param recordingDistance Recording distance in the new units
     * @param followingDistance Following distance in the new units
     */
    suspend fun updateUnitSettings(
        distanceUnits: DistanceUnits,
        maxRange: Int,
        selectedRange: Float,
        recordingDistance: Int,
        followingDistance: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[DISTANCE_UNITS_KEY] = distanceUnits.value
            prefs[MAX_RANGE_KEY] = maxRange
            prefs[SELECTED_RANGE_KEY] = selectedRange
            prefs[RECORDING_DISTANCE_KEY] = recordingDistance
            prefs[FOLLOWING_DISTANCE_KEY] = followingDistance
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

/**
 * Distance units enumeration.
 *
 * @property value String representation for storage
 */
enum class DistanceUnits(val value: String) {
    /** Metric units: kilometers, meters */
    METRIC("metric"),
    /** Imperial units: miles, feet */
    IMPERIAL("imperial");

    /**
     * Returns the selectable recording/following distances for this unit system.
     *
     * @return Metric values in meters or imperial values in feet
     */
    fun distanceOptions(): List<Int> = when (this) {
        METRIC -> AppSettings.METRIC_DISTANCES
        IMPERIAL -> AppSettings.IMPERIAL_DISTANCES
    }

    /**
     * Returns the default recording/following distance for this unit system.
     *
     * @return Default distance in meters (metric) or feet (imperial)
     */
    fun defaultDistance(): Int = when (this) {
        METRIC -> AppSettings.DEFAULT_METRIC_DISTANCE
        IMPERIAL -> AppSettings.DEFAULT_IMPERIAL_DISTANCE
    }

    /**
     * Normalizes a stored distance to a valid option for this unit system.
     *
     * Missing or legacy values (for example a metric value after switching to
     * imperial) fall back to [defaultDistance].
     *
     * @param stored Distance read from DataStore, or null if unset
     * @return A value from [distanceOptions]
     */
    fun normalizeDistance(stored: Int?): Int =
        if (stored != null && stored in distanceOptions()) stored else defaultDistance()

    companion object {
        /**
         * Converts a string value to DistanceUnits.
         * Returns [METRIC] if the string doesn't match any known value.
         *
         * @param value String value from DataStore (e.g., "metric", "imperial")
         * @return DistanceUnits enum or METRIC as default fallback
         */
        fun fromString(value: String): DistanceUnits {
            return entries.find { it.value == value } ?: METRIC
        }
    }
}