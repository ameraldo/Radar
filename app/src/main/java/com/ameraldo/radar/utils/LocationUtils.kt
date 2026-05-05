package com.ameraldo.radar.utils

import com.ameraldo.radar.data.DistanceUnits
import com.ameraldo.radar.data.RecordedPointEntity
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Represents a point in polar coordinates relative to the current GPS position.
 * Used for displaying points on the radar visualization.
 *
 * @property angleDeg Bearing from current position to the point (0° = North, clockwise)
 * @property radiusFraction Distance as fraction of radar radius (0.0 = center, 1.0 = outer ring)
 * @property distanceMeters Raw great-circle distance in meters (for display purposes)
 */
data class PolarPoint(
    val angleDeg: Float,
    val radiusFraction: Float,
    val distanceMeters: Float
)

/**
 * Converts a list of recorded GPS points to polar coordinates relative to current position.
 *
 * Maps each [RecordedPointEntity] to a [PolarPoint] using the current location as origin.
 * Used by [com.ameraldo.radar.ui.RadarApp] to prepare points for radar display.
 *
 * @param currentLat Current latitude in degrees (origin for polar conversion)
 * @param currentLon Current longitude in degrees (origin for polar conversion)
 * @param points List of recorded GPS points to convert
 * @param radarRange The real-world distance (meters or feet) the outer radar ring represents
 * @param radarDistanceUnits Unit system for radar range conversion
 * @return List of PolarPoint objects ready for radar display
 */
fun toPolarPoints(
    currentLat: Double, currentLon: Double,
    points: List<RecordedPointEntity>,
    radarRange: Float,
    radarDistanceUnits: DistanceUnits
): List<PolarPoint> {
    // Convert each recorded point to polar coordinates relative to current position
    return points.map { point ->
        toPolarPoint(currentLat, currentLon, point.latitude, point.longitude,
            radarRange, radarDistanceUnits)
    }
}

/**
 * Calculates the great-circle distance between two GPS coordinates using the Haversine formula.
 *
 * The Haversine formula determines the distance between two points on a sphere
 * given their latitudes and longitudes. It accounts for Earth's curvature.
 *
 * Formula: d = 2r * atan2(√a, √(1-a))
 * where a = sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)
 *
 * @param lat1 Latitude of first point in degrees
 * @param lon1 Longitude of first point in degrees
 * @param lat2 Latitude of second point in degrees
 * @param lon2 Longitude of second point in degrees
 * @return Distance in meters (great-circle distance)
 */
fun calculatePointsDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    // Earth's mean radius in meters (WGS84 ellipsoid)
    val r = 6371000.0

    // Convert coordinate differences to radians (Haversine formula requires radians)
    val dLat = Math.toRadians(lat2 - lat1)  // Latitude difference in radians
    val dLon = Math.toRadians(lon2 - lon1)  // Longitude difference in radians

    // Haversine formula:
    // a = sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)
    // This calculates the square of half the chord length between the points
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)

    // Distance = 2 * R * atan2(√a, √(1-a))
    // atan2 returns the angle whose tangent is y/x, handling all quadrants correctly
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
}

/**
 * Converts two lat/lon coordinates into a polar point relative to the current position.
 *
 * Performs two key calculations:
 * 1. **Distance**: Great-circle distance using Haversine formula
 * 2. **Bearing**: Initial bearing from current position to target (0° = North, clockwise)
 *
 * The result is used for positioning points on the radar display.
 *
 * @param currentLat Current latitude in degrees (origin)
 * @param currentLon Current longitude in degrees (origin)
 * @param targetLat Target latitude in degrees
 * @param targetLon Target longitude in degrees
 * @param radarRange The real-world distance the outer radar ring represents
 * @param radarDistanceUnits Unit system for radar range (affects unit conversion)
 * @return [PolarPoint] with angle, radius fraction, and raw distance
 */
private fun toPolarPoint(
    currentLat: Double, currentLon: Double,
    targetLat: Double,  targetLon: Double,
    radarRange: Float,
    radarDistanceUnits: DistanceUnits
): PolarPoint {
    // Earth's mean radius in meters (WGS84 ellipsoid)
    val earthRadius = 6371000.0

    // Convert radar range to meters for consistent calculation
    // (radarRange may be in feet for imperial units display)
    val radarRangeMeters = (
            if (radarDistanceUnits == DistanceUnits.METRIC) radarRange
            else (radarRange / 3.28084).toFloat() // feet → meters conversion
    )

    // Convert all coordinates to radians for trigonometric functions
    val lat1 = Math.toRadians(currentLat)  // Current position latitude (radians)
    val lat2 = Math.toRadians(targetLat)    // Target position latitude (radians)
    val dLat = Math.toRadians(targetLat - currentLat)  // Latitude difference (radians)
    val dLon = Math.toRadians(targetLon - currentLon)  // Longitude difference (radians)

    // --- STEP 1: Calculate great-circle distance using Haversine formula ---
    // Formula: a = sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)
    // This calculates the square of half the chord length between the points
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) *
            sin(dLon / 2) * sin(dLon / 2)

    // Distance = 2 * R * atan2(√a, √(1-a))
    // atan2 returns the angle whose tangent is y/x, handling all quadrants
    val distanceMeters = (2 * earthRadius * atan2(sqrt(a), sqrt(1 - a))).toFloat()

    // --- STEP 2: Calculate initial bearing (direction) from current to target ---
    // Bearing formula using atan2:
    //   y = sin(Δlon) * cos(lat2)
    //   x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(Δlon)
    //   bearing = atan2(y, x) in radians
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    val bearingRad = atan2(y, x)

    // Convert bearing from radians to degrees, normalize to 0-360° range
    // (atan2 returns -π to +π, we need 0-360° with 0° = North)
    val bearingDeg = ((Math.toDegrees(bearingRad).toFloat() + 360f) % 360f)

    // --- STEP 3: Calculate radius fraction for radar display ---
    // Fraction = distance to point / radar's max range
    // Clamped to 1.0 (points beyond radar range appear at outer ring)
    val radiusFraction = (distanceMeters / radarRangeMeters).coerceAtMost(1f)

    return PolarPoint(
        angleDeg      = bearingDeg,
        radiusFraction = radiusFraction,
        distanceMeters = distanceMeters
    )
}
