package com.ameraldo.radar.viewmodel

import androidx.lifecycle.ViewModel
import com.ameraldo.radar.navigation.AppDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel that manages UI-specific state not tied to data/business logic.
 *
 * Handles:
 * - Current navigation destination ([currentDestination])
 * - Picture-in-Picture mode state ([isInPiPMode])
 * - Pending stop actions from notification bar ([pendingStopAction])
 */
class UIStateViewModel : ViewModel() {
    /**
     * Represents which action triggered the stop request from notification.
     *
     * @property RECORDING Stop recording action
     * @property FOLLOWING Stop following action
     */
    enum class StopAction {
        RECORDING,
        FOLLOWING
    }

    /** Current navigation destination (which tab is selected) */
    private val _currentDestination = MutableStateFlow(AppDestinations.HOME)
    val currentDestination: StateFlow<AppDestinations> = _currentDestination.asStateFlow()

    /** Whether the app is in Picture-in-Picture mode */
    private val _isInPiPMode = MutableStateFlow(false)
    val isInPiPMode: StateFlow<Boolean> = _isInPiPMode.asStateFlow()

    /** Pending stop action from notification bar (triggers dialog in RadarScreen) */
    private val _pendingStopAction = MutableStateFlow<StopAction?>(null)
    val pendingStopAction: StateFlow<StopAction?> = _pendingStopAction.asStateFlow()

    /** Updates the current navigation destination (tab selection) */
    fun updateDestination(destination: AppDestinations) {
        _currentDestination.value = destination
    }

    /** Updates Picture-in-Picture mode state */
    fun updateIsInPiPMode(isInPiPMode: Boolean) {
        _isInPiPMode.value = isInPiPMode
    }

    /** Sets a pending stop action (triggered from notification bar) */
    fun setPendingStopAction(action: StopAction?) {
        _pendingStopAction.value = action
    }

    /** Clears the pending stop action after it's been handled */
    fun clearPendingStopAction() {
        _pendingStopAction.value = null
    }
}