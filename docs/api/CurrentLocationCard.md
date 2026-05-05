# CurrentLocationCard Component#

> Displays current GPS location information in a card.

CurrentLocationCard shows latitude, longitude, accuracy, and handles different states (loading, error, success). Also shows recording indicator.

## Overview#

**Location**: `app/src/main/java/com/ameraldo/radar/ui/components/CurrentLocationCard.kt`

## Features#

### State Handling
- **Loading**: Shows progress indicator + "Acquiring location..." message
- **Error**: Shows error message with retry button
  - Permission denied: "Location permission needed" + Grant Permission button
  - Permission permanently denied: "Please enable location in app settings" + Open Settings button
  - Service unavailable: "Location services unavailable" + Open Location Settings button
- **Success**: Shows coordinates + accuracy

### Recording Indicator
- Animated pulsing red dot + "Recording" text
- Uses `infiniteRepeatable` animation (700ms, Reverse)

## Parameters#

| Parameter | Type | Description |
|-----------|------|-------------|
| `locationState` | `LocationState` | Current GPS location state |
| `isRecording` | `Boolean` | Whether recording is active (shows indicator) |
| `onRetryGrantPermissions` | `() -> Unit` | Callback to request location permissions |

## Coordinate Display#

Shows three values in a row:
- **Latitude**: "XX.XXXXX°" (or "—" if null)
- **Longitude**: "XX.XXXXX°" (or "—" if null)
- **Accuracy**: "±X.Xm" (or "—" if null)

## Related Documentation#

- [LocationState](../api/LocationState.md) - Data model
- [LocationError](../api/LocationError.md) - Error types
- [Home Screen](../screens/home.md) - Parent screen
- [SensorViewModel](../api/SensorViewModel.md) - Compass heading
