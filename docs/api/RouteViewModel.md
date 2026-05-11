# RouteViewModel

> Manages saved routes list and GPX export

RouteViewModel provides a reactive list of all saved routes from the Room database and functionality to export routes as GPX files.

## Overview

**Package**: `com.ameraldo.radar.viewmodel`

**Key Responsibilities**:
- Expose list of all saved routes
- Delete routes from database
- Export routes as GPX 1.1 files

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
| `exportRoute(routeId: Long, uri: Uri)` | Export a route as GPX 1.1 to the given URI |

## Related Documentation

- [RouteDao](RouteDao.md) - Database access
- [RouteEntity](RouteEntity.md) - Database entity
- [GpxUtils](GpxUtils.md) - GPX file generation