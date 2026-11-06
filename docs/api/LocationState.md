# LocationState

> Data model for current GPS location state

LocationState holds the current GPS location information including coordinates, accuracy, and satellite data.

## Overview

**Package**: `com.ameraldo.radar.data`

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `latitude` | `Double?` | Current latitude in degrees (null if unavailable) |
| `longitude` | `Double?` | Current longitude in degrees (null if unavailable) |
| `accuracy` | `Float?` | GPS accuracy in meters (null if unavailable) |
| `satellites` | `List<SatelliteBlip>` | List of visible GPS satellites |
| `isLoading` | `Boolean` | Whether location is being acquired |
| `error` | `LocationError` | Current error state (NONE if no error) |

## State Flow

```
GPS Update
    │
    ▼
LocationService._locationState (MutableStateFlow)
    │
    ▼
LocationViewModel.locationState (StateFlow)
    │
    ▼
UI Screens observe and display
```

## Usage

```kotlin
// In LocationService — location updates modify the existing state with copy()
_locationState.value = _locationState.value.copy(
    latitude = location.latitude,
    longitude = location.longitude,
    accuracy = location.accuracy,
    isLoading = false
)

// In LocationService — satellite updates replace only the satellite list
_locationState.value = _locationState.value.copy(satellites = blips)
```

## Related Documentation

- [LocationService](LocationService.md) - Updates location state
- [LocationViewModel](../api/LocationViewModel.md) - Exposes state
- [SatelliteBlip](SatelliteBlip.md) - Satellite data model
- [LocationError](LocationError.md) - Error types
