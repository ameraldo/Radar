# AppSettings

> DataStore-based preferences for user settings

AppSettings manages app preferences using Android DataStore (Preferences), providing type-safe access to user settings.

## Overview

**Package**: `com.ameraldo.radar.data`

**Key Responsibilities**:
- Store and retrieve distance units preference
- Store and retrieve radar range preferences
- Store and retrieve recording and following distances
- Store and retrieve adaptive following preference
- Provide reactive Flows for settings changes
- Normalize legacy distance values to the current unit system

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
| `recordingDistance` | `Flow<Int>` | Minimum movement before recording a new point |
| `followingDistance` | `Flow<Int>` | Proximity required to reach a followed waypoint |

Distance flows emit values in the current unit system: meters for metric and
feet for imperial. Values not present in the current unit's option list are
normalized to that unit's default.

## Public Methods

| Method | Description |
|--------|-------------|
| `setMaxRange(range: Int)` | Update maximum range |
| `setSelectedRange(range: Float)` | Update selected range |
| `setDistanceUnits(units: DistanceUnits)` | Update distance units only |
| `updateUnitSettings(...)` | Atomically update units, ranges, and distances |
| `setAdaptiveFollowing(enabled: Boolean)` | Update adaptive following preference |
| `setRecordingDistance(distance: Int)` | Update recording distance |
| `setFollowingDistance(distance: Int)` | Update following distance |

## Distance Options

Recording and following share the same selectable values:

| Unit system | Options | Default |
|-------------|---------|---------|
| Metric | 1, 5, 10, 25, 50, 100 m | 5 m |
| Imperial | 3, 30, 80, 160, 320 ft | 30 ft |

## Default Values

| Setting | Default |
|---------|---------|
| `maxRange` | 1000 m (metric) / 5280 ft (imperial) |
| `selectedRange` | 500 m (metric) / 2640 ft (imperial) |
| `distanceUnits` | METRIC |
| `adaptiveFollowing` | false (disabled) |
| `recordingDistance` | 5 m (metric) / 30 ft (imperial) |
| `followingDistance` | 5 m (metric) / 30 ft (imperial) |

## Related Documentation

- [RouteDao](RouteDao.md) - Route persistence
- [Settings Screen](../screens/settings.md)