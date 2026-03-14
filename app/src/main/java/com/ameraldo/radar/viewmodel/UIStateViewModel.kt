package com.ameraldo.radar.viewmodel

import androidx.lifecycle.ViewModel
import com.ameraldo.radar.navigation.AppDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UIStateViewModel : ViewModel() {
    private val _currentDestination = MutableStateFlow(AppDestinations.HOME)
    private val _isInPiPMode = MutableStateFlow(false)

    val currentDestination: StateFlow<AppDestinations> = _currentDestination.asStateFlow()
    val isInPiPMode: StateFlow<Boolean> = _isInPiPMode.asStateFlow()

    fun updateDestination(destination: AppDestinations) {
        _currentDestination.value = destination
    }

    fun updateIsInPiPMode(isInPiPMode: Boolean) {
        _isInPiPMode.value = isInPiPMode
    }
}