# ConfirmationDialog Component#

> Reusable confirmation dialog with Yes/No buttons.

ConfirmationDialog provides a simple dialog with title, message, and Yes/No buttons. Used for stop recording, stop following, and delete route confirmations.

## Overview#

**Location**: `app/src/main/java/com/ameraldo/radar/ui/components/ConfirmationDialog.kt`

## Parameters#

| Parameter | Type | Description |
|-----------|------|-------------|
| `title` | `String` | Dialog title text |
| `message` | `String` | Dialog message/question text |
| `onConfirm` | `() -> Unit` | Callback when user confirms (Yes) |
| `onDismiss` | `() -> Unit` | Callback when user dismisses (No or outside tap) |

## Usage#

```kotlin
ConfirmationDialog(
    title = "Stop Recording?",
    message = "Are you sure you want to stop recording?",
    onConfirm = { /* stop recording */ },
    onDismiss = { /* clear pending action */ }
)
```

## Button Actions#

| Button | Action |
|-------|--------|
| **Yes** | Calls `onConfirm()` callback |
| **No** | Calls `onDismiss()` callback |

## Related Documentation#

- [RouteCard](RouteCard.md) - Uses dialog for stop confirmations
- [RoutesList](RoutesList.md) - Uses dialog for delete confirmations
- [LocationViewModel](../api/LocationViewModel.md) - Stop recording/following logic
