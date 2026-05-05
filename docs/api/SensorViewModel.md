# SensorViewModel

> Handles compass/heading using device accelerometer and magnetometer

SensorViewModel manages device sensors to provide compass heading for the radar display.

## Overview

**Package**: `com.ameraldo.radar.viewmodel`

**Key Responsibilities**:
- Read accelerometer and magnetometer sensors
- Calculate device heading (azimuth)
- Expose heading as StateFlow for UI

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`SensorViewModel.kt`](../../app/src/main/java/com/ameraldo/radar/viewmodel/SensorViewModel.kt)

## State Properties

| Property | Type | Description |
|----------|------|-------------|
| `headingDegrees` | `StateFlow<Float>` | Current heading in degrees (0-360°) |

## Public Methods

| Method | Description |
|--------|-------------|
| `startListening()` | Start sensor updates |
| `stopListening()` | Stop sensor updates |

## Related Documentation

- [Radar Screen](../screens/radar.md) - Uses heading for display