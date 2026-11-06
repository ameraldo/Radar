# RoutesList Component

> Displays a list of saved routes with follow/download/delete actions.

RoutesList shows all saved routes in a `LazyColumn` and starts following when a route card is tapped.

## Overview

**Location**: `app/src/main/java/com/ameraldo/radar/ui/screens/routes/RoutesList.kt`

## Features

### Route List
- Scrollable list using `LazyColumn`
- Each item shows:
  - Route name
  - Formatted date/time (`MMM dd, yyyy HH:mm`)
  - Point count (`"N points"`)
  - Download button (download icon)
  - Delete button (red trash icon)
- Tapping the whole card starts following that route
- The empty state is handled by [RoutesScreen](../screens/routes.md), not by `RoutesList`

### Delete Confirmation
- Uses `ConfirmationDialog` for delete confirmation
- Route ID used for the dialog is stored with `rememberSaveable`

### GPX Export
- Download button on each route item triggers the Storage Access Framework file picker
- User chooses the save location for the `.gpx` file
- Exported file follows GPX 1.1 format
- Filename is derived from the route name:
  1. Whitespace is replaced with `_`.
  2. Characters outside `[a-zA-Z0-9_.-]` are removed.
  3. `.gpx` is appended.

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `routeViewModel` | `RouteViewModel` | For accessing saved routes and exporting them |
| `onFollowRoute` | `(Long) -> Unit` | Callback with route ID when user wants to follow |

## Sub-Components

| Component | Description |
|-----------|-------------|
| `RoutesListItem` | Single route item with name, date, points, and action buttons |
| `ConfirmationDialog` | Delete confirmation dialog |

## Route Item Display

Each `RoutesListItem` shows:
- **Route name**: Bold title
- **Date**: Formatted as "MMM dd, yyyy HH:mm"
- **Point count**: "X points"
- **Download button**: GPX export (download icon)
- **Delete button**: Red trash icon (delete icon)

In-progress routes normally show `0 points` because `pointCount` is finalized when recording stops. **TODO:** Add a live point count or "Recording…" label; not implemented yet.

## Related Documentation

- [RouteViewModel](../api/RouteViewModel.md) - Route list management and GPX export
- [RouteEntity](../api/RouteEntity.md) - Data model
- [GpxUtils](../api/GpxUtils.md) - GPX file generation
- [Routes Screen](../screens/routes.md) - Parent screen
- [ConfirmationDialog](ConfirmationDialog.md) - Delete confirmation
