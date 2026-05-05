# LocationService

> Foreground service for GPS tracking and route recording

LocationService is the **single source of truth** for location and recording functionality. It runs as a Foreground Service to ensure reliable GPS tracking even when the screen is locked or the app is in the background.

## Overview

**Package**: `com.ameraldo.radar.service`

**Key Responsibilities**:
- Manage GPS location updates via Google Play Services Location API
- Handle route recording (capturing GPS points)
- Handle route following (navigating back along saved routes)
- Run as Foreground Service with notification
- Track GPS satellite information

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`LocationService.kt`](../../app/src/main/java/com/ameraldo/radar/service/LocationService.kt)

## State Properties

| Property | Type | Description |
|----------|------|-------------|
| `locationState` | `StateFlow<LocationState>` | Current GPS location, accuracy, satellites |
| `isRecording` | `StateFlow<Boolean>` | Whether route is being recorded |
| `isFollowing` | `StateFlow<Boolean>` | Whether following a saved route |
| `currentRouteId` | `StateFlow<Long?>` | ID of current route |
| `currentRouteName` | `StateFlow<String?>` | Name of current route |
| `currentRoutePoints` | `StateFlow<List<RecordedPointEntity>>` | Points for current route |
| `followingRemainingPoints` | `StateFlow<List<RecordedPointEntity>>` | Points yet to be reached |

## Actions (Companion Object)

| Action | Description |
|--------|-------------|
| `ACTION_START_RECORDING` | Start recording a new route |
| `ACTION_STOP_RECORDING` | Stop recording the current route |
| `ACTION_START_FOLLOWING` | Start following a saved route |
| `ACTION_STOP_FOLLOWING` | Stop following the current route |

## Usage from MainActivity

```kotlin
// Start recording
val intent = LocationService.createStartRecordingIntent(context, routeName)
startForegroundService(intent)

// Stop recording
val intent = LocationService.createStopRecordingIntent(context)
startService(intent)

// Start following
val intent = LocationService.createStartFollowingIntent(context, routeId)
startForegroundService(intent)

// Stop following
val intent = LocationService.createStopFollowingIntent(context)
startService(intent)
```

## Related Documentation

- [Data Flow](../architecture/data-flow.md) - How location flows through the app
- [Overview](../architecture/overview.md) - System architecture