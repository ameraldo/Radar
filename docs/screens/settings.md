# Settings Screen

> Configures app preferences (distance units, radar range)

## Overview

The Settings Screen allows users to configure app preferences including distance units and maximum radar range.

## Location

**File**: `app/src/main/java/com/ameraldo/radar/ui/screens/settings/SettingsScreen.kt`

## Features

### Distance Units
- **Metric**: Kilometers, meters
- **Imperial**: Miles, feet
- Toggle between units via segmented buttons

### Maximum Radar Range
- Controls maximum display range for radar
- Options vary by unit system:

**Metric Options**:

| Value | Display |
|-------|---------|
| 100 m | 0.1 km |
| 500 m | 0.5 km |
| 1000 m | 1 km |
| 1500 m | 1.5 km |
| 2000 m | 2 km |
| 5000 m | 5 km |

**Imperial Options**:

| Value | Display |
|-------|---------|
| 500 ft | 0.1 mi |
| 2500 ft | 0.5 mi |
| 5280 ft | 1 mi |
| 7920 ft | 1.5 mi |
| 10560 ft | 2 mi |
| 26400 ft | 5 mi |

Range selector updates:
- Range slider to select max value
- Selected value displayed in current units

## UI Components

| Component | Description |
|-----------|-------------|
| `SegmentedButton` | Distance unit selector |
| `Slider` | Max range selector |
| `Card` | Settings sections |

## State Dependencies

| State | Source | Usage |
|-------|--------|-------|
| `distanceUnits` | SettingsViewModel | Current unit system |
| `maxRange` | SettingsViewModel | Current max range |
| `availableMaxRanges` | SettingsViewModel | Available options |

## User Interactions

1. **Change units**: Tap Metric or Imperial button
2. **Change max range**: Drag slider to select value

## Settings Persistence

Settings stored in Android DataStore:
- Key: `distance_units` - "metric" or "imperial"
- Key: `max_range` - Integer value
- Key: `selected_range` - Float value (last used range)

## Effects of Changing Units

When changing distance units:
1. Max range resets to default (1000m / 5280ft)
2. Selected range resets to 50% of max
3. All display values update to new units

## Related Documentation

- [SettingsViewModel](../api/SettingsViewModel.md) - Settings management
- [AppSettings](../api/AppSettings.md) - DataStore preferences
- [DistanceUnits Enum](../api/AppSettings-detailed.md) - Unit definitions