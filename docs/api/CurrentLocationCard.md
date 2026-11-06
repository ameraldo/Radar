# CurrentLocationCard Component

> Displays current GPS location information in a card.

CurrentLocationCard shows latitude, longitude, accuracy, and handles loading, error, and success states. It also shows a recording indicator.

## Overview

**Location**: `app/src/main/java/com/ameraldo/radar/ui/components/CurrentLocationCard.kt`

## Features

### State Handling
- **Loading**: Shows a progress indicator and an "Acquiring location..." message.
- **Permission denied**: "Location permission needed" and a Grant Permission button.
- **Permission permanently denied**: "Please enable location in app settings" and an Open Settings button.
- **Service unavailable**: Rendered, but **TODO / not active** because the error is never emitted yet.
- **Other error**: Rendered, but **TODO / not active** because the error is never emitted yet. This branch has no retry button.
- **Success**: Shows coordinates and accuracy.

### Recording Indicator
- Animated pulsing red dot and "Recording" text.
- Uses an `infiniteRepeatable` animation (700ms, Reverse).
- **TODO:** Following status is not shown yet; add it when following-status support is implemented.

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `locationState` | `LocationState` | Current GPS location state |
| `isRecording` | `Boolean` | Whether recording is active (shows indicator) |
| `onRetryGrantPermissions` | `() -> Unit` | Callback to request location permissions |

## Coordinate Display

Shows three values in a row:
- **Latitude**: "XX.XXXXX°" (or "—" if null)
- **Longitude**: "XX.XXXXX°" (or "—" if null)
- **Accuracy**: "±X.Xm" (or "—" if null)

## Related Documentation

- [LocationState](../api/LocationState.md) - Data model
- [LocationError](../api/LocationError.md) - Error types
- [Home Screen](../screens/home.md) - Parent screen
- [SensorViewModel](../api/SensorViewModel.md) - Compass heading
