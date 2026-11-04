# AppDatabase

> Room database for routes and recorded GPS points

AppDatabase is the Room database singleton used by the app. It stores saved routes
and their recorded points and exposes the [RouteDao](RouteDao.md) used by the
service and view models.

## Overview

**Package**: `com.ameraldo.radar.data`

**Key Responsibilities**:
- Declare the Room entities (`RouteEntity`, `RecordedPointEntity`)
- Provide the [`RouteDao`](RouteDao.md) accessor
- Own the thread-safe singleton database instance

## Source Documentation

For detailed API documentation, see the KDoc comments in:
- [`AppDatabase.kt`](../../app/src/main/java/com/ameraldo/radar/data/AppDatabase.kt)

## Database Configuration

```kotlin
@Database(
    entities = [RouteEntity::class, RecordedPointEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
}
```

| Setting | Value |
|---------|-------|
| Database name | `radar_database` |
| Version | `2` |
| Entities | `RouteEntity`, `RecordedPointEntity` |
| Schema export | Disabled (`exportSchema = false`) |
| Migration policy | `fallbackToDestructiveMigration(true)` |

Destructive migration is enabled because no migrations are defined yet: on a
version mismatch the database is recreated rather than migrated.

## Singleton

`getInstance(context)` returns the shared instance. Access is guarded with
`@Volatile` and `synchronized`, and the builder uses `applicationContext` to
avoid leaking an Activity.

```kotlin
val database = AppDatabase.getInstance(context)
val routeDao = database.routeDao()
```

Callers currently include:
- [`LocationService`](LocationService.md) — recording and following reads/writes
- [`LocationViewModel`](LocationViewModel.md) — route rename/delete
- [`RouteViewModel`](RouteViewModel.md) — route list, delete, GPX export

## Related Documentation

- [RouteDao](RouteDao.md) - Data access operations
- [RouteEntity](RouteEntity.md) - Database entities
- [Architecture Overview](../architecture/overview.md) - Data layer
- [Data Flow](../architecture/data-flow.md) - Database interaction
