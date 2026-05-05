# Radar Screen

> Displays recorded route or follows saved route with radar visualization

## Overview

The Radar Screen shows the radar visualization with recorded route points or route being followed. It provides controls for recording, following, and adjusting radar range.

## Location

**File**: `app/src/main/java/com.ameraldo.radar/ui/screens/radar/RadarScreen.kt`

## Features

### Radar View
- Displays current location at center
- Shows recorded/following route points as dots
- When following: highlights next point to reach
- Radar rotates based on device compass heading
- Range selector controls visible radius

### Current Location Card
- Shows current latitude/longitude
- Displays GPS accuracy
- Shows recording/following status

### Range Selector
- Adjustable radar display range
- Options based on settings (metric/imperial)
- Segmented button for quick range adjustment

### Route Card
- Shows current route info:
  - Route name
  - Point count
  - Recording time
- Actions:
  - Stop recording
  - Start/stop following

## Recording Mode

When recording a route:
- Points captured automatically (every 5+ meters)
- Route points displayed in real-time
- FAB shows stop option
- Card shows recording duration/points

## Following Mode

When navigating back along a route:
- Route points shown from current location reversed
- Next point highlighted
- Points removed as you reach them
- "Following complete" when all points reached

## UI Components

| Component | Description |
|-----------|-------------|
| [RadarView](../api/RadarView.md) | Radar visualization with points |
| [CurrentLocationCard](../api/CurrentLocationCard.md) | Location coordinates |
| [RangeSelector](../api/RangeSelector.md) | Radar range adjustment |
| [RouteCard](../api/RouteCard.md) | Route info and actions |

## State Dependencies

| State | Source | Usage |
|-------|--------|-------|
| `locationState` | LocationViewModel | Current coordinates |
| `isRecording` | LocationViewModel | Recording mode |
| `isFollowing` | LocationViewModel | Following mode |
| `followingRemainingPoints` | LocationViewModel | Points to reach |
| `recordedPolarPoints` | RadarApp | Converted for display |
| `followingPolarPoints` | RadarApp | Converted for display |
| `headingDegrees` | SensorViewModel | Radar rotation |
| `distanceUnits` | SettingsViewModel | Display units |
| `maxRange` | SettingsViewModel | Max range options |
| `selectedRange` | SettingsViewModel | Current display range |
| `pendingStopAction` | UIStateViewModel | Notification stop actions |

## User Interactions

1. **Start Recording**: Tap FAB on HomeScreen → navigates here
2. **Stop Recording**: Tap stop in RouteCard
3. **Start Following**: Select route from RoutesScreen → navigates here
4. **Adjust Range**: Use RangeSelector buttons

## Display Formats

### Distance Units
- **Metric**: meters (m), kilometers (km)
- **Imperial**: feet (ft), miles (mi)

### Range Display
- Max range: 100m to 5km (metric) / 500ft to 5mi (imperial)
- Range options: 25%, 50%, 75%, 100% of max

## Related Documentation

- [RadarView Component](../api/RadarView.md) - Radar visualization
- [RangeSelector](../api/RangeSelector.md) - Range selector component
- [RouteCard](../api/RouteCard.md) - Route info card
- [ConfirmationDialog](../api/ConfirmationDialog.md) - Confirmation dialogs
- [LocationViewModel](../api/LocationViewModel.md) - Location state
- [UIStateViewModel](../api/UIStateViewModel.md) - UI state management
