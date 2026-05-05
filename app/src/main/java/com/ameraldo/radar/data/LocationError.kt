package com.ameraldo.radar.data

/**
 * Sealed class representing location-related errors.
 *
 * @see LocationError.NONE No error
 * @see LocationError.PermissionDenied Location permission denied
 * @see LocationError.ServiceUnavailable Location service unavailable
 * @see LocationError.OtherError Other unexpected errors
 */
sealed class LocationError {
    object NONE : LocationError()
    data class PermissionDenied(val permanentlyDenied: Boolean = false) : LocationError()
    data class ServiceUnavailable(val message: String) : LocationError()
    data class OtherError(val message: String, val throwable: Throwable? = null) : LocationError()
}