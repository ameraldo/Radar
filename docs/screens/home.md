# Home Screen

> Main entry point - radar view, current location, satellite summary, and recording control

## Overview

The Home Screen is the main entry point of the Radar app. It displays a radar visualization of GPS satellites, the current location card, a satellite summary card, and a floating action button (FAB) for starting route recording.

## Location

**File**: `app/src/main/java/com/ameraldo/radar/ui/screens/home/HomeScreen.kt`

## Features

### Radar View
- Displays GPS satellites on a polar radar plot.
- Outer ring represents the horizon (0° elevation).
- Center represents directly overhead (90° elevation).
- Each satellite is positioned using:
  - Azimuth for the angle.
  - Elevation for the radius.
- Locked satellites (used in the position fix) are green.
- Unlocked satellites use a continuous red → orange → yellow gradient based on C/N0 signal strength.
- All satellite blips use the same size and a three-layer glow/ring/core rendering. Size and shape do not encode signal strength.
- Distance ring labels are not drawn on the Home screen because it is displaying satellite blips.

### Current Location Card
- Shows current latitude, longitude, and GPS accuracy.
- Shows a loading state while acquiring a location.
- Shows permission-denied error states with "Grant Permission" or "Open Settings" actions.
- Shows a pulsing red "Recording" indicator while recording.
- **TODO:** `ServiceUnavailable` and `OtherError` handling is not active yet; see [LocationError](../api/LocationError.md).

### Satellite Summary Card
- Shows a summary such as `"3 locked / 12 in view"` with an info icon.
- Tapping the card opens a full-screen dialog containing:
  - A scrollable list of all visible satellites.
  - Constellation and SVID.
  - Lock status badge (`LOCKED` / `VISIBLE`).
  - Color-coded signal strength bar.
  - Azimuth, elevation, signal (dB-Hz), almanac, ephemeris, and frequency details.
  - A "No satellites in view" message when the list is empty.

### Recording FAB
- Tapping starts recording when no recording is active.
- Always navigates to the Radar screen.
- Uses a dimmed appearance while recording is active.
- Content description: `"Start Recording"`.

## UI Components

| Component | Description |
|-----------|-------------|
| [RadarView](../api/RadarView.md) | Canvas-based radar visualization |
| [CurrentLocationCard](../api/CurrentLocationCard.md) | Displays current GPS coordinates, accuracy, and recording status |
| [SatellitesList](../api/SatellitesList.md) | Satellite summary card and detail dialog |

## State Dependencies

| State | Source | Usage |
|-------|--------|-------|
| `locationState` | LocationViewModel | Coordinates, accuracy, satellites |
| `isRecording` | LocationViewModel | FAB appearance and recording indicator |
| `headingDegrees` | SensorViewModel | Radar rotation |

## User Interactions

1. **View satellites**: Displayed automatically on the radar view and satellite summary card.
2. **Inspect satellites**: Tap the satellite summary card to open the detail dialog.
3. **Check location**: Coordinates and accuracy are shown in the current location card.
4. **Start recording**: Tap the FAB to begin recording (if idle) and open the Radar screen.

## Navigation

- **From**: App launch or the Home tab.
- **To**: Radar screen (via the FAB).

## Permissions Required

- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `POST_NOTIFICATIONS`

## Related Documentation

- [RadarView Component](../api/RadarView.md) - Radar visualization
- [LocationViewModel](../api/LocationViewModel.md) - Location state
- [SensorViewModel](../api/SensorViewModel.md) - Compass heading
- [CurrentLocationCard](../api/CurrentLocationCard.md) - Location display
- [SatellitesList](../api/SatellitesList.md) - Satellite summary and details
