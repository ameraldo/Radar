# LocationUtils

> Utility functions for GPS coordinate calculations

LocationUtils provides polar coordinate conversion and distance calculations for the radar display.

## Overview

**Package**: `com.ameraldo.radar.utils`

## Key Functions

### toPolarPoints()

Converts a list of recorded GPS points to polar coordinates relative to current position.

**Parameters**:
- `currentLat`: Current latitude in degrees
- `currentLon`: Current longitude in degrees
- `points`: List of recorded GPS points
- `radarRange`: Outer radar ring distance
- `radarDistanceUnits`: Current distance units

**Returns**: `List<PolarPoint>` ready for radar display

### toPolarPoint()

Converts two lat/lon coordinates into a polar point relative to current position.

**Parameters**:
- `currentLat`, `currentLon`: Current position (origin)
- `targetLat`, `targetLon`: Target position
- `radarRange`, `radarDistanceUnits`: For radius fraction calculation

**Returns**: `PolarPoint` with angle, radius fraction, and raw distance

### calculatePointsDistance()

Calculates great-circle distance using the Haversine formula.

**Formula**: d = 2r × atan2(√a, √(1-a))
where a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)

**Parameters**:
- `lat1`, `lon1`: First point coordinates
- `lat2`, `lon2`: Second point coordinates

**Returns**: Distance in meters (great-circle distance)

## Data Classes

### PolarPoint

| Property | Type | Description |
|----------|------|-------------|
| `angleDeg` | `Float` | Bearing from current position (0° = North, clockwise) |
| `radiusFraction` | `Float` | Distance as fraction of radar radius (0.0 = center, 1.0 = outer ring) |
| `distanceMeters` | `Float` | Raw great-circle distance in meters |

## Usage in RadarApp

```kotlin
// Convert recorded points to polar coordinates
val recordedPolarPoints = remember(currentRoutePoints, locationState.latitude, ...) {
    toPolarPoints(lat, lon, currentRoutePoints, radarRange, radarDistanceUnits)
}
```

## Related Documentation

- [RadarView](../api/RadarView.md) - Consumes PolarPoint data
- [LocationService](LocationService.md) - Source of GPS points
