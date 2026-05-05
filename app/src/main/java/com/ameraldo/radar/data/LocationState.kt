package com.ameraldo.radar.data

/**
 * Represents the current location state including coordinates, accuracy, and satellite info.
 *
 * @property latitude Current latitude, or null if unavailable
 * @property longitude Current longitude, or null if unavailable
 * @property accuracy GPS accuracy in meters
 * @property satellites List of visible GPS satellites
 * @property isLoading Whether location is still being acquired
 * @property error Current error state, if any
 */
data class LocationState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val satellites: List<SatelliteBlip> = emptyList(),
    val isLoading: Boolean = true,
    val error: LocationError = LocationError.NONE
)