# RouteCard Component

> Card displaying route recording/following controls

RouteCard provides the action button for starting or stopping recording and following. It does not display route metadata.

## Overview

**Location**: `app/src/main/java/com/ameraldo/radar/ui/screens/radar/RouteCard.kt`

## Features

### Recording Mode
- Shows a red "Stop Recording" button.
- Triggers a stop-confirmation dialog.
- After stopping, opens the save-route dialog.

### Following Mode
- Shows a red "Stop Following" button.
- Triggers a stop-confirmation dialog.
- Calls `onFollowingComplete()` when confirmed.
- Automatic completion when all points are reached is handled by the service, not by this card.

### Idle Mode
- Shows a primary-colored "Start Recording" button.
- Starts recording directly.
- Navigation to the Radar screen is handled by the Home screen action.

### What the Card Does Not Show
- No route name.
- No point count.
- No recording duration.
- No start-following action.
- No floating action button.

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `locationViewModel` | `LocationViewModel` | For starting/stopping recording and saving/deleting the route |
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
- Text field for the route name.
- Prefill removes the `(In Progress)` prefix from `currentRouteName` and trims whitespace.
- If no route name is available, the fallback is `Route <currentTimeMillis>`.
- "Save" button: Saves the route with the edited name.
- "Delete" button: Discards the route (deletes it from the database).

## Related Documentation

- [LocationViewModel](../api/LocationViewModel.md) - Recording/following control
- [SaveRouteDialog](#save-route-dialog) - Save dialog component
- [RadarScreen](../screens/radar.md) - Parent screen
