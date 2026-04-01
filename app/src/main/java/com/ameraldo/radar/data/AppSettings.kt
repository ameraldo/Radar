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
class AppSettings(private val context: Context) {
    companion object {
        val MAX_RANGE_KEY = intPreferencesKey("max_range")
        val SELECTED_RANGE_KEY = floatPreferencesKey("selected_range")
        val DISTANCE_UNITS_KEY = stringPreferencesKey("distance_units")

        const val DEFAULT_MAX_RANGE      = 1000      // meters | feet
        const val DEFAULT_SELECTED_RANGE = 500f      // meters | feet
        const val DEFAULT_DISTANCE_UNITS = "metric"  // metric | imperial
    }
    val maxRange: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[MAX_RANGE_KEY] ?: DEFAULT_MAX_RANGE
    }
    val selectedRange: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_RANGE_KEY] ?: DEFAULT_SELECTED_RANGE
    }
    val distanceUnits: Flow<DistanceUnits> = context.dataStore.data.map { prefs ->
        DistanceUnits.fromString(prefs[DISTANCE_UNITS_KEY] ?: DEFAULT_DISTANCE_UNITS)
    }
    suspend fun setMaxRange(range: Int) {
        context.dataStore.edit { prefs ->
            prefs[MAX_RANGE_KEY] = range
        }
    }
    suspend fun setSelectedRange(range: Float) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_RANGE_KEY] = range
        }
    }
    suspend fun setDistanceUnits(distanceUnits: DistanceUnits) {
        context.dataStore.edit { prefs ->
            prefs[DISTANCE_UNITS_KEY] = distanceUnits.value
        }
    }
}

enum class DistanceUnits(val value: String) {
    METRIC("metric"),
    IMPERIAL("imperial");

    companion object {
        fun fromString(value: String): DistanceUnits {
            return entries.find { it.value == value } ?: METRIC
        }
    }
}