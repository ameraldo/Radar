# LocationError#

> Sealed class representing location-related errors#

LocationError defines the types of errors that can occur with location services.

## Overview#

**Package**: `com.ameraldo.radar.data`

## Error Types#

| Type | Description |
|------|-------------|
| `NONE` | No error state |
| `PermissionDenied` | Location permission denied by user |
| `ServiceUnavailable` | Location services disabled or unavailable |
| `OtherError` | Other unexpected errors |

## Properties#

### PermissionDenied#

| Property | Type | Description |
|----------|------|-------------|
| `permanentlyDenied` | `Boolean` | True if user checked "Don't ask again" |

### OtherError#

| Property | Type | Description |
|----------|------|-------------|
| `message` | `String` | Error description |
| `throwable` | `Throwable?` | Original exception (for debugging) |

## Usage#

```kotlin
// In MainActivity when permission denied
if (permanentlyDenied) {
    locationViewModel.onPermissionPermanentlyDenied()
} else {
    locationViewModel.onPermissionDenied()
}

// In LocationService
locationService?.setPermissionError(LocationError.PermissionDenied(permanentlyDenied = true))
```

## Error Handling#

### PermissionDenied#
- Shows error card in CurrentLocationCard#
- "Grant Permission" button → requests permission again#

### Permanently Denied#
- Shows error card in CurrentLocationCard#
- "Open Settings" button → opens app settings#

### ServiceUnavailable#
- Shows error card with message#
- "Open Location Settings" button → opens location settings#

## Related Documentation#

- [LocationViewModel](../api/LocationViewModel.md) - Handles permission errors#
- [CurrentLocationCard](../api/CurrentLocationCard.md) - Displays errors#
- [MainActivity](MainActivity.md) - Checks permanent denial#
