# RouteViewModel

> Manages saved routes list

RouteViewModel provides a reactive list of all saved routes from the Room database.

## Overview

**Package**: `com.ameraldo.radar.viewmodel`

**Key Responsibilities**:
- Expose list of all saved routes
- Delete routes from database

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`RouteViewModel.kt`](../../app/src/main/java/com/ameraldo/radar/viewmodel/RouteViewModel.kt)

## State Properties

| Property | Type | Description |
|----------|------|-------------|
| `routes` | `StateFlow<List<RouteEntity>>` | All saved routes, sorted by start time (newest first) |

## Public Methods

| Method | Description |
|--------|-------------|
| `deleteRoute(routeId: Long)` | Delete a route by ID |

## Related Documentation

- [RouteDao](RouteDao.md) - Database access
- [RouteEntity](RouteEntity.md) - Database entity