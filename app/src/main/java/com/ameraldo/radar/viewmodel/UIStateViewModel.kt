package com.ameraldo.radar.viewmodel

import androidx.lifecycle.ViewModel
import com.ameraldo.radar.navigation.AppDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class StopAction {
    RECORDING,
    FOLLOWING
}

class UIStateViewModel : ViewModel() {
    private val _currentDestination = MutableStateFlow(AppDestinations.HOME)
    private val _isInPiPMode = MutableStateFlow(false)
    private val _pendingStopAction = MutableStateFlow<StopAction?>(null)

    val currentDestination: StateFlow<AppDestinations> = _currentDestination.asStateFlow()
    val isInPiPMode: StateFlow<Boolean> = _isInPiPMode.asStateFlow()
    val pendingStopAction: StateFlow<StopAction?> = _pendingStopAction.asStateFlow()

    fun updateDestination(destination: AppDestinations) {
        _currentDestination.value = destination
    }
    fun updateIsInPiPMode(isInPiPMode: Boolean) {
        _isInPiPMode.value = isInPiPMode
    }
    fun setPendingStopAction(action: StopAction?) {
        _pendingStopAction.value = action
    }
    fun clearPendingStopAction() {
        _pendingStopAction.value = null
    }
}