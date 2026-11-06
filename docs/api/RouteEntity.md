# RouteEntity

> Room database entities for routes and recorded points

Defines the database schema for storing GPS routes and their recorded points.

## Overview

**Package**: `com.ameraldo.radar.data`

**Entities:**
- `RouteEntity` - Represents a saved GPS route
- `RecordedPointEntity` - Represents a single GPS point in a route

## RouteEntity

```kotlin
@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startTime: Long,
    val endTime: Long? = null,
    val pointCount: Int = 0
)
```

| Property | Type | Description |
|----------|------|-------------|
| `id` | Long | Primary key, auto-generated |
| `name` | String | Route display name; auto-generated as `(In Progress) Route yyyy-MM-dd HH:mm` and user-renamable |
| `startTime` | Long | Unix timestamp in milliseconds when recording started |
| `endTime` | Long? | Unix timestamp in milliseconds when recording stopped (null while recording) |
| `pointCount` | Int | Number of recorded points; finalized when recording stops |

## RecordedPointEntity

```kotlin
@Entity(
    tableName = "recorded_points",
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routeId")]
)
data class RecordedPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: Long?,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    val timestamp: Long,
    val sequenceNumber: Int
)
```

| Property | Type | Description |
|----------|------|-------------|
| `id` | Long | Primary key, auto-generated |
| `routeId` | Long? | Foreign key to parent route; nullable, so a point can exist without an assigned route |
| `latitude` | Double | GPS latitude |
| `longitude` | Double | GPS longitude |
| `elevation` | `Double?` | Elevation in meters above sea level (null if unavailable) |
| `timestamp` | Long | Unix timestamp in milliseconds when the point was recorded |
| `sequenceNumber` | Int | Order in the route |

## Relationships

- Each `RouteEntity` can have many `RecordedPointEntity` (one-to-many)
- Deleting a route automatically deletes all its points (CASCADE)
- A point with `routeId = null` is not associated with any route and will not appear in that route's point list, point count, or GPX export

## Related Documentation

- [RouteDao](RouteDao.md) - Database operations
- [AppDatabase](AppDatabase.md) - Database setup
- [LocationViewModel](LocationViewModel.md) - Recording points
