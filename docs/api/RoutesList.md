# RoutesList Component

> Displays a list of saved routes with follow/delete actions.

RoutesList shows all saved routes in a LazyColumn with the ability to follow or delete each route.

## Overview

**Location**: `app/src/main/java/com/ameraldo/radar/ui/screens/routes/RoutesList.kt`

## Features

### Route List
- Scrollable list using `LazyColumn`
- Each item shows:
  - Route name
  - Formatted date/time
  - Point count
  - Delete button (trash icon)
- Tapping a route starts following it

### Empty State
- Shows "No Saved Routes" message
- Displays helpful text: "Your saved routes will appear here."

### Delete Confirmation
- Uses `ConfirmationDialog` for delete confirmation
- Route ID passed to dialog via `rememberSavable`

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `routesViewModel` | `RouteViewModel` | For accessing saved routes |
| `onFollowRoute` | `(Long) -> Unit` | Callback with route ID when user wants to follow |

## Sub-Components

| Component | Description |
|-----------|-------------|
| `RoutesListItem` | Single route item with name, date, points |
| `ConfirmationDialog` | Delete confirmation dialog |

## Route Item Display

Each `RoutesListItem` shows:
- **Route name**: Bold title
- **Date**: Formatted as "MMM dd, yyyy HH:mm"
- **Point count**: "X points"
- **Delete button**: Red trash icon

## Related Documentation

- [RouteViewModel](../api/RouteViewModel.md) - Route list management
- [RouteEntity](../api/RouteEntity.md) - Data model
- [Routes Screen](../screens/routes.md) - Parent screen
- [ConfirmationDialog](ConfirmationDialog.md) - Delete confirmation
