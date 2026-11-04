# RangeSelector Component

> Segmented button row for selecting radar display range

RangeSelector provides quick range selection for the radar display. Options are based on the current distance unit system.

## Overview

**Location**: `app/src/main/java/com/ameraldo/radar/ui/screens/radar/RangeSelector.kt`

## Features

- **Segmented button row** with Material 3 styling
- **Dynamic options** based on metric/imperial units
- **Visual feedback** for selected range
- **Automatic formatting** (m, km, ft, mi)

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `selectedIndex` | `Int` | Currently selected range position (0 = 25%, 3 = 100%) |
| `rangeList` | `List<Float>` | Available range options |
| `distanceUnits` | `DistanceUnits` | Current distance units |
| `onRangeChange` | `(Int) -> Unit` | Callback with the selected range position |
| `modifier` | `Modifier` | Modifier for styling |

## Range Options

### Metric (meters/kilometers)
| Value | Display |
|-------|---------|
| 250 | 250m |
| 500 | 500m |
| 750 | 750m |
| 1000 | 1.0km |

### Imperial (feet/miles)
| Value | Display |
|-------|---------|
| 1320 | 1/4mi |
| 2640 | 1/2mi |
| 3960 | 3/4mi |
| 5280 | 1.0mi |

## Usage

The range list comes from `SettingsViewModel.radarRangeList`, which maps the
configured maximum range through `rangeSteps(maxRange)`:
- 25%, 50%, 75%, 100% of the maximum range
- The selected position is held in `SettingsViewModel.selectedRangeIndex`
- Maximum range is configured in Settings screen

## Related Documentation

- [SettingsViewModel](../api/SettingsViewModel.md) - Radar range state (`radarRangeList`, `selectedRangeIndex`)
- [Settings Screen](../screens/settings.md) - Max range configuration
- [RadarScreen](../screens/radar.md) - Uses range selector
