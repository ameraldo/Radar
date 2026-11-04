# SettingsViewModel

> Manages app settings (distance units, radar range, distances, adaptive following)

SettingsViewModel handles user preferences including distance units (metric/imperial), radar display range, recording/following distances, and adaptive following mode.

## Overview

**Package**: `com.ameraldo.radar.viewmodel`

**Key Responsibilities**:
- Manage distance units preference (metric/imperial)
- Manage radar range settings (max range, selected range position)
- Manage recording and following distances
- Manage adaptive following preference
- Derive radar range options from the maximum range via `rangeSteps()`

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`SettingsViewModel.kt`](../../app/src/main/java/com/ameraldo/radar/viewmodel/SettingsViewModel.kt)

## State Properties

| Property | Type | Description |
|----------|------|-------------|
| `distanceUnits` | `StateFlow<DistanceUnits>` | Current distance units (METRIC or IMPERIAL) |
| `maxRange` | `StateFlow<Int>` | Maximum radar range in meters/feet |
| `selectedRangeIndex` | `StateFlow<Int>` | Selected range position (0 = 25%, 3 = 100%) |
| `selectedRange` | `StateFlow<Float>` | Currently selected radar range for display |
| `radarRangeList` | `StateFlow<List<Float>>` | Four range values derived from `maxRange` (25/50/75/100%) |
| `availableMaxRanges` | `StateFlow<List<Int>>` | Available max range options |
| `recordingDistance` | `StateFlow<Int>` | Minimum movement before recording a point |
| `followingDistance` | `StateFlow<Int>` | Proximity required to reach a followed waypoint |
| `availableDistances` | `StateFlow<List<Int>>` | Available distance options for both settings |
| `adaptiveFollowing` | `StateFlow<Boolean>` | Adaptive following enabled state |

Initial values are `METRIC`, `maxRange = 1000`, `selectedRangeIndex = 1`,
`selectedRange = 500f`, and `radarRangeList = [250f, 500f, 750f, 1000f]` until
DataStore emits.

## Public Methods

| Method | Description |
|--------|-------------|
| `updateMaxRange(range: Int)` | Update maximum radar range |
| `updateSelectedRangeIndex(index: Int)` | Update selected range position (clamped to 0–3) |
| `updateDistanceUnits(distanceUnits: DistanceUnits)` | Change units and atomically reset all unit-dependent settings |
| `updateRecordingDistance(distance: Int)` | Update recording distance |
| `updateFollowingDistance(distance: Int)` | Update following distance |
| `updateAdaptiveFollowing(enabled: Boolean)` | Toggle adaptive following on/off |

## Related Documentation

- [AppSettings](AppSettings.md) - DataStore preferences
- [Settings Screen](../screens/settings.md)