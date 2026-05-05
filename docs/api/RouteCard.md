# RouteCard Component

> Card displaying route recording/following controls

RouteCard shows the current route status and provides controls for starting/stopping recording or following.

## Overview

**Location**: `app/src/main/java/com/ameraldo/radar/ui/screens/radar/RouteCard.kt`

## Features

### Recording Mode
- Shows "Stop Recording" button (red)
- Triggers confirmation dialog
- After stopping, shows save dialog for route name

### Following Mode
- Shows "Stop Following" button (red)
- Triggers confirmation dialog
- Completes when all points are reached

### Idle Mode
- Shows "Start Recording" button (primary color)
- Begins recording and navigates to Radar screen

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `locationViewModel` | `LocationViewModel` | For starting/stopping recording |
| `isRecording` | `Boolean` | Whether currently recording |
| `isFollowing` | `Boolean` | Whether currently following a route |
| `onFollowingComplete` | `() -> Unit` | Callback when following is stopped |
| `pendingStopAction` | `StopAction?` | Stop action from notification bar |
| `onStopActionHandled` | `() -> Unit` | Callback after dialog is handled |

## Dialogs

### Stop Recording Confirmation
- Title: "Stop Recording?"
- Message: "Are you sure you want to stop recording?"
- On confirm: Stop recording → Show save dialog
- On dismiss: Clear pending action

### Stop Following Confirmation
- Title: "Stop Following?"
- Message: "Are you sure you want to stop following route?"
- On confirm: Stop following → Clear pending action
- On dismiss: Clear pending action

### Save Route Dialog
- Text field for route name (pre-filled with current name)
- "Save" button: Saves route with new name
- "Delete" button: Discards route (deletes from database)

## Related Documentation

- [LocationViewModel](../api/LocationViewModel.md) - Recording/following control
- [SaveRouteDialog](#save-route-dialog) - Save dialog component
- [RadarScreen](../screens/radar.md) - Parent screen
