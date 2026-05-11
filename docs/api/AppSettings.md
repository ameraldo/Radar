# AppSettings

> DataStore-based preferences for user settings

AppSettings manages app preferences using Android DataStore (Preferences), providing type-safe access to user settings.

## Overview

**Package**: `com.ameraldo.radar.data`

**Key Responsibilities**:
- Store and retrieve distance units preference
- Store and retrieve radar range preferences
- Store and retrieve adaptive following preference
- Provide reactive Flows for settings changes

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`AppSettings.kt`](../../app/src/main/java/com/ameraldo/radar/data/AppSettings.kt)

## State Properties

| Property | Type | Description |
|----------|------|-------------|
| `maxRange` | `Flow<Int>` | Maximum radar range |
| `selectedRange` | `Flow<Float>` | Selected radar range for display |
| `distanceUnits` | `Flow<DistanceUnits>` | Distance units (METRIC/IMPERIAL) |
| `adaptiveFollowing` | `Flow<Boolean>` | Adaptive following (skip missed waypoints when following) |

## Public Methods

| Method | Description |
|--------|-------------|
| `setMaxRange(range: Int)` | Update maximum range |
| `setSelectedRange(range: Float)` | Update selected range |
| `setDistanceUnits(units: DistanceUnits)` | Update distance units |
| `setAdaptiveFollowing(enabled: Boolean)` | Update adaptive following preference |

## Default Values

| Setting | Default |
|---------|---------|
| `maxRange` | 1000 (meters or feet) |
| `selectedRange` | 500 (meters or feet) |
| `distanceUnits` | METRIC |
| `adaptiveFollowing` | false (disabled) |

## Related Documentation

- [RouteDao](RouteDao.md) - Route persistence
- [Settings Screen](../screens/settings.md)