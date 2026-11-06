# LocationError

> Sealed class representing location-related errors

LocationError defines the types of errors that can occur with location services.

## Overview

**Package**: `com.ameraldo.radar.data`

## Error Types

| Type | Status | Description |
|------|--------|-------------|
| `NONE` | Implemented | No error state |
| `PermissionDenied` | Implemented | Location permission denied by user |
| `ServiceUnavailable` | TODO / planned | Location services disabled or unavailable |
| `OtherError` | TODO / planned | Other unexpected errors |

`ServiceUnavailable` and `OtherError` are declared and rendered by
[CurrentLocationCard](CurrentLocationCard.md), but they are not constructed anywhere
yet. They are marked with TODO comments in the code.

## Properties

### PermissionDenied

| Property | Type | Description |
|----------|------|-------------|
| `permanentlyDenied` | `Boolean` | True if user checked "Don't ask again" |

### ServiceUnavailable

| Property | Type | Description |
|----------|------|-------------|
| `message` | `String` | Description of the unavailability |

### OtherError

| Property | Type | Description |
|----------|------|-------------|
| `message` | `String` | Error description |
| `throwable` | `Throwable?` | Original exception (for debugging) |

## Usage

```kotlin
// In RadarApp when permission denied (permission launcher callback)
val permanentlyDenied = activity?.let {
    !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)
} ?: false
if (permanentlyDenied) {
    locationViewModel.onPermissionPermanentlyDenied()
} else {
    locationViewModel.onPermissionDenied()
}

// In LocationService
locationService?.setPermissionError(LocationError.PermissionDenied(permanentlyDenied = true))
```

## Error Handling

### PermissionDenied

- Shows error card in CurrentLocationCard.
- "Grant Permission" button → requests permission again.

### Permanently Denied

- Shows error card in CurrentLocationCard.
- "Open Settings" button → opens app settings.

### ServiceUnavailable — TODO / planned

- Intended to show an error card with the message and an "Open Location Settings" button.
- The UI branch exists, but `LocationService` does not emit this error yet.
- Currently, Play Services/security failures in `startLocationUpdates()` simply stop the service.
- A TODO comment in `LocationService` marks where this should be implemented.

### OtherError — TODO / planned

- Intended to show an error card with the message.
- The UI branch exists, but nothing constructs this error yet.
- A TODO comment in `LocationService` marks where this should be implemented.

## Related Documentation

- [LocationViewModel](../api/LocationViewModel.md) - Handles permission errors
- [CurrentLocationCard](../api/CurrentLocationCard.md) - Displays errors
- [RadarApp](../ui/RadarApp.md) - Requests permissions and checks permanent denial
