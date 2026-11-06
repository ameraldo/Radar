# RadarView Component

> Main radar visualization component with compass and satellite display

RadarView is a Canvas-based Compose component that renders a radar display with compass ring, satellite blips, recorded points, and following indicators.

## Overview

**Location**: `app/src/main/java/com/ameraldo/radar/ui/components/RadarView.kt`

## Features

### Radar Display
- **Rotating compass ring** with cardinal directions (N, E, S, W)
- **Stationary radar grid** with concentric rings and polar/cartesian mesh
- **Animated sweep line** with trailing gradient (3s per revolution)
- **Center dot** marking device position

### Data Visualization
- **Recorded points**: Green (start), amber (current/last), dim amber (path), with three-layer rendering and an amber connecting line
- **Satellite blips**: Locked satellites are green; unlocked satellites use a continuous red → orange → yellow gradient
- **Following indicator**: Dashed line from center to the next point plus a yellow inward arrow on the compass ring
- **Compass arrows**: A stationary arrow at the top and a rotating red arrow that tracks true north

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `modifier` | `Modifier` | Modifier for styling |
| `headingDegrees` | `Float` | Current compass heading (0-360°) |
| `satelliteBlips` | `List<SatelliteBlip>?` | Satellites to display (null = radar/route mode) |
| `recordedPoints` | `List<PolarPoint>` | Recorded or following points to display |
| `nextPointToFollow` | `PolarPoint?` | Next point to follow (shows arrow + dashed line) |
| `radarRange` | `Float` | Selected radar range in meters/feet |
| `radarDistanceUnits` | `DistanceUnits` | Current distance units |

## Private Drawing Functions

| Function | Description |
|----------|-------------|
| `drawCompassRing()` | Draws outer ring with tick marks and cardinal labels |
| `drawCartesianGrid()` | Draws horizontal and vertical grid lines |
| `drawGridSegment()` | Draws individual grid segment (clipped to radar) |
| `drawTicks()` | Draws tick marks on radar circle |
| `drawSweepTrail()` | Draws fading arc behind sweep line |
| `drawArrow()` | Draws directional arrow (inward/outward) |

## Satellite Blip Colors

Satellite colors in `RadarView` are intentionally simple:

| Satellite state | Color |
|-----------------|-------|
| Locked (`isLocked == true`) | Green |
| Unlocked | Continuous gradient from red through orange to yellow, based on `signalStrength` |

Unlocked blips interpolate from red at 0 dB-Hz to yellow at approximately 42 dB-Hz:

- 0–21 dB-Hz: red → orange
- 21–42 dB-Hz: orange → yellow

The discrete signal-quality thresholds and labels (`Excellent`, `Good`, `Usable`, `Poor`, `Very Weak`) are implemented in [SatellitesList](SatellitesList.md), not in `RadarView`.

## Distance Ring Labels

Distance labels are drawn only when `satelliteBlips == null` (i.e. on the Radar screen, not the Home screen). When shown:

- Only the first three rings are labeled (`r <= radarR * 0.75f`).
- Labels are drawn at 45° to avoid the crosshairs.
- Each label is offset 12 px outside its ring.
- Ring values start at `radarRange / 4` and increase by `radarRange / 4` per ring.
- Labels are formatted with `DistanceUnits.formatDistance()` in the current unit system.

## Smooth Compass Behavior

The component uses accumulated rotation to avoid wraparound jumps at 0°/360°:
1. Track `previousHeading` and `accumulatedHeading`
2. Calculate shortest-path delta: `((new - old + 540) % 360) - 180`
3. Animate with `animateFloatAsState` (200ms, LinearEasing)

## Polar Coordinate System

Points are converted to polar coordinates via `toPolarPoints()`:
- **angleDeg**: Bearing from current position (0° = North, clockwise)
- **radiusFraction**: Distance as a fraction of the radar range (0.0 = center, clamped to 1.0 at the outer ring)
- **distanceMeters**: Raw great-circle distance for display

The single-point helper `toPolarPoint()` is private to `LocationUtils.kt`.

## Related Documentation

- [LocationUtils](LocationUtils.md) - Polar coordinate conversion
- [SensorViewModel](../api/SensorViewModel.md) - Compass heading
- [SatelliteBlip](../api/SatelliteBlip.md) - Satellite data model
- [SatellitesList](SatellitesList.md) - Discrete signal thresholds and labels
