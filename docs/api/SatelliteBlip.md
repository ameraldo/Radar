# SatelliteBlip

> Data model for GPS satellite information

SatelliteBlip represents a single GPS satellite with its position, signal strength, and lock status.

## Overview

**Package**: `com.ameraldo.radar.data`

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `svid` | `Int` | Satellite ID (SV ID) |
| `constellation` | `String` | Constellation (GPS, GLONASS, Galileo, etc.) |
| `angleDeg` | `Float` | Azimuth angle (0-360°) |
| `elevationDeg` | `Float` | Elevation angle (0° = horizon, 90° = overhead) |
| `radiusFraction` | `Float` | Elevation mapped to 0-1 (for radar display) |
| `isLocked` | `Boolean` | Whether satellite is used in fix |
| `signalStrength` | `Float` | Signal-to-noise ratio (C/N0 in dB-Hz) |
| `hasAlmanac` | `Boolean` | Whether satellite has almanac data |
| `hasEphemeris` | `Boolean` | Whether satellite has ephemeris data |
| `carrierFrequencyMhz` | `Float?` | Carrier frequency in MHz (L1/L5) |

## Signal Strength Colors

| Signal (dB-Hz) | Color |
|---------------|-------|
| ≥42 | Green (Excelent) |
| 35-41 | Light Green (Good) |
| 30-34 | Yellow (Usable) |
| 20-29 | Orange (Poor) |
| <20 | Red (Very Weak) |

## Data Flow

```
GNSS Status Callback (GnssStatus.Callback)
    │
    ▼
onSatelliteStatusChanged(GnssStatus)
    │
    ▼
Map to SatelliteBlip:
  - angleDeg: azimuth
  - elevationDeg: elevation → radiusFraction
  - isLocked: usedInFix
  - signalStrength: Cn0DbHz
    │
    ▼
_locationState.value = _locationState.value.copy(satellites = blips)
```

## Related Documentation

- [LocationService](LocationService.md) - Source of satellite data
- [LocationState](LocationState.md) - Contains satellite list
- [RadarView](../api/RadarView.md) - Displays satellite blips
- [SatellitesList](../api/SatellitesList.md) - Satellite list component
