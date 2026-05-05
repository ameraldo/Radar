package com.ameraldo.radar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ameraldo.radar.data.AppDatabase
import com.ameraldo.radar.data.RouteEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the list of saved routes.
 *
 * Provides a reactive list of all saved routes from the database.
 *
 * ## State Properties
 * - [routes]: All saved routes, sorted by start time (newest first)
 *
 * ## Usage
 * To delete a route:
 * ```
 * routeViewModel.deleteRoute(routeId)
 * ```
 */
class RouteViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val routeDao = database.routeDao()

    /**
     * Flow of all saved routes, sorted by start time (newest first).
     */
    val routes: StateFlow<List<RouteEntity>> = routeDao.getAllRoutes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Deletes a route and all its associated points (cascade delete).
     *
     * @param routeId ID of the route to delete
     */
    fun deleteRoute(routeId: Long) {
        viewModelScope.launch {
            // Delete from Room (async, doesn't block UI)
            routeDao.deleteRoute(routeId)
        }
    }
}