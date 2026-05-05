package com.ameraldo.radar.data

/**
 * Sealed class representing location-related errors.
 *
 * @see NONE No error state
 * @see PermissionDenied Location permission denied by user
 * @see ServiceUnavailable Location services disabled or unavailable
 * @see OtherError Other unexpected errors (with optional throwable for debugging)
 */
sealed class LocationError {
    /** No error - location is available or not yet requested */
    object NONE : LocationError()

    /**
     * Location permission denied.
     *
     * @property permanentlyDenied true when user checked "Don't ask again"
     */
    data class PermissionDenied(val permanentlyDenied: Boolean = false) : LocationError()

    /**
     * Location service unavailable (e.g., Google Play Services missing).
     *
     * @property message Description of the unavailability
     */
    data class ServiceUnavailable(val message: String) : LocationError()

    /**
     * Other unexpected location errors.
     *
     * @property message Error description
     * @property throwable Original exception for debugging (may be null)
     */
    data class OtherError(val message: String, val throwable: Throwable? = null) : LocationError()
}