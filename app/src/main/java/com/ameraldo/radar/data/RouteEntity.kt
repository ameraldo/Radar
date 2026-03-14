package com.ameraldo.radar.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startTime: Long,
    val endTime: Long? = null,
    val pointCount: Int = 0
)

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
