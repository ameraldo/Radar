package com.ameraldo.radar.ui.screens.routes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ameraldo.radar.viewmodel.RouteViewModel

/**
 * Routes screen composable.
 *
 * Displays saved routes or empty state message.
 * Allows users to follow a route or delete it.
 *
 * @param modifier Modifier for styling
 * @param routesViewModel For accessing saved routes
 * @param onFollowRoute Callback with route ID when user wants to follow
 */
@Composable
fun RoutesScreen(
    modifier: Modifier = Modifier,
    routesViewModel: RouteViewModel,
    onFollowRoute: (Long) -> Unit
) {
    val routes by routesViewModel.routes.collectAsState()

    if (routes.isEmpty()) {
        // Show empty state
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Saved Routes",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your saved routes will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RoutesList(routesViewModel, onFollowRoute)
        }
    }
}
