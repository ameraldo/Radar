package com.ameraldo.radar

import com.ameraldo.radar.data.AppSettings
import com.ameraldo.radar.data.DistanceUnits
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
        assertEquals(listOf(1, 5, 10, 25, 50, 100), DistanceUnits.METRIC.distanceOptions())
        assertEquals(listOf(3, 30, 80, 160, 320), DistanceUnits.IMPERIAL.distanceOptions())
    }

    @Test
    fun defaultDistance_matchesUnitSystem() {
        assertEquals(5, DistanceUnits.METRIC.defaultDistance())
        assertEquals(30, DistanceUnits.IMPERIAL.defaultDistance())
    }

    @Test
    fun normalizeDistance_missingValue_returnsDefault() {
        assertEquals(5, DistanceUnits.METRIC.normalizeDistance(null))
        assertEquals(30, DistanceUnits.IMPERIAL.normalizeDistance(null))
    }

    @Test
    fun normalizeDistance_validValue_isPreserved() {
        assertEquals(25, DistanceUnits.METRIC.normalizeDistance(25))
        assertEquals(160, DistanceUnits.IMPERIAL.normalizeDistance(160))
    }

    @Test
    fun normalizeDistance_legacyValue_fallsBackToDefault() {
        assertEquals(5, DistanceUnits.METRIC.normalizeDistance(15))
        assertEquals(30, DistanceUnits.IMPERIAL.normalizeDistance(5))
    }

    @Test
    fun unitDefaults_haveSensibleRanges() {
        assertEquals(1000, AppSettings.DEFAULT_METRIC_MAX_RANGE)
        assertEquals(5280, AppSettings.DEFAULT_IMPERIAL_MAX_RANGE)
        assertEquals(500f, AppSettings.DEFAULT_METRIC_MAX_RANGE / 2f, 0.0001f)
        assertEquals(2640f, AppSettings.DEFAULT_IMPERIAL_MAX_RANGE / 2f, 0.0001f)
    }
}
