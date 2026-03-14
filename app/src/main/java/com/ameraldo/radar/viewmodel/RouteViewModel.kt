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

class RouteViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val routeDao = database.routeDao()

    val routes: StateFlow<List<RouteEntity>> = routeDao.getAllRoutes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteRoute(routeId: Long) {
        viewModelScope.launch {
            routeDao.deleteRoute(routeId)
        }
    }
}