package com.ameraldo.radar.ui.screens.radar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.ameraldo.radar.ui.components.ConfirmationDialog
import com.ameraldo.radar.viewmodel.LocationViewModel
import com.ameraldo.radar.viewmodel.StopAction

@Composable
fun RouteCard(
    locationViewModel: LocationViewModel,
    isRecording: Boolean,
    isFollowing: Boolean,
    onFollowingComplete: () -> Unit,
    pendingStopAction: StopAction? = null,
    onStopActionHandled: () -> Unit
) {
    val currentRouteName by locationViewModel.currentRouteName.collectAsState()

    var showStopRecordingConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var showStopFollowingConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var showSaveRouteDialog by rememberSaveable { mutableStateOf(false) }

    // Trigger dialog when pendingStopAction is set (when pressing "Stop" from notification bar)
    LaunchedEffect(pendingStopAction) {
        when (pendingStopAction) {
            StopAction.RECORDING -> {
                showStopRecordingConfirmationDialog = true
            }
            StopAction.FOLLOWING -> {
                showStopFollowingConfirmationDialog = true
            }
            null -> { /* do nothing */ }
        }
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier                = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement   = Arrangement.spacedBy(12.dp)
        ) {
            when {
                isFollowing -> {
                    Button(
                        onClick  = { showStopFollowingConfirmationDialog = true },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Stop Following")
                    }
                }
                isRecording -> {
                    Button(
                        onClick  = { showStopRecordingConfirmationDialog = true },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Stop Recording")
                    }
                }
                else -> {
                    Button(
                        onClick = { locationViewModel.startRecording() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Recording")
                    }
                }

            }
        }
    }

    if (showStopRecordingConfirmationDialog) {
        ConfirmationDialog(
            title = "Stop Recording?",
            message = "Are you sure you want to stop recording?",
            onConfirm = {
                locationViewModel.stopRecording()
                showStopRecordingConfirmationDialog = false
                showSaveRouteDialog = true
                onStopActionHandled()
            },
            onDismiss = {
                showStopRecordingConfirmationDialog = false
                onStopActionHandled()
            }
        )
    }

    if (showStopFollowingConfirmationDialog) {
        ConfirmationDialog(
            title = "Stop Following?",
            message = "Are you sure you want to stop following route?",
            onConfirm = {
                onFollowingComplete()
                showStopFollowingConfirmationDialog = false
                onStopActionHandled()
            },
            onDismiss = {
                showStopFollowingConfirmationDialog = false
                onStopActionHandled()
            }
        )
    }

    if (showSaveRouteDialog) {
        SaveRouteDialog(
            currentRouteName = currentRouteName,
            onSave = {
                newName -> locationViewModel.saveRoute(newName)
                showSaveRouteDialog = false
            },
            onDismiss = {
                locationViewModel.deleteRoute()
                showSaveRouteDialog = false
            }
        )
    }
}

@Composable
private fun SaveRouteDialog(
    currentRouteName: String?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var editedName by remember(currentRouteName) {
        mutableStateOf(currentRouteName
            ?.removePrefix("(In Progress)") // Remove the in progress part assigned by the service.
            ?.trim()
            ?: "Route ${System.currentTimeMillis()}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Route") },
        text = {
            Column {
                Text(
                    text = "Enter a name for your route:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Route Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(editedName) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}