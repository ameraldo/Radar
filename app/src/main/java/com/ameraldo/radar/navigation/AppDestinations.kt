package com.ameraldo.radar.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.SatelliteAlt),
    RADAR("Radar", Icons.Default.Radar),
    ROUTES("Locations", Icons.Default.Directions)
}