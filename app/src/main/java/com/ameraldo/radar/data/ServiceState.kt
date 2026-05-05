package com.ameraldo.radar.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore extension for persisting service state across process restarts.
 * Creates a "service_state" DataStore with preferences.
 */
val Context.serviceStateDataStore: DataStore<Preferences> by preferencesDataStore(name = "service_state")

/**
 * Persists service state across process restarts using DataStore.
 *
 * Stores recording/following state, route IDs, and route names
 * so the [LocationService] can restore its state after being killed by the system.
 *
 * @property isRecording Flow of recording state (true if actively recording)
 * @property recordingRouteId Flow of current recording route ID (null if not recording)
 * @property recordingRouteName Flow of current recording route name (null if not recording)
 * @property isFollowing Flow of following state (true if actively following)
 * @property followingRouteId Flow of current following route ID (null if not following)
 */
class ServiceState private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: ServiceState? = null

        /**
         * Gets the singleton ServiceState instance.
         * Uses [@Volatile] for thread safety across concurrent access.
         *
         * @param context Application context (uses applicationContext to avoid leaks)
         * @return The singleton ServiceState instance
         */
        fun getInstance(context: Context): ServiceState {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ServiceState(context.applicationContext).also { INSTANCE = it }
            }
        }

        val IS_RECORDING_KEY = booleanPreferencesKey("is_recording")
        val RECORDING_ROUTE_ID_KEY = longPreferencesKey("recording_route_id")
        val RECORDING_ROUTE_NAME_KEY = stringPreferencesKey("recording_route_name")
        val IS_FOLLOWING_KEY = booleanPreferencesKey("is_following")
        val FOLLOWING_ROUTE_ID_KEY = longPreferencesKey("following_route_id")
    }

    /** Flow of recording state, defaults to false if not set */
    val isRecording: Flow<Boolean> = context.serviceStateDataStore.data.map { prefs ->
        prefs[IS_RECORDING_KEY] ?: false
    }

    /** Flow of current recording route ID, null if not recording */
    val recordingRouteId: Flow<Long?> = context.serviceStateDataStore.data.map { prefs ->
        prefs[RECORDING_ROUTE_ID_KEY]
    }

    /** Flow of current recording route name, null if not recording */
    val recordingRouteName: Flow<String?> = context.serviceStateDataStore.data.map { prefs ->
        prefs[RECORDING_ROUTE_NAME_KEY]
    }

    /** Flow of following state, defaults to false if not set */
    val isFollowing: Flow<Boolean> = context.serviceStateDataStore.data.map { prefs ->
        prefs[IS_FOLLOWING_KEY] ?: false
    }

    /** Flow of current following route ID, null if not following */
    val followingRouteId: Flow<Long?> = context.serviceStateDataStore.data.map { prefs ->
        prefs[FOLLOWING_ROUTE_ID_KEY]
    }

    /**
     * Sets the recording state and associated route info.
     *
     * @param isRecording Whether recording is active
     * @param routeId ID of the route being recorded (null if not recording)
     * @param routeName Name of the route (null if not recording)
     */
    suspend fun setRecordingState(isRecording: Boolean, routeId: Long?, routeName: String?) {
        context.serviceStateDataStore.edit { prefs ->
            prefs[IS_RECORDING_KEY] = isRecording
            if (isRecording && routeId != null) {
                prefs[RECORDING_ROUTE_ID_KEY] = routeId
                routeName?.let { prefs[RECORDING_ROUTE_NAME_KEY] = it }
            } else {
                prefs.remove(RECORDING_ROUTE_ID_KEY)
                prefs.remove(RECORDING_ROUTE_NAME_KEY)
            }
        }
    }

    /**
     * Sets the following state and associated route ID.
     *
     * @param isFollowing Whether following is active
     * @param routeId ID of the route being followed (null if not following)
     */
    suspend fun setFollowingState(isFollowing: Boolean, routeId: Long?) {
        context.serviceStateDataStore.edit { prefs ->
            prefs[IS_FOLLOWING_KEY] = isFollowing
            if (isFollowing && routeId != null) {
                prefs[FOLLOWING_ROUTE_ID_KEY] = routeId
            } else {
                prefs.remove(FOLLOWING_ROUTE_ID_KEY)
            }
        }
    }

    /** Clears recording state (sets isRecording to false, removes route ID and name) */
    suspend fun clearRecordingState() {
        context.serviceStateDataStore.edit { prefs ->
            prefs[IS_RECORDING_KEY] = false
            prefs.remove(RECORDING_ROUTE_ID_KEY)
            prefs.remove(RECORDING_ROUTE_NAME_KEY)
        }
    }

    /** Clears following state (sets isFollowing to false, removes route ID) */
    suspend fun clearFollowingState() {
        context.serviceStateDataStore.edit { prefs ->
            prefs[IS_FOLLOWING_KEY] = false
            prefs.remove(FOLLOWING_ROUTE_ID_KEY)
        }
    }

    /** Clears all service state (for debugging or reset purposes) */
    suspend fun clearAll() {
        context.serviceStateDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}