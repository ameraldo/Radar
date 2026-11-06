# Radar Screen

> Displays recorded or followed route points on a radar visualization

## Overview

The Radar Screen shows the radar visualization with recorded route points or route points being followed. It provides controls for recording, following, and adjusting the radar range.

## Location

**File**: `app/src/main/java/com/ameraldo/radar/ui/screens/radar/RadarScreen.kt`

## Features

### Radar View
- Displays the current location at the center.
- Shows recorded route points while recording.
- Shows the remaining route points while following.
- When following, highlights the next point with a yellow inward arrow and a dashed amber line.
- Rotates based on device compass heading.
- The range selector controls the displayed radius.

### Current Location Card
- Shows current latitude, longitude, and GPS accuracy.
- Shows a recording indicator while recording.
- **TODO:** Following status is not shown yet; add it when following-status support is implemented.

### Range Selector
- Adjustable radar display range.
- Options derived from the configured maximum range (25%, 50%, 75%, 100%).
- Segmented button row for quick range adjustment.
- Values formatted in the current unit system.

### Route Card
- Contains a single full-width action button:
  - **Start Recording** when idle.
  - **Stop Recording** when recording.
  - **Stop Following** when following.
- Stop actions show a confirmation dialog.
- Stopping recording opens the save-route dialog.
- The card does not display route name, point count, or recording duration.
- The card has no start-following action; following starts from the Routes screen.

## Recording Mode

When recording a route:
- Points are captured automatically at the configured recording distance.
- Recorded points are displayed on the radar in real time.
- The route card shows the **Stop Recording** button.
- There is no FAB on this screen.

## Following Mode

When navigating back along a saved route:
- The saved route points are loaded and reversed so the first point to reach is at the front.
- The next point is indicated by a yellow arrow and a dashed line.
- Points are removed when they are within the configured following distance.
- With adaptive following enabled, all points up to and including the first matching point are removed.
- With adaptive following disabled, only the single next point is checked.
- Following stops automatically when no points remain.

## UI Components

| Component | Description |
|-----------|-------------|
| [RadarView](../api/RadarView.md) | Radar visualization with points |
| [CurrentLocationCard](../api/CurrentLocationCard.md) | Location coordinates and recording status |
| [RangeSelector](../api/RangeSelector.md) | Radar range adjustment |
| [RouteCard](../api/RouteCard.md) | Recording/following controls |

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
| `radarRangeList` | SettingsViewModel | Available range options |
| `selectedRangeIndex` | SettingsViewModel | Current selected range position |
| `selectedRange` | SettingsViewModel | Current display range |
| `currentRouteName` | LocationViewModel | Save-dialog prefill |
| `pendingStopAction` | UIStateViewModel | Notification stop actions |

## User Interactions

1. **Start Recording**: Tap the FAB on the Home screen, or tap **Start Recording** in the route card.
2. **Stop Recording**: Tap **Stop Recording** in the route card and confirm.
3. **Start Following**: Select a route on the Routes screen.
4. **Stop Following**: Tap **Stop Following** in the route card and confirm.
5. **Adjust Range**: Use the RangeSelector buttons.

## Display Formats

### Distance Units
- **Metric**: meters (m), kilometers (km)
- **Imperial**: feet (ft), miles (mi)

### Range Display
- Max range: 100 m to 5 km (metric) / 1320 ft to 26400 ft, displayed as 1/4 mi to 5.0 mi (imperial)
- Range options: 25%, 50%, 75%, 100% of max

## Related Documentation

- [RadarView Component](../api/RadarView.md) - Radar visualization
- [RangeSelector](../api/RangeSelector.md) - Range selector component
- [RouteCard](../api/RouteCard.md) - Recording/following controls
- [ConfirmationDialog](../api/ConfirmationDialog.md) - Confirmation dialogs
- [LocationViewModel](../api/LocationViewModel.md) - Location state
- [UIStateViewModel](../api/UIStateViewModel.md) - UI state management
