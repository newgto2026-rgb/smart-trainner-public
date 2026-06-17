package com.smarttrainner.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomExerciseDao {
    @Query(
        """
        SELECT * FROM custom_exercises
        WHERE ownerSessionId = :ownerSessionId
        AND archivedAt IS NULL
        ORDER BY updatedAt DESC
        """
    )
    fun observeActiveForOwner(ownerSessionId: String): Flow<List<CustomExerciseEntity>>

    @Query(
        """
        SELECT * FROM custom_exercises
        WHERE ownerSessionId = :ownerSessionId
        AND syncState <> 'synced'
        ORDER BY updatedAt ASC
        """
    )
    suspend fun pendingSyncForOwner(ownerSessionId: String): List<CustomExerciseEntity>

    @Query(
        """
        SELECT * FROM custom_exercises
        WHERE ownerSessionId = :ownerSessionId
        AND id = :id
        """
    )
    suspend fun getById(ownerSessionId: String, id: String): CustomExerciseEntity?

    @Query(
        """
        SELECT COUNT(*) FROM custom_exercises
        WHERE ownerSessionId = :ownerSessionId
        AND id = :id
        """
    )
    suspend fun countById(ownerSessionId: String, id: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CustomExerciseEntity)

    @Query(
        """
        UPDATE custom_exercises
        SET syncState = :syncState
        WHERE ownerSessionId = :ownerSessionId
        AND id = :id
        """
    )
    suspend fun updateSyncState(ownerSessionId: String, id: String, syncState: String)

    @Query(
        """
        UPDATE custom_exercises
        SET archivedAt = :archivedAt,
            updatedAt = :archivedAt,
            syncState = 'pending_delete'
        WHERE ownerSessionId = :ownerSessionId
        AND id = :id
        """
    )
    suspend fun markPendingArchive(ownerSessionId: String, id: String, archivedAt: String): Int

    @Query(
        """
        SELECT DISTINCT routine.id FROM custom_routines AS routine
        INNER JOIN custom_routine_days AS day
            ON day.routineId = routine.id
        INNER JOIN custom_routine_exercises AS exercise
            ON exercise.dayId = day.id
        WHERE routine.sessionId = :ownerSessionId
        AND routine.syncState <> 'pending_delete'
        AND exercise.exerciseId = :exerciseId
        """
    )
    suspend fun routineIdsReferencingExercise(ownerSessionId: String, exerciseId: String): List<String>

    @Query(
        """
        DELETE FROM custom_routine_exercises
        WHERE exerciseId = :exerciseId
        AND dayId IN (
            SELECT day.id FROM custom_routine_days AS day
            INNER JOIN custom_routines AS routine
                ON routine.id = day.routineId
            WHERE routine.sessionId = :ownerSessionId
        )
        """
    )
    suspend fun deleteRoutineExerciseReferences(ownerSessionId: String, exerciseId: String): Int

    @Query(
        """
        UPDATE custom_routines
        SET syncState = CASE
                WHEN syncState = 'pending_delete' THEN syncState
                ELSE 'pending_upsert'
            END,
            updatedAt = :updatedAt
        WHERE sessionId = :ownerSessionId
        AND id IN (:routineIds)
        """
    )
    suspend fun markRoutinesPendingUpsert(ownerSessionId: String, routineIds: List<String>, updatedAt: String): Int

    @Query(
        """
        SELECT id FROM workout_logs
        WHERE sessionId = :ownerSessionId
        AND exerciseId = :exerciseId
        """
    )
    suspend fun workoutLogIdsForExercise(ownerSessionId: String, exerciseId: String): List<Long>

    @Query("DELETE FROM workout_set_logs WHERE workoutLogId IN (:workoutLogIds)")
    suspend fun deleteSetLogsByWorkoutLogIds(workoutLogIds: List<Long>)

    @Query("DELETE FROM workout_logs WHERE id IN (:workoutLogIds)")
    suspend fun deleteWorkoutLogsByIds(workoutLogIds: List<Long>)

    @Transaction
    suspend fun markPendingArchiveAndRemoveReferences(
        ownerSessionId: String,
        id: String,
        archivedAt: String
    ): Int {
        val routineIds = routineIdsReferencingExercise(ownerSessionId, id)
        val updatedRows = markPendingArchive(ownerSessionId, id, archivedAt)
        if (updatedRows == 0) return 0

        deleteRoutineExerciseReferences(ownerSessionId, id)
        if (routineIds.isNotEmpty()) {
            markRoutinesPendingUpsert(ownerSessionId, routineIds, archivedAt)
        }

        val workoutLogIds = workoutLogIdsForExercise(ownerSessionId, id)
        if (workoutLogIds.isNotEmpty()) {
            deleteSetLogsByWorkoutLogIds(workoutLogIds)
            deleteWorkoutLogsByIds(workoutLogIds)
        }
        return updatedRows
    }
}
