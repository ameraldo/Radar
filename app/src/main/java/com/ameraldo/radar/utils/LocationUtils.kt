package com.ameraldo.radar.utils

import com.ameraldo.radar.data.DistanceUnits
import com.ameraldo.radar.data.RecordedPointEntity
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class PolarPoint(
    val angleDeg: Float,        // bearing from current position to recorded point
    val radiusFraction: Float,  // distance as fraction of radar radius (clamped 0-1)
    val distanceMeters: Float   // raw distance in meters
)

fun toPolarPoints(
    currentLat: Double, currentLon: Double,
    points: List<RecordedPointEntity>,
    radarRange: Float,
    radarDistanceUnits: DistanceUnits
): List<PolarPoint> {
    return points.map { point ->
        toPolarPoint(currentLat, currentLon, point.latitude, point.longitude,
            radarRange, radarDistanceUnits)
    }
}

fun calculatePointsDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val r    = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a    = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
}

/**
 * Converts two lat/lon coordinates into a polar point
 * relative to the current position, for display on the radar.
 *
 * @param radarRange the real-world distance the outer radar ring represents
 */
private fun toPolarPoint(
    currentLat: Double, currentLon: Double,
    targetLat: Double,  targetLon: Double,
    radarRange: Float,
    radarDistanceUnits: DistanceUnits
): PolarPoint {
    val earthRadius = 6371000.0 // meters

    val radarRangeMeters = (
            if (radarDistanceUnits == DistanceUnits.METRIC) radarRange
            else radarRange / 3.28084 // convert feet to meters
    ) as Float

    val lat1 = Math.toRadians(currentLat)
    val lat2 = Math.toRadians(targetLat)
    val dLat = Math.toRadians(targetLat - currentLat)
    val dLon = Math.toRadians(targetLon - currentLon)

    // Haversine distance
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) *
            sin(dLon / 2) * sin(dLon / 2)
    val distanceMeters = (2 * earthRadius * atan2(sqrt(a), sqrt(1 - a))).toFloat()

    // Bearing from current to target (0° = north, clockwise)
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    val bearingRad = atan2(y, x)
    val bearingDeg = ((Math.toDegrees(bearingRad).toFloat() + 360f) % 360f)

    val radiusFraction = (distanceMeters / radarRangeMeters).coerceAtMost(1f)

    return PolarPoint(
        angleDeg      = bearingDeg,
        radiusFraction = radiusFraction,
        distanceMeters = distanceMeters
    )
}
