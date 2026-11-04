package com.ameraldo.radar

import com.ameraldo.radar.data.DistanceUnits
import com.ameraldo.radar.data.rangeSteps
import com.ameraldo.radar.utils.toMeters
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for distance unit conversion, option lists, and legacy value normalization.
 */
class DistanceSettingsTest {
    @Test
    fun toMeters_metric_returnsValueUnchanged() {
        assertEquals(30f, DistanceUnits.METRIC.toMeters(30f), 0.0001f)
    }

    @Test
    fun toMeters_imperial_convertsFeetToMeters() {
        assertEquals(9.144f, DistanceUnits.IMPERIAL.toMeters(30f), 0.0001f)
    }

    @Test
    fun distanceOptions_matchUnitSystem() {
        assertEquals(listOf(1, 5, 10, 25, 50, 100), DistanceUnits.METRIC.distanceOptions)
        assertEquals(listOf(3, 15, 30, 80, 160, 320), DistanceUnits.IMPERIAL.distanceOptions)
    }

    @Test
    fun defaultDistance_matchesUnitSystem() {
        assertEquals(5, DistanceUnits.METRIC.defaultDistance)
        assertEquals(15, DistanceUnits.IMPERIAL.defaultDistance)
    }

    @Test
    fun normalizeDistance_missingValue_returnsDefault() {
        assertEquals(5, DistanceUnits.METRIC.normalizeDistance(null))
        assertEquals(15, DistanceUnits.IMPERIAL.normalizeDistance(null))
    }

    @Test
    fun normalizeDistance_validValue_isPreserved() {
        assertEquals(25, DistanceUnits.METRIC.normalizeDistance(25))
        assertEquals(160, DistanceUnits.IMPERIAL.normalizeDistance(160))
    }

    @Test
    fun normalizeDistance_legacyValue_fallsBackToDefault() {
        assertEquals(5, DistanceUnits.METRIC.normalizeDistance(15))
        assertEquals(15, DistanceUnits.IMPERIAL.normalizeDistance(5))
    }

    @Test
    fun maxRangeOptions_matchUnitSystem() {
        assertEquals(
            listOf(100, 500, 1000, 1500, 2000, 5000),
            DistanceUnits.METRIC.maxRangeOptions
        )
        assertEquals(
            listOf(1320, 2640, 5280, 7920, 10560, 26400),
            DistanceUnits.IMPERIAL.maxRangeOptions
        )
    }

    @Test
    fun normalizeMaxRange_missingOrLegacy_returnsDefault() {
        assertEquals(1000, DistanceUnits.METRIC.normalizeMaxRange(null))
        assertEquals(5280, DistanceUnits.IMPERIAL.normalizeMaxRange(1000))
    }

    @Test
    fun unitDefaults_haveSensibleRanges() {
        assertEquals(1000, DistanceUnits.METRIC.defaultMaxRange)
        assertEquals(5280, DistanceUnits.IMPERIAL.defaultMaxRange)
        assertEquals(500f, DistanceUnits.METRIC.defaultMaxRange / 2f, 0.0001f)
        assertEquals(2640f, DistanceUnits.IMPERIAL.defaultMaxRange / 2f, 0.0001f)
    }

    @Test
    fun rangeSteps_areQuarterIncrements() {
        assertEquals(listOf(250f, 500f, 750f, 1000f), rangeSteps(1000))
        assertEquals(listOf(1320f, 2640f, 3960f, 5280f), rangeSteps(5280))
        assertEquals(listOf(330f, 660f, 990f, 1320f), rangeSteps(1320))
    }
}
