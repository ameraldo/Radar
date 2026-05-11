package com.ameraldo.radar.ui.screens.routes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Routes",
                style = MaterialTheme.typography.headlineMedium,
            )
            IconButton(onClick = { /* upload action */ }) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = "Import/Export routes"
                )
            }
        }

        if (routes.isEmpty()) {
            // Show empty state
            Column(
                modifier = modifier.fillMaxSize(),
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
            Spacer(modifier = Modifier.height(16.dp)) // Vertical padding
            RoutesList(routesViewModel, onFollowRoute)
        }
    }
}
