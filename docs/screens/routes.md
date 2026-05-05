# Routes Screen

> Displays and manages saved GPS routes

## Overview

The Routes Screen shows all saved GPS routes, allowing users to view, follow, or delete them.

## Location

**File**: `app/src/main/java/com.ameraldo.radar/ui/screens/routes/RoutesScreen.kt`

## Features

### Route List
- Displays all saved routes
- Sorted by start time (newest first)
- Each route shows:
  - Route name
  - Date/time recorded
  - Number of points
  - Duration (if ended)

### Empty State
- Shown when no routes saved
- Displays helpful message

### Route Selection
- Tap route to select it for following
- Navigates to Radar Screen in following mode

## UI Components

| Component | Description |
|-----------|-------------|
| [RoutesList](../api/RoutesList.md) | List of saved routes with actions |

## State Dependencies

| State | Source | Usage |
|-------|--------|-------|
| `routes` | RouteViewModel | List of saved routes |

## Route Item Display

```
┌─────────────────────────────────────┐
│ Route 2024-01-15 10:30              │
│ 150 points • 1.2 km • 45 min        │
└─────────────────────────────────────┘
```

Or when recording in progress:
```
┌─────────────────────────────────────┐
│ Route 2024-01-15 10:30 (Recording)  │
│ 45 points                           │
└─────────────────────────────────────┘
```

## Data Model

### RouteEntity

```kotlin
data class RouteEntity(
    val id: Long = 0,
    val name: String,              // "Route YYYY-MM-DD HH:MM"
    val startTime: Long,           // Unix timestamp
    val endTime: Long? = null,     // Null if recording in progress
    val pointCount: Int = 0        // Number of recorded points
)
```

## User Interactions

1. **View routes**: Browse saved routes list
2. **Follow route**: Tap route to start navigation
3. **Delete route**: Tap delete icon in RoutesList

## Storage

Routes stored in Room database:
- Table: `routes`
- Points stored in `recorded_points` table
- CASCADE delete removes points when route deleted

## Related Documentation

- [RouteViewModel](../api/RouteViewModel.md) - Route list management
- [RoutesList](../api/RoutesList.md) - Route list component
- [ConfirmationDialog](../api/ConfirmationDialog.md) - Delete confirmation
- [RouteDao](../api/RouteDao.md) - Database operations
- [RouteEntity](../api/RouteEntity.md) - Data model
