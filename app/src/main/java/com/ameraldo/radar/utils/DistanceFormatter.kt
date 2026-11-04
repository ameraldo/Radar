package com.ameraldo.radar.utils

import com.ameraldo.radar.data.DistanceUnits
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val FEET_PER_MILE = 5280f
private const val METERS_PER_KILOMETER = 1000f
private const val MIN_MILES_FEET = 1000f
private const val FRACTION_TOLERANCE_MILES = 0.001f

private val MILE_FRACTIONS = listOf(
    1 to 8,
    1 to 4,
    3 to 8,
    1 to 2,
    5 to 8,
    3 to 4,
    7 to 8
)

/**
 * Formats a distance expressed in the unit system's base unit
 * (meters for METRIC, feet for IMPERIAL) as a compact UI label.
 *
 * Imperial distances below 1000ft stay in feet. Distances from
 * 1000ft to 1mi use eighth-mile fractions where possible. Whole
 * miles use one decimal place.
 */
fun DistanceUnits.formatDistance(
    value: Float,
    locale: Locale = Locale.getDefault()
): String = when (this) {
    DistanceUnits.METRIC -> {
        val meters = value.roundToInt()
        if (meters >= METERS_PER_KILOMETER) {
            "%.1fkm".format(locale, meters / METERS_PER_KILOMETER)
        } else {
            "${meters}m"
        }
    }

    DistanceUnits.IMPERIAL -> when {
        value < MIN_MILES_FEET -> "${value.roundToInt()}ft"
        value >= FEET_PER_MILE -> "%.1fmi".format(locale, value / FEET_PER_MILE)
        else -> formatSubMile(value / FEET_PER_MILE, locale)
    }
}

private fun formatSubMile(miles: Float, locale: Locale): String {
    val fraction = MILE_FRACTIONS.firstOrNull { (numerator, denominator) ->
        abs(miles - numerator.toFloat() / denominator) < FRACTION_TOLERANCE_MILES
    }

    if (fraction != null) {
        val (numerator, denominator) = fraction
        return "$numerator/${denominator}mi"
    }

    val text = "%.2f".format(locale, miles)
        .trimEnd('0')
        .trimEnd('.', ',')

    return "${text}mi"
}