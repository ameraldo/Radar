package com.ameraldo.radar.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ameraldo.radar.data.AppDatabase
import com.ameraldo.radar.data.LocationError
import com.ameraldo.radar.data.LocationState
import com.ameraldo.radar.data.RecordedPointEntity
import com.ameraldo.radar.service.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    // Cached default flows
    private val _defaultLocationState = MutableStateFlow(LocationState())
    private val _defaultFalse = MutableStateFlow(false)
    private val _defaultNullLong = MutableStateFlow<Long?>(null)
    private val _defaultNullString = MutableStateFlow<String?>(null)
    private val _defaultEmptyPoints = MutableStateFlow<List<RecordedPointEntity>>(emptyList())

    @SuppressLint("StaticFieldLeak") // Lifecycle is properly managed, warning is a false positive
    private var _locationService: LocationService? = null

    private val database = AppDatabase.getInstance(getApplication())
    private val routeDao = database.routeDao()

    val locationState: StateFlow<LocationState>
        get() = _locationService?.locationState ?: _defaultLocationState
    val isRecording: StateFlow<Boolean>
        get() = _locationService?.isRecording ?: _defaultFalse
    val isFollowing: StateFlow<Boolean>
        get() = _locationService?.isFollowing ?: _defaultFalse
    val currentRouteId: StateFlow<Long?>
        get() = _locationService?.currentRouteId ?: _defaultNullLong
    val currentRouteName: StateFlow<String?>
        get() = _locationService?.currentRouteName ?: _defaultNullString
    val currentRoutePoints: StateFlow<List<RecordedPointEntity>>
        get() = _locationService?.currentRoutePoints ?: _defaultEmptyPoints
    val followingRemainingPoints: StateFlow<List<RecordedPointEntity>>
        get() = _locationService?.followingRemainingPoints ?: _defaultEmptyPoints

    fun setLocationService(service: LocationService) {
        _locationService = service
    }
    fun clearLocationService() {
        _locationService = null
    }

    private val context: Context
        get() = getApplication<Application>()

    fun startLocationUpdates() {
        val intent = LocationService.requestStartLocationUpdates(context)
        context.startService(intent)
    }

    fun startRecording() {
        val intent = LocationService.createStartRecordingIntent(context, "")
        context.startForegroundService(intent)
    }

    fun stopRecording() {
        _locationService?.stopRecordingViaBinder()
            ?: run {
                // Fallback: service not bound (shouldn't happen when activity is active)
                val intent = LocationService.createStopRecordingIntent(context)
                context.startService(intent)
            }
    }

    fun saveRoute(newName: String?) {
        if (newName != null) {
            viewModelScope.launch {
                val routeId = currentRouteId.first() ?: return@launch
                val route = routeDao.getRouteById(routeId) ?: return@launch
                routeDao.updateRoute(route.copy(name = newName))
            }
        }
    }

    fun deleteRoute() {
        _locationService?.deleteCurrentRoute()
    }

    fun startFollowing(routeId: Long) {
        val intent = LocationService.createStartFollowingIntent(context, routeId)
        context.startForegroundService(intent)
    }

    fun stopFollowing() {
        _locationService?.stopFollowingViaBinder()
            ?: run {
                // Fallback: service not bound (shouldn't happen when activity is active)
                val intent = LocationService.createStopFollowingIntent(context)
                context.startService(intent)
            }
    }

    fun onPermissionDenied() {
        _locationService?.setPermissionError(LocationError.PermissionDenied())
    }

    fun onPermissionPermanentlyDenied() {
        _locationService?.setPermissionError(LocationError.PermissionDenied(permanentlyDenied = true))
    }
}