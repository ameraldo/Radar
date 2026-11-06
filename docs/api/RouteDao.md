# RouteDao

> Database access object for routes and recorded points

RouteDao provides the data access layer for routes and GPS points using the Room database.

## Overview

**Package**: `com.ameraldo.radar.data`

**Key Responsibilities**:
- Insert, update, delete, and query routes
- Batch-insert recorded points
- Query points for a route with a reactive Flow
- Delete all points for a route

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`RouteDao.kt`](../../app/src/main/java/com/ameraldo/radar/data/RouteDao.kt)

## Methods

### Route Operations

| Method | Description |
|--------|-------------|
| `insertRoute(route)` | Insert a new route, return its generated ID |
| `updateRoute(route)` | Update an existing route (must have a valid ID) |
| `deleteRoute(routeId)` | Delete a route by ID; associated points are removed by CASCADE |
| `getAllRoutes()` | Get all routes as a `Flow`, ordered by `startTime DESC` (newest first) |
| `getRouteById(routeId)` | Get a single route by ID (suspend, returns `null` if absent) |

### Point Operations

| Method | Description |
|--------|-------------|
| `insertPoints(points)` | Batch-insert a list of recorded points |
| `getPointsForRoute(routeId)` | Get points for a route as a `Flow`, ordered by `sequenceNumber` |
| `deletePointsForRoute(routeId)` | Delete all points for a route |

There is no point update or point-by-ID query. Points are inserted in batches, read
as a route-ordered `Flow`, or deleted with their route.

## Orphaned Points

`RecordedPointEntity.routeId` is nullable. If a point is inserted before the route ID
has been assigned, it is stored with `routeId = null` and is not reachable through
`getPointsForRoute()`. See [Data Flow](../architecture/data-flow.md) for the recording
sequence that can produce this situation.

## Related Documentation

- [RouteEntity](RouteEntity.md) - Database entities
- [AppDatabase](AppDatabase.md) - Database setup
