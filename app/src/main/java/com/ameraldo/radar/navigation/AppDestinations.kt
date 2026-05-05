package com.ameraldo.radar.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Defines the top-level navigation destinations for the app.
 *
 * Used by [androidx.compose.material3.adaptive.navigationuite.NavigationSuiteScaffold]
 * to render the bottom navigation bar.
 * Each destination maps to a screen composable in [com.ameraldo.radar.ui.RadarApp].
 *
 * @property label Display text shown in the navigation bar
 * @property icon Icon displayed in the navigation bar (from Material Icons)
 */
enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    /** Home screen - current location with satellite list */
    HOME("Home", Icons.Default.SatelliteAlt),

    /** Radar screen - real-time radar visualization of recorded/following points */
    RADAR("Radar", Icons.Default.Radar),

    /** Routes screen - list of saved routes available for following */
    ROUTES("Routes", Icons.Default.Directions),

    /** Settings screen - distance units and radar range preferences */
    SETTINGS("Settings", Icons.Default.Settings)
}