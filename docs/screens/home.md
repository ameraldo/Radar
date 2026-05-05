# Home Screen#

> Main entry point - displays radar view with satellite positions and recording controls#

## Overview#

The Home Screen is the main entry point of the Radar app. It displays a radar visualization of GPS satellites and provides access to route recording.

## Location#

**File**: `app/src/main/java/com.ameraldo.radar/ui/screens/home/HomeScreen.kt`

## Features#

### Radar View#
- Displays GPS satellites on a polar radar plot#
- Outer ring represents horizon (0° elevation)#
- Center represents directly overhead (90° elevation)#
- Each satellite shown with:#
  - Position based on azimuth (angle) and elevation#
  - Color indicates lock status (used in position fix)#
  - Size/shape indicates signal strength#

### Current Location Card#
- Shows current latitude/longitude#
- Displays GPS accuracy in meters#
- Shows loading state while acquiring location#
- Shows error state if permission denied or unavailable#

### Satellite List#
- Scrollable list of all visible GPS satellites#
- Shows for each satellite:#
  - Constellation (GPS, GLONASS, Galileo, etc.)#
  - Vehicle ID#
  - Signal strength (C/N0 in dB-Hz)#
  - Elevation angle#
  - Lock status#

### Recording FAB (Floating Action Button)#
- Tap to start recording a new route#
- Visual state changes when recording is active#
- Navigates to Radar screen when recording starts#

## UI Components#

| Component | Description |
|-----------|-------------|
| [RadarView](../api/RadarView.md) | Canvas-based radar visualization |
| [CurrentLocationCard](../api/CurrentLocationCard.md) | Displays current GPS coordinates and accuracy |
| [SatellitesList](../api/SatellitesList.md) | Shows satellite details in a list |

## State Dependencies#

| State | Source | Usage |
|-------|--------|-------|
| `locationState` | LocationViewModel | Display coordinates, accuracy, satellites |
| `isRecording` | LocationViewModel | FAB appearance |
| `headingDegrees` | SensorViewModel | Radar rotation |

## User Interactions#

1. **View satellites**: Automatically displayed on radar view#
2. **Check location**: Current coordinates shown in card#
3. **Start recording**: Tap FAB to begin route recording#

## Navigation#

- **From**: App launch#
- **To**: Radar screen (via FAB when recording starts)#

## Permissions Required#

- `ACCESS_FINE_LOCATION`#
- `ACCESS_COARSE_LOCATION`#

## Related Documentation#

- [RadarView Component](../api/RadarView.md) - Radar visualization#
- [LocationViewModel](../api/LocationViewModel.md) - Location state#
- [SensorViewModel](../api/SensorViewModel.md) - Compass heading#
- [CurrentLocationCard](../api/CurrentLocationCard.md) - Location display#
- [SatellitesList](../api/SatellitesList.md) - Satellite details#
