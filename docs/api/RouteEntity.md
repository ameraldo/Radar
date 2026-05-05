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
| `name` | String | Route display name |
| `startTime` | Long | Unix timestamp of route start |
| `endTime` | Long? | Unix timestamp of route end (null if recording) |
| `pointCount` | Int | Number of recorded points |

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
    val timestamp: Long,
    val sequenceNumber: Int
)
```

| Property | Type | Description |
|----------|------|-------------|
| `id` | Long | Primary key, auto-generated |
| `routeId` | Long? | Foreign key to parent route |
| `latitude` | Double | GPS latitude |
| `longitude` | Double | GPS longitude |
| `timestamp` | Long | Unix timestamp of point |
| `sequenceNumber` | Int | Order in the route |

## Relationships

- Each `RouteEntity` can have many `RecordedPointEntity` (one-to-many)
- Deleting a route automatically deletes all its points (CASCADE)

## Related Documentation

- [RouteDao](RouteDao.md) - Database operations
- [AppDatabase](AppDatabase.md) - Database setup
- [LocationViewModel](LocationViewModel.md) - Recording points