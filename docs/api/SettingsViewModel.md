# SettingsViewModel

> Manages app settings (distance units, radar range, adaptive following)

SettingsViewModel handles user preferences including distance units (metric/imperial), radar display range, and adaptive following mode.

## Overview

**Package**: `com.ameraldo.radar.viewmodel`

**Key Responsibilities**:
- Manage distance units preference (metric/imperial)
- Manage radar range settings (max range, selected range)
- Manage adaptive following preference
- Generate range selector options

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`SettingsViewModel.kt`](../../app/src/main/java/com/ameraldo/radar/viewmodel/SettingsViewModel.kt)

## State Properties

| Property | Type | Description |
|----------|------|-------------|
| `distanceUnits` | `StateFlow<DistanceUnits>` | Current distance units (METRIC or IMPERIAL) |
| `maxRange` | `StateFlow<Int>` | Maximum radar range in meters/feet |
| `selectedRange` | `StateFlow<Float>` | Currently selected radar range |
| `availableMaxRanges` | `StateFlow<List<Int>>` | Available max range options |
| `adaptiveFollowing` | `StateFlow<Boolean>` | Adaptive following enabled state |

## Public Methods

| Method | Description |
|--------|-------------|
| `updateMaxRange(range)` | Update maximum radar range |
| `updateSelectedRange(range)` | Update selected radar range |
| `updateDistanceUnits(units)` | Change distance units |
| `updateAdaptiveFollowing(enabled)` | Toggle adaptive following on/off |
| `generateRangeList(maximum)` | Generate list of range options |

## Related Documentation

- [AppSettings](AppSettings.md) - DataStore preferences
- [Settings Screen](../screens/settings.md)