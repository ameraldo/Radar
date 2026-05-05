# RouteDao

> Database access object for routes and recorded points

RouteDao provides the data access layer for routes and GPS points using Room database.

## Overview

**Package**: `com.ameraldo.radar.data`

**Key Responsibilities**:
- CRUD operations for routes
- CRUD operations for recorded points
- Query routes and points with reactive Flows

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`RouteDao.kt`](../../app/src/main/java/com/ameraldo/radar/data/RouteDao.kt)

## Methods

### Route Operations
| Method | Description |
|--------|-------------|
| `insertRoute(route)` | Insert new route, return ID |
| `updateRoute(route)` | Update existing route |
| `deleteRoute(routeId)` | Delete route by ID |
| `getAllRoutes()` | Get all routes (Flow) |
| `getRouteById(routeId)` | Get route by ID (suspend) |

### Point Operations
| Method | Description |
|--------|-------------|
| `insertPoints(points)` | Insert multiple points |
| `getPointsForRoute(routeId)` | Get points for route (Flow) |
| `deletePointsForRoute(routeId)` | Delete all points for route |

## Related Documentation

- [RouteEntity](RouteEntity.md) - Database entities