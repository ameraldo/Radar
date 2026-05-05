# UIStateViewModel

> Manages UI-specific state not tied to data/business logic

UIStateViewModel handles navigation destination, Picture-in-Picture mode, and pending stop actions from the notification bar.

## Overview

**Package**: `com.ameraldo.radar.viewmodel`

**Key Responsibilities**:
- Track current navigation destination (Home, Radar, Routes, Settings)
- Manage Picture-in-Picture (PiP) mode state
- Handle pending stop actions (from notification bar)

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`UIStateViewModel.kt`](../../app/src/main/java/com/ameraldo/radar/viewmodel/UIStateViewModel.kt)

## StopAction Enum

Represents which action triggered the stop request from notification.

| Value | Description |
|-------|-------------|
| `RECORDING` | Stop recording action |
| `FOLLOWING` | Stop following action |

## State Properties

| Property | Type | Description |
|----------|------|-------------|
| `currentDestination` | `StateFlow<AppDestinations>` | Current navigation tab selection |
| `isInPiPMode` | `StateFlow<Boolean>` | Whether app is in Picture-in-Picture mode |
| `pendingStopAction` | `StateFlow<StopAction?>` | Pending stop action from notification (null if none) |

## Public Methods

| Method | Description |
|--------|-------------|
| `updateDestination(destination)` | Update current navigation destination |
| `updateIsInPiPMode(isInPiPMode)` | Update PiP mode state |
| `setPendingStopAction(action)` | Set pending stop action (triggered from notification) |
| `clearPendingStopAction()` | Clear the pending stop action after it's been handled |

## Related Documentation

- [MainActivity](../api/MainActivity.md) - Handles notification intents
- [RadarScreen](../screens/radar.md) - Observes pending stop actions
- [Navigation Destinations](../navigation/AppDestinations.md) - Destination enum
