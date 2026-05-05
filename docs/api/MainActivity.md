# MainActivity

> Main entry point for the Radar application

MainActivity is the single activity that hosts the Compose UI. It manages service binding, Picture-in-Picture mode, and notification intent handling.

## Overview

**Package**: `com.ameraldo.radar`

**Key Responsibilities**:
- Bind to `LocationService` for GPS tracking
- Manage Picture-in-Picture (PiP) mode for background radar display
- Handle notification intents (stop recording/following from notification bar)
- Orchestrate lifecycle for sensors and service connection

## Source Documentation

For detailed documentation, see the KDoc comments in:
- [`MainActivity.kt`](../../app/src/main/java/com/ameraldo/radar/MainActivity.kt)

## Key Properties

| Property | Type | Description |
|----------|------|-------------|
| `locationService` | `LocationService?` | Reference to bound service (null when not bound) |
| `serviceBound` | `Boolean` | Whether service is currently bound |
| `locationViewModel` | `LocationViewModel` | ViewModel for GPS/location state |
| `sensorViewModel` | `SensorViewModel` | ViewModel for compass sensor data |
| `uiStateViewModel` | `UIStateViewModel` | ViewModel for UI state |
| `routesViewModel` | `RouteViewModel` | ViewModel for saved routes |
| `isEnteringPiP` | `Boolean` | Flag to prevent multiple PiP entry attempts |

## Lifecycle Methods

| Method | Description |
|--------|-------------|
| `onCreate(savedInstanceState)` | Initialize activity, bind service, set Compose content |
| `onNewIntent(intent)` | Handle new intents (e.g., from notification when activity is running) |
| `onDestroy()` | Clean up lifecycle observer and service connection |
| `onUserLeaveHint()` | Enter PiP mode if recording/following is active |
| `onPictureInPictureModeChanged(isInPiP, newConfig)` | Handle PiP mode changes |

## Service Management

### ServiceConnection
- `onServiceConnected()`: Get service from binder, set in ViewModel
- `onServiceDisconnected()`: Clear service reference from ViewModel

### AppLifecycleObserver
- `onStart()`: Start sensor listening
- `onStop()`: Stop sensor listening

## Picture-in-Picture Mode

The app supports PiP mode:
1. **Trigger**: User leaves app (Home button, Recent Apps)
2. **Condition**: Recording or following is active
3. **Aspect Ratio**: 1:1 (square) for radar display
4. **Behavior**: Shows only the radar view (zoomed in)

## Notification Handling

The activity handles notification bar actions:
- `ACTION_STOP_RECORDING` → Sets pending stop action for recording
- `ACTION_STOP_FOLLOWING` → Sets pending stop action for following
- Navigates to RADAR tab to show the confirmation dialog

## Related Documentation

- [LocationService](LocationService.md) - Service being bound
- [UIStateViewModel](UIStateViewModel.md) - Manages pending stop actions
- [RadarApp](../ui/RadarApp.md) - Compose UI entry point
