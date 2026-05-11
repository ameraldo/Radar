package com.ameraldo.radar.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ameraldo.radar.data.AppDatabase
import com.ameraldo.radar.data.RouteEntity
import com.ameraldo.radar.utils.generateGpx
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the list of saved routes and GPX export.
 *
 * Provides a reactive list of all saved routes from the database and
 * functionality to export routes as GPX 1.1 files via [exportRoute].
 *
 * ## State Properties
 * - [routes]: All saved routes, sorted by start time (newest first)
 *
 * ## Usage
 * To delete a route:
 * ```
 * routeViewModel.deleteRoute(routeId)
 * ```
 * To export a route as GPX:
 * ```
 * routeViewModel.exportRoute(routeId, uri)
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

    /**
     * Exports a route as a GPX 1.1 file and writes it to the given URI.
     *
     * Loads the route and its recorded points from Room, generates GPX XML
     * via [GpxUtils.generateGpx], and writes the result to the URI
     * (typically obtained from a Storage Access Framework file picker).
     *
     * @param routeId ID of the route to export
     * @param uri Content URI where the GPX file will be written
     */
    fun exportRoute(routeId: Long, uri: Uri) {
        viewModelScope.launch {
            val route = routeDao.getRouteById(routeId) ?: return@launch
            val points = routeDao.getPointsForRoute(routeId).first()
            val gpx = generateGpx(route, points)
            getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                it.write(gpx.toByteArray(Charsets.UTF_8))
            }
        }
    }
}