# SatellitesList Component#

> Displays GPS satellite information with detailed dialog.

SatellitesList shows a card with satellite count and opens a full dialog with detailed satellite information (signal strength, azimuth, elevation, etc.).

## Overview#

**Location**: `app/src/main/java/com/ameraldo/radar/ui/screens/home/SatellitesList.kt`

## Features#

### Satellite Card
- Shows locked count / total in view (e.g., "3 locked / 12 in view")
- Tappable to open detailed dialog
- Uses `rememberSavable` for dialog state persistence

### Satellite Dialog
- Full-screen dialog with:
  - Scrollable list of all satellites
  - Signal strength bar (color-coded)
  - Lock status badge ("LOCKED" / "VISIBLE")
  - Detail grid (azimuth, elevation, signal, almanac, ephemeris, frequency)

## Parameters#

| Parameter | Type | Description |
|-----------|------|-------------|
| `satellites` | `List<SatelliteBlip>` | List of visible GPS satellites |
| `modifier` | `Modifier` | Modifier for styling |

## Sub-Components#

| Component | Description |
|-----------|-------------|
| `SatelliteDialog` | Full-screen dialog with satellite list |
| `SatelliteRow` | Single satellite info row |
| `LockBadge` | Lock status badge (LOCKED/VISIBLE) |
| `SignalBar` | Signal strength bar with color coding |
| `DetailItem` | Label-value pair for satellite details |

## Signal Strength Labels#

| Signal (dB-Hz) | Label | Color |
|---------------|-------|-------|
| ≥42 | Excellent | Green |
| 35-41 | Good | Light Green |
| 30-34 | Usable | Yellow |
| 20-29 | Poor | Orange |
| <20 | Very Weak | Red |

## Related Documentation#

- [SatelliteBlip](../api/SatelliteBlip.md) - Data model
- [Home Screen](../screens/home.md) - Parent screen
- [LocationService](../api/LocationService.md) - Source of satellite data
