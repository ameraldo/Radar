package com.ameraldo.radar.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * DataStore-based preferences manager for app settings.
 *
 * Provides reactive access to user preferences stored in Android DataStore.
 * Settings include distance units and radar range preferences.
 *
 * ## Default Values
 * - maxRange: 1000 (meters or feet)
 * - selectedRange: 500 (meters or feet)
 * - distanceUnits: METRIC
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
        /** DataStore key for max range preference */
        val MAX_RANGE_KEY = intPreferencesKey("max_range")
        /** DataStore key for selected range preference */
        val SELECTED_RANGE_KEY = floatPreferencesKey("selected_range")
        /** DataStore key for distance units preference */
        val DISTANCE_UNITS_KEY = stringPreferencesKey("distance_units")

        /** Default max range in meters or feet */
        const val DEFAULT_MAX_RANGE      = 1000
        /** Default selected range in meters or feet */
        const val DEFAULT_SELECTED_RANGE = 500f
        /** Default distance units */
        const val DEFAULT_DISTANCE_UNITS = "metric"
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
     * Updates the distance units preference.
     *
     * @param distanceUnits New distance units (METRIC or IMPERIAL)
     */
    suspend fun setDistanceUnits(distanceUnits: DistanceUnits) {
        context.dataStore.edit { prefs ->
            prefs[DISTANCE_UNITS_KEY] = distanceUnits.value
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

    companion object {
        /**
         * Converts a string value to DistanceUnits.
         *
         * @param value String value to convert
         * @return DistanceUnits or METRIC if invalid
         */
        fun fromString(value: String): DistanceUnits {
            return entries.find { it.value == value } ?: METRIC
        }
    }
}