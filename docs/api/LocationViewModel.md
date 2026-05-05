# LocationViewModel

> Manages location state and bridges Service and UI

LocationViewModel acts as a bridge between the LocationService and the UI. It delegates to the service for state and exposes data for the UI via StateFlow.

## Overview

**Package**: `com.ameraldo.radar.viewmodel`

**Key Responsibilities**:
- Delegate location state management to LocationService
- Handle route recording (start/stop via service)
- Handle route following (start/stop via service)
- Process permission errors and set in service

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
| `currentRoutePoints` | `StateFlow<List<RecordedPointEntity>>` | Points for current route (Room-backed) |
| `followingRemainingPoints` | `StateFlow<List<RecordedPointEntity>>` | Points yet to be reached (in-memory) |

## Public Methods

### Service Management
- `setLocationService(service)` - Set service reference after binding
- `clearLocationService()` - Clear service reference on unbind

### Location Updates
- `startLocationUpdates()` - Request location updates via service

### Permissions
- `onPermissionDenied()` - Handle denied permission
- `onPermissionPermanentlyDenied()` - Handle permanently denied permission

### Recording
- `startRecording()` - Start recording (creates intent with empty name)
- `stopRecording()` - Stop recording (tries binder, falls back to intent)
- `saveRoute(newName)` - Save route with optional new name
- `deleteRoute()` - Discard current route (deletes from service)

### Following
- `startFollowing(routeId)` - Start following a saved route
- `stopFollowing()` - Stop following (tries binder, falls back to intent)

## Related Documentation

- [LocationService](LocationService.md) - Service layer
- [Data Flow](../architecture/data-flow.md) - How data flows through the app
- [UIStateViewModel](UIStateViewModel.md) - Pending stop actions
