package com.ameraldo.radar

import com.ameraldo.radar.data.DistanceUnits
import com.ameraldo.radar.utils.formatDistance
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceFormatterTest {

    // Imperial below 1000ft
    @Test
    fun formatDistance_imperial_belowThreshold_staysInFeet() {
        assertEquals("330ft", DistanceUnits.IMPERIAL.formatDistance(330f, Locale.US))
        assertEquals("660ft", DistanceUnits.IMPERIAL.formatDistance(660f, Locale.US))
        assertEquals("999ft", DistanceUnits.IMPERIAL.formatDistance(999f, Locale.US))
    }

    // Imperial fractions
    @Test
    fun formatDistance_imperial_eighthFractions() {
        assertEquals("1/4mi", DistanceUnits.IMPERIAL.formatDistance(1320f, Locale.US))
        assertEquals("3/8mi", DistanceUnits.IMPERIAL.formatDistance(1980f, Locale.US))
        assertEquals("1/2mi", DistanceUnits.IMPERIAL.formatDistance(2640f, Locale.US))
        assertEquals("5/8mi", DistanceUnits.IMPERIAL.formatDistance(3300f, Locale.US))
        assertEquals("3/4mi", DistanceUnits.IMPERIAL.formatDistance(3960f, Locale.US))
        assertEquals("7/8mi", DistanceUnits.IMPERIAL.formatDistance(4620f, Locale.US))
    }

    // Imperial whole miles
    @Test
    fun formatDistance_imperial_wholeMiles() {
        assertEquals("1.0mi", DistanceUnits.IMPERIAL.formatDistance(5280f, Locale.US))
        assertEquals("1.1mi", DistanceUnits.IMPERIAL.formatDistance(5940f, Locale.US))
        assertEquals("1.5mi", DistanceUnits.IMPERIAL.formatDistance(7920f, Locale.US))
        assertEquals("2.0mi", DistanceUnits.IMPERIAL.formatDistance(10560f, Locale.US))
        assertEquals("3.8mi", DistanceUnits.IMPERIAL.formatDistance(19800f, Locale.US))
        assertEquals("5.0mi", DistanceUnits.IMPERIAL.formatDistance(26400f, Locale.US))
    }

    // Imperial decimal fallback
    @Test
    fun formatDistance_imperial_nonFractionSubMile_usesDecimal() {
        assertEquals("0.24mi", DistanceUnits.IMPERIAL.formatDistance(1250f, Locale.US))
        assertEquals("0.36mi", DistanceUnits.IMPERIAL.formatDistance(1875f, Locale.US))
        assertEquals("0.47mi", DistanceUnits.IMPERIAL.formatDistance(2500f, Locale.US))
        assertEquals("0.56mi", DistanceUnits.IMPERIAL.formatDistance(2970f, Locale.US))
    }

    // Imperial below threshold shows feet even for 660ft (1/8mi)
    @Test
    fun formatDistance_imperial_660ft_showsFeet() {
        assertEquals("660ft", DistanceUnits.IMPERIAL.formatDistance(660f, Locale.US))
    }

    // Metric
    @Test
    fun formatDistance_metric() {
        assertEquals("250m", DistanceUnits.METRIC.formatDistance(250f, Locale.US))
        assertEquals("999m", DistanceUnits.METRIC.formatDistance(999f, Locale.US))
        assertEquals("1.0km", DistanceUnits.METRIC.formatDistance(1000f, Locale.US))
        assertEquals("5.0km", DistanceUnits.METRIC.formatDistance(5000f, Locale.US))
    }
}