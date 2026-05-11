package com.ameraldo.radar.ui.screens.routes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ameraldo.radar.data.RouteEntity
import com.ameraldo.radar.ui.components.ConfirmationDialog
import com.ameraldo.radar.viewmodel.RouteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale
import kotlinx.coroutines.launch

/**
 * Displays a list of saved routes with follow/delete actions.
 *
 * Each item shows route name, date, point count, and delete button.
 * Clicking a route starts following it.
 *
 * @param routeViewModel For accessing saved routes
 * @param onFollowRoute Callback with route ID when user wants to follow
 */
@Composable
fun RoutesList(
    routeViewModel: RouteViewModel,
    onFollowRoute: (Long) -> Unit
) {
    val routes by routeViewModel.routes.collectAsState()

    var routeIdToDelete: Long? by rememberSaveable { mutableStateOf(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(routes) { route ->
            RoutesListItem(
                route = route,
                routeViewModel = routeViewModel,
                onDelete = { routeId -> routeIdToDelete = routeId },
                onClick = { onFollowRoute(route.id) }
            )
        }
    }

    routeIdToDelete?.let {
        ConfirmationDialog(
            title = "Delete Route?",
            message = "Are you sure you want to delete the route?",
            onConfirm = {
                routeViewModel.deleteRoute(routeIdToDelete!!)
                routeIdToDelete = null
            },
            onDismiss = { routeIdToDelete = null }
        )
    }
}

/**
 * Displays a single route item in the list.
 *
 * Shows route name, formatted date, point count, and action buttons.
 * Clicking the item triggers following the route.
 * Download button triggers GPX export via Storage Access Framework.
 *
 * @param route The route data to display
 * @param routeViewModel ViewModel for GPX export action
 * @param onClick Callback when item is clicked (start following)
 * @param onDelete Callback with route ID when delete is requested
 */
@Composable
private fun RoutesListItem(
    route: RouteEntity,
    routeViewModel: RouteViewModel,
    onClick: () -> Unit,
    onDelete: (Long) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", LocalLocale.current.platformLocale)
    val formattedDate = dateFormat.format(Date(route.startTime))

    val coroutineScope = rememberCoroutineScope()
    val exportLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("application/gpx")) {
        uri: Uri? -> if (uri != null) coroutineScope.launch { routeViewModel.exportRoute(route.id, uri) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${route.pointCount} points",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = {
                    val safeName = route.name
                        .replace(Regex("\\s+"), "_")
                        .replace(Regex("[^a-zA-Z0-9_.\\-]"), "")

                    exportLauncher.launch("${safeName}.gpx")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download Route",
                )
            }
            IconButton(onClick = { onDelete(route.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Route",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}