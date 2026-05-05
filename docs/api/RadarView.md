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
- **Recorded points**: Green (start), amber (current/path) with 3-layer rendering
- **Satellite blips**: Color-coded by signal strength (red → yellow → green)
- **Following indicator**: Dashed line from center to next point
- **Compass arrow**: Fixed at top (red, points to true North)

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `modifier` | `Modifier` | Modifier for styling |
| `headingDegrees` | `Float` | Current compass heading (0-360°) |
| `satelliteBlips` | `List<SatelliteBlip>?` | Satellites to display (null = show radar screen) |
| `recordedPoints` | `List<PolarPoint>` | Recorded points to display |
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

## Smooth Compass Behavior

The component uses accumulated rotation to avoid wraparound jumps at 0°/360°:
1. Track `previousHeading` and `accumulatedHeading`
2. Calculate shortest-path delta: `((new - old + 540) % 360) - 180`
3. Animate with `animateFloatAsState` (200ms, LinearEasing)

## Polar Coordinate System

Points are converted to polar coordinates via `toPolarPoints()`:
- **angleDeg**: Bearing from current position (0° = North, clockwise)
- **radiusFraction**: Distance as fraction of radar radius (0.0 = center, 1.0 = outer ring)
- **distanceMeters**: Raw great-circle distance for display

## Signal Strength Colors (Satellites)

| Signal (dB-Hz) | Color |
|---------------|-------|
| ≥42 | Green (Excellent) |
| 35-41 | Light Green (Good) |
| 30-34 | Yellow (Usable) |
| 20-29 | Orange (Poor) |
| <20 | Red (Very Weak) |

## Related Documentation

- [LocationUtils](../utils/LocationUtils.md) - Polar coordinate conversion
- [SensorViewModel](../api/SensorViewModel.md) - Compass heading
- [SatelliteBlip](../api/SatelliteBlip.md) - Satellite data model
