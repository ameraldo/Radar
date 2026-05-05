package com.ameraldo.radar.data

/**
 * Data class representing a single GPS satellite visible to the device.
 *
 * @property angleDeg Azimuth angle in degrees (0-360°), used for radar display
 * @property radiusFraction Elevation mapped to 0-1 for radar display (0 = overhead, 1 = horizon)
 * @property isLocked Whether the satellite is being used in the position fix
 * @property signalStrength Signal-to-noise ratio in dB-Hz
 * @property svid Space Vehicle ID - unique identifier within the constellation
 * @property constellation Satellite system: GPS, GLONASS, Galileo, BeiDou, QZSS, SBAS
 * @property elevationDeg Angle above horizon (0° = horizon, 90° = directly overhead)
 * @property hasAlmanac Whether the satellite has broadcast rough orbital data
 * @property hasEphemeris Whether the satellite has broadcast precise orbital data (required for positioning)
 * @property carrierFrequencyMhz Signal frequency in MHz (e.g., L1 = 1575.42, L5 = 1176.45)
 */
data class SatelliteBlip(
    val angleDeg: Float,
    val radiusFraction: Float,
    val isLocked: Boolean,
    val signalStrength: Float,
    val svid: Int,
    val constellation: String,
    val elevationDeg: Float,
    val hasAlmanac: Boolean,
    val hasEphemeris: Boolean,
    val carrierFrequencyMhz: Float?
)