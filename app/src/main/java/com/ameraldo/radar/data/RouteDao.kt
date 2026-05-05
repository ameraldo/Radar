package com.ameraldo.radar.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for routes and GPS points.
 *
 * Provides database operations using Room.
 * - Write operations (insert/update/delete) are suspending functions
 * - Read operations return [Flow] for reactive updates when data changes
 */
@Dao
interface RouteDao {
    /**
     * Inserts a new route into the database.
     *
     * @param route RouteEntity to insert
     * @return Generated ID of the inserted route
     */
    @Insert
    suspend fun insertRoute(route: RouteEntity): Long

    /**
     * Updates an existing route.
     *
     * @param route RouteEntity with updated values (must have valid id)
     */
    @Update
    suspend fun updateRoute(route: RouteEntity)

    /**
     * Deletes a route and all its associated points.
     *
     * @param routeId ID of the route to delete
     */
    @Query("DELETE FROM routes WHERE id = :routeId")
    suspend fun deleteRoute(routeId: Long)

    /**
     * Gets all routes, sorted by start time (newest first).
     *
     * @return Flow emitting list of RouteEntity whenever data changes
     */
    @Query("SELECT * FROM routes ORDER BY startTime DESC")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    /**
     * Gets a specific route by ID.
     *
     * @param routeId ID of the route to retrieve
     * @return RouteEntity or null if not found
     */
    @Query("SELECT * FROM routes WHERE id = :routeId")
    suspend fun getRouteById(routeId: Long): RouteEntity?

    /**
     * Inserts multiple GPS points into the database.
     * Used for batch insertion during route recording.
     *
     * @param points List of RecordedPointEntity to insert
     */
    @Insert
    suspend fun insertPoints(points: List<RecordedPointEntity>)

    /**
     * Gets all points for a specific route, ordered by sequence number.
     *
     * @param routeId ID of the route
     * @return Flow emitting list of RecordedPointEntity
     */
    @Query("SELECT * FROM recorded_points WHERE routeId = :routeId ORDER BY sequenceNumber")
    fun getPointsForRoute(routeId: Long): Flow<List<RecordedPointEntity>>

    /**
     * Deletes all points for a specific route.
     *
     * @param routeId ID of the route
     */
    @Query("DELETE FROM recorded_points WHERE routeId = :routeId")
    suspend fun deletePointsForRoute(routeId: Long)
}