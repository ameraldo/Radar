package com.ameraldo.radar.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Insert
    suspend fun insertRoute(route: RouteEntity): Long
    @Update
    suspend fun updateRoute(route: RouteEntity)
    @Query("DELETE FROM routes WHERE id = :routeId")
    suspend fun deleteRoute(routeId: Long)
    @Query("SELECT * FROM routes ORDER BY startTime DESC") fun getAllRoutes(): Flow<List<RouteEntity>>
    @Query("SELECT * FROM routes WHERE id = :routeId") suspend fun getRouteById(routeId: Long): RouteEntity?

    @Insert suspend fun insertPoints(points: List<RecordedPointEntity>)
    @Query("SELECT * FROM recorded_points WHERE routeId = :routeId ORDER BY sequenceNumber")
    fun getPointsForRoute(routeId: Long): Flow<List<RecordedPointEntity>>
    @Query("DELETE FROM recorded_points WHERE routeId = :routeId") suspend fun deletePointsForRoute(routeId: Long)
}