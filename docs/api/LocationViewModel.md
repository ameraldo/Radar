# LocationViewModel

> Manages location state and bridges Service and UI

LocationViewModel acts as a bridge between the LocationService and the UI. It manages location state, handles recording/following logic, and exposes data for the UI via StateFlow.

## Overview

**Package**: `com.ameraldo.radar.viewmodel`

**Key Responsibilities**:
- Manage GPS location state
- Handle route recording (start/stop)
- Handle route following (start/stop)
- Process satellite information
- Coordinate with MainActivity for service lifecycle

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`LocationViewModel.kt`](../../app/src/main/java/com/ameraldo/radar/viewmodel/LocationViewModel.kt)

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

## Public Methods

### Location Updates
- `startLocationUpdates()` - Begin receiving location updates
- `stopLocationUpdates()` - Stop receiving location updates

### Permissions
- `onPermissionDenied()` - Handle denied permission
- `onPermissionPermanentlyDenied()` - Handle permanently denied permission

### Recording
- `startRecording()` - Begin recording a new route
- `stopRecording()` - Stop recording and save
- `saveRoute(newName)` - Save route with optional new name
- `deleteRoute()` - Discard current route

### Following
- `startFollowing(routeId, onLoaded)` - Start following a saved route
- `stopFollowing()` - Stop following

## Related Documentation

- [LocationService](LocationService.md) - Service layer
- [Data Flow](../architecture/data-flow.md) - How data flows through the app