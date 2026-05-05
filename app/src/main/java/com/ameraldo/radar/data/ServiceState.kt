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

val Context.serviceStateDataStore: DataStore<Preferences> by preferencesDataStore(name = "service_state")

class ServiceState private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: ServiceState? = null

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

    val isRecording: Flow<Boolean> = context.serviceStateDataStore.data.map { prefs ->
        prefs[IS_RECORDING_KEY] ?: false
    }

    val recordingRouteId: Flow<Long?> = context.serviceStateDataStore.data.map { prefs ->
        prefs[RECORDING_ROUTE_ID_KEY]
    }

    val recordingRouteName: Flow<String?> = context.serviceStateDataStore.data.map { prefs ->
        prefs[RECORDING_ROUTE_NAME_KEY]
    }

    val isFollowing: Flow<Boolean> = context.serviceStateDataStore.data.map { prefs ->
        prefs[IS_FOLLOWING_KEY] ?: false
    }

    val followingRouteId: Flow<Long?> = context.serviceStateDataStore.data.map { prefs ->
        prefs[FOLLOWING_ROUTE_ID_KEY]
    }

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

    suspend fun clearRecordingState() {
        context.serviceStateDataStore.edit { prefs ->
            prefs[IS_RECORDING_KEY] = false
            prefs.remove(RECORDING_ROUTE_ID_KEY)
            prefs.remove(RECORDING_ROUTE_NAME_KEY)
        }
    }

    suspend fun clearFollowingState() {
        context.serviceStateDataStore.edit { prefs ->
            prefs[IS_FOLLOWING_KEY] = false
            prefs.remove(FOLLOWING_ROUTE_ID_KEY)
        }
    }

    suspend fun clearAll() {
        context.serviceStateDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}