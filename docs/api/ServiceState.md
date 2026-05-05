# ServiceState

> DataStore-based persistence for LocationService state

ServiceState persists service state (recording/following) across process restarts using DataStore Preferences.

## Overview

**Package**: `com.ameraldo.radar.data`

## Purpose

The LocationService can be killed by the system (low memory, etc.). ServiceState saves the current state so the service can restore:
- Recording state (isRecording, routeId, routeName)
- Following state (isFollowing, routeId)

## Key Properties

| Property | Type | Description |
|----------|------|-------------|
| `isRecording` | `Flow<Boolean>` | Whether recording was active |
| `recordingRouteId` | `Flow<Long?>` | ID of route being recorded |
| `recordingRouteName` | `Flow<String?>` | Name of route being recorded |
| `isFollowing` | `Flow<Boolean>` | Whether following was active |
| `followingRouteId` | `Flow<Long?>` | ID of route being followed |

## Key Methods

| Method | Description |
|--------|-------------|
| `setRecordingState(isRecording, routeId, routeName)` | Save recording state |
| `setFollowingState(isFollowing, routeId)` | Save following state |
| `clearRecordingState()` | Clear recording state |
| `clearFollowingState()` | Clear following state |
| `clearAll()` | Clear all state |

## DataStore Keys

| Key | Type | Default |
|-----|------|---------|
| `is_recording` | Boolean | false |
| `recording_route_id` | Long | null |
| `recording_route_name` | String | null |
| `is_following` | Boolean | false |
| `following_route_id` | Long | null |

## Usage in LocationService

```kotlin
// Restore state on service restart
private suspend fun restoreStateIfNeeded() {
    val isRecording = serviceState.isRecording.first()
    if (isRecording) {
        // Restore recording state...
    }
}

// Save state when starting recording
serviceState.setRecordingState(true, routeId, routeName)
```

## Related Documentation

- [LocationService](LocationService.md) - Uses ServiceState for persistence
- [Overview](../architecture/overview.md) - Architecture integration
