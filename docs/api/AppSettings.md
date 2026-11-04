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
| `selectedRangeIndex` | `Flow<Int>` | Selected radar range position (0 = 25%, 3 = 100%) |
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
| `setSelectedRangeIndex(index: Int)` | Update selected range position (clamped to 0–3) |
| `setDistanceUnits(distanceUnits: DistanceUnits)` | Update distance units only |
| `updateUnitSettings(distanceUnits: DistanceUnits, maxRange: Int, selectedRangeIndex: Int, recordingDistance: Int, followingDistance: Int)` | Atomically update units, ranges, and distances |
| `setAdaptiveFollowing(enabled: Boolean)` | Update adaptive following preference |
| `setRecordingDistance(distance: Int)` | Update recording distance |
| `setFollowingDistance(distance: Int)` | Update following distance |

## Distance Options

Recording and following share the same selectable values:

| Unit system | Options | Default |
|-------------|---------|---------|
| Metric | 1, 5, 10, 25, 50, 100 m | 5 m |
| Imperial | 3, 15, 30, 80, 160, 320 ft | 15 ft |

## Default Values

| Setting | Default |
|---------|---------|
| `maxRange` | 1000 m (metric) / 5280 ft (imperial) |
| `selectedRangeIndex` | 1 (50% → 500 m metric / 2640 ft imperial) |
| `distanceUnits` | METRIC |
| `adaptiveFollowing` | false (disabled) |
| `recordingDistance` | 5 m (metric) / 15 ft (imperial) |
| `followingDistance` | 5 m (metric) / 15 ft (imperial) |

## DataStore Keys

Settings are stored in the `settings` DataStore.

| Key | Type | Description |
|-----|------|-------------|
| `max_range` | Int | Maximum radar range |
| `selected_range_index` | Int | Selected range position (0–3) |
| `distance_units` | String | `"metric"` or `"imperial"` |
| `adaptive_following` | Boolean | Adaptive following enabled |
| `recording_distance` | Int | Recording distance in current units |
| `following_distance` | Int | Following distance in current units |
| `selected_range` | Float | Legacy absolute range; removed on the next write of `selected_range_index` or `updateUnitSettings` |

## Related Documentation

- [DistanceUnits](DistanceUnits.md) - Unit systems, option lists, and defaults
- [RouteDao](RouteDao.md) - Route persistence
- [Settings Screen](../screens/settings.md)