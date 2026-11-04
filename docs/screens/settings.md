# Settings Screen

> Configures app preferences (distance units, radar range, distances)

## Overview

The Settings Screen allows users to configure app preferences including distance units, maximum radar range, recording/following distances, and adaptive following. The content scrolls vertically when it exceeds the screen height.

## Location

**File**: `app/src/main/java/com/ameraldo/radar/ui/screens/settings/SettingsScreen.kt`

## Features

### Adaptive Following
- Toggle automatic skipping of missed waypoints during route following
- When enabled: scans all remaining points, skips ahead to the furthest match within the configured following distance
- When disabled: strict mode, only checks the single next point
- Default: disabled

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
| 100 m | 100m |
| 500 m | 500m |
| 1000 m | 1.0km |
| 1500 m | 1.5km |
| 2000 m | 2.0km |
| 5000 m | 5.0km |

**Imperial Options**:

| Value | Display |
|-------|---------|
| 1320 ft | 1/4mi |
| 2640 ft | 1/2mi |
| 5280 ft | 1.0mi |
| 7920 ft | 1.5mi |
| 10560 ft | 2.0mi |
| 26400 ft | 5.0mi |

Range selector updates:
- Range slider to select max value
- Selected value displayed in current units

### Recording Distance
- Minimum movement before a new GPS point is saved
- Options are shared with Following Distance:

| Unit system | Options | Default |
|-------------|---------|---------|
| Metric | 1, 5, 10, 25, 50, 100 m | 5 m |
| Imperial | 3, 15, 30, 80, 160, 320 ft | 15 ft |

### Following Distance
- Proximity required to mark a followed waypoint as reached
- Uses the same options and default as Recording Distance

## UI Components

| Component | Description |
|-----------|-------------|
| `SegmentedButton` | Distance unit selector |
| `Slider` | Max range, recording distance, and following distance selectors |
| `Switch` | Adaptive following toggle |
| `Card` | Settings sections |

## State Dependencies

| State | Source | Usage |
|-------|--------|-------|
| `distanceUnits` | SettingsViewModel | Current unit system |
| `maxRange` | SettingsViewModel | Current max range |
| `availableMaxRanges` | SettingsViewModel | Available range options |
| `recordingDistance` | SettingsViewModel | Current recording distance |
| `followingDistance` | SettingsViewModel | Current following distance |
| `availableDistances` | SettingsViewModel | Available distance options for both sliders |
| `adaptiveFollowing` | SettingsViewModel | Adaptive following toggle state |

## User Interactions

1. **Change units**: Tap Metric or Imperial button
2. **Change max range**: Drag slider to select value
3. **Change recording distance**: Drag the Recording Distance slider
4. **Change following distance**: Drag the Following Distance slider
5. **Toggle adaptive following**: Flip the switch on the Adaptive Following card

## Settings Persistence

Settings stored in Android DataStore:
- Key: `distance_units` - "metric" or "imperial"
- Key: `max_range` - Integer value
- Key: `selected_range_index` - Integer position (0 = 25%, 3 = 100%); the legacy `selected_range` Float key is removed on the next write
- Key: `recording_distance` - Integer value in current units
- Key: `following_distance` - Integer value in current units
- Key: `adaptive_following` - Boolean value

## Effects of Changing Units

When changing distance units, all unit-dependent settings are written atomically:
1. Max range resets to default (1000 m / 5280 ft)
2. Selected range resets to 50% of max (500 m / 2640 ft)
3. Recording distance resets to default (5 m / 15 ft)
4. Following distance resets to default (5 m / 15 ft)
5. All display values update to new units

## Related Documentation

- [SettingsViewModel](../api/SettingsViewModel.md) - Settings management
- [AppSettings](../api/AppSettings.md) - DataStore preferences
- [DistanceUnits](../api/DistanceUnits.md) - Unit definitions and option lists