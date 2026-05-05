# LocationState#

> Data model for current GPS location state#

LocationState holds the current GPS location information including coordinates, accuracy, and satellite data.

## Overview#

**Package**: `com.ameraldo.radar.data`

## Properties#

| Property | Type | Description |
|----------|------|-------------|
| `latitude` | `Double?` | Current latitude in degrees (null if unavailable) |
| `longitude` | `Double?` | Current longitude in degrees (null if unavailable) |
| `accuracy` | `Float?` | GPS accuracy in meters (null if unavailable) |
| `satellites` | `List<SatelliteBlip>` | List of visible GPS satellites |
| `isLoading` | `Boolean` | Whether location is being acquired |
| `error` | `LocationError` | Current error state (NONE if no error) |

## State Flow#

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

## Usage#

```kotlin
// In LocationService
_locationState.value = LocationState(
    latitude = location.latitude,
    longitude = location.longitude,
    accuracy = location.accuracy,
    satellites = blips,
    isLoading = false,
    error = LocationError.NONE
)
```

## Related Documentation#

- [LocationService](LocationService.md) - Updates location state#
- [LocationViewModel](../api/LocationViewModel.md) - Exposes state#
- [SatelliteBlip](SatelliteBlip.md) - Satellite data model#
- [LocationError](LocationError.md) - Error types#
