# SettingsViewModel

> Manages app settings (distance units, radar range)

SettingsViewModel handles user preferences including distance units (metric/imperial) and radar display range.

## Overview

**Package**: `com.ameraldo.radar.viewmodel`

**Key Responsibilities**:
- Manage distance units preference (metric/imperial)
- Manage radar range settings (max range, selected range)
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

## Public Methods

| Method | Description |
|--------|-------------|
| `updateMaxRange(range)` | Update maximum radar range |
| `updateSelectedRange(range)` | Update selected radar range |
| `updateDistanceUnits(units)` | Change distance units |
| `generateRangeList(maximum)` | Generate list of range options |

## Related Documentation

- [AppSettings](AppSettings.md) - DataStore preferences
- [Settings Screen](../screens/settings.md)