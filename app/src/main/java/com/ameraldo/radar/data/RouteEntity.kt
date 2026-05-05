package com.ameraldo.radar.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a saved GPS route.
 *
 * @property id Auto-generated primary key
 * @property name User-provided or auto-generated route name
 * @property startTime Timestamp (ms) when recording started
 * @property endTime Timestamp (ms) when recording stopped (null if in progress)
 * @property pointCount Total points recorded (updated when recording stops)
 */
@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startTime: Long,
    val endTime: Long? = null,
    val pointCount: Int = 0
)

/**
 * Represents a single GPS point recorded during a route.
 *
 * @property id Auto-generated primary key
 * @property routeId Foreign key to [RouteEntity] (CASCADE delete when route removed)
 * @property latitude Latitude in degrees
 * @property longitude Longitude in degrees
 * @property timestamp Timestamp (ms) when point was recorded
 * @property sequenceNumber Order in the route (0 = start, increments by 1)
 */
@Entity(
    tableName = "recorded_points",
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE // Auto-delete points when route is deleted
        )
    ],
    indices = [Index("routeId")] // Index for query performance on routeId lookups
)
data class RecordedPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: Long?,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val sequenceNumber: Int
)
