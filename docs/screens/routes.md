# Routes Screen

> Displays and manages saved GPS routes

## Overview

The Routes Screen shows all saved GPS routes, allowing users to follow, export, or delete them.

## Location

**File**: `app/src/main/java/com/ameraldo/radar/ui/screens/routes/RoutesScreen.kt`

## Features

### Screen Header
- Displays the title "Routes".
- Includes an Upload icon for import/export.
- The import/export action is **not implemented yet**; the button currently has no effect.

### Route List
- Displays all saved routes.
- Sorted by start time (newest first).
- Each route item shows:
  - Route name.
  - Formatted date/time (`MMM dd, yyyy HH:mm`).
  - Point count as `"N points"`.
- The whole card is clickable to start following.
- A download icon exports the route as GPX.
- A red delete icon opens a delete confirmation dialog.
- Duration and distance are not shown.

### Empty State
- Shown by `RoutesScreen` when no routes are saved.
- Displays:
  - "No Saved Routes"
  - "Your saved routes will appear here."

### Route Selection
- Tapping a route immediately starts following it.
- Navigates to the Radar Screen in following mode.
- There is no separate selection state.

### GPX Export
- Download button on each route item.
- Uses Storage Access Framework — the user chooses the save location.
- Exports the route as a GPX 1.1 XML file.
- Filename is derived from the route name:
  1. Whitespace is replaced with `_`.
  2. Characters outside `[a-zA-Z0-9_.-]` are removed.
  3. `.gpx` is appended.

## UI Components

| Component | Description |
|-----------|-------------|
| [RoutesList](../api/RoutesList.md) | List of saved routes with actions |

## State Dependencies

| State | Source | Usage |
|-------|--------|-------|
| `routes` | RouteViewModel | List of saved routes |

## Route Item Display

Saved route:

```
┌─────────────────────────────────────┐
│ Morning hike                        │
│ Jan 15, 2026 10:30                  │
│ 150 points                          │
│                  [download] [delete] │
└─────────────────────────────────────┘
```

Route still being recorded:

```
┌─────────────────────────────────────┐
│ (In Progress) Route 2026-01-15 10:30│
│ Jan 15, 2026 10:30                  │
│ 0 points                            │
│                  [download] [delete] │
└─────────────────────────────────────┘
```

In-progress routes normally show `0 points` because `pointCount` is written only when recording stops. **TODO:** Add a live point count or "Recording…" label; not implemented yet.

## Data Model

See [RouteEntity](../api/RouteEntity.md) for the full entity definitions. Key points:

- `name` is auto-generated as `(In Progress) Route yyyy-MM-dd HH:mm` and can be renamed in the save dialog.
- `startTime` and `endTime` are Unix timestamps in milliseconds.
- `endTime` is `null` while recording is in progress.
- `pointCount` is finalized when recording stops.

## User Interactions

1. **View routes**: Browse the saved routes list.
2. **Follow route**: Tap a route card to start following it.
3. **Export route**: Tap the download icon to save it as a GPX file.
4. **Delete route**: Tap the delete icon and confirm.
5. **Import/Export**: The header Upload button is a placeholder and is not implemented yet.

## Storage

Routes are stored in the Room database:
- Table: `routes`
- Points are stored in the `recorded_points` table
- CASCADE delete removes points when a route is deleted

## Related Documentation

- [RouteViewModel](../api/RouteViewModel.md) - Route list management and GPX export
- [RoutesList](../api/RoutesList.md) - Route list component
- [GpxUtils](../api/GpxUtils.md) - GPX file generation
- [ConfirmationDialog](../api/ConfirmationDialog.md) - Delete confirmation
- [RouteDao](../api/RouteDao.md) - Database operations
- [RouteEntity](../api/RouteEntity.md) - Data model
