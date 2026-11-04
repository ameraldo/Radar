package com.ameraldo.radar.data

/**
 * Distance units enumeration and the single source of truth for all
 * unit-dependent static values.
 *
 * @property value String representation for storage
 * @property distanceOptions Selectable recording/following distances
 * @property defaultDistance Default recording/following distance
 * @property maxRangeOptions Selectable maximum radar ranges
 * @property defaultMaxRange Default maximum radar range
 */
enum class DistanceUnits(
    val value: String,
    val distanceOptions: List<Int>,
    val defaultDistance: Int,
    val maxRangeOptions: List<Int>,
    val defaultMaxRange: Int
) {
    /** Metric units: kilometers, meters */
    METRIC(
        value = "metric",
        distanceOptions = listOf(1, 5, 10, 25, 50, 100),
        defaultDistance = 5,
        maxRangeOptions = listOf(100, 500, 1000, 1500, 2000, 5000),
        defaultMaxRange = 1000
    ),
    /** Imperial units: miles, feet */
    IMPERIAL(
        value = "imperial",
        distanceOptions = listOf(3, 15, 30, 80, 160, 320),
        defaultDistance = 15,
        maxRangeOptions = listOf(1320, 2640, 5280, 7920, 10560, 26400),
        defaultMaxRange = 5280
    );

    /**
     * Normalizes a stored distance to a valid option for this unit system.
     *
     * Missing or legacy values (for example a metric value after switching to
     * imperial) fall back to [defaultDistance].
     *
     * @param stored Distance read from DataStore, or null if unset
     * @return A value from [distanceOptions]
     */
    fun normalizeDistance(stored: Int?): Int =
        stored?.takeIf { it in distanceOptions } ?: defaultDistance

    /**
     * Normalizes a stored radar range to a valid option for this unit system.
     *
     * @param stored Range read from DataStore, or null if unset
     * @return A value from [maxRangeOptions]
     */
    fun normalizeMaxRange(stored: Int?): Int =
        stored?.takeIf { it in maxRangeOptions } ?: defaultMaxRange

    companion object {
        /** Number of selectable radar range steps (25/50/75/100%). */
        const val RANGE_STEP_COUNT = 4
        /** Default selected range position: 50% (index 1). */
        const val DEFAULT_SELECTED_RANGE_INDEX = 1

        /**
         * Converts a string value to DistanceUnits.
         * Returns [METRIC] if the string doesn't match any known value.
         *
         * @param value String value from DataStore (e.g., "metric", "imperial")
         * @return DistanceUnits enum or METRIC as default fallback
         */
        fun fromString(value: String): DistanceUnits {
            return entries.find { it.value == value } ?: METRIC
        }
    }
}

/**
 * Generates the four selectable radar ranges: 25%, 50%, 75% and 100% of [maxRange].
 *
 * @param maxRange Maximum radar range in the current unit system
 * @return List of four increasing range values
 */
fun rangeSteps(maxRange: Int): List<Float> =
    (1..DistanceUnits.RANGE_STEP_COUNT)
        .map { it * maxRange.toFloat() / DistanceUnits.RANGE_STEP_COUNT }
