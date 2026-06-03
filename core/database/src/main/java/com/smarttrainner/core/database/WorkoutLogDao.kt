package com.smarttrainner.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {
    @Transaction
    @Query(
        """
        SELECT * FROM workout_logs
        WHERE sessionId = :sessionId
        AND performedDate BETWEEN :startDate AND :endDate
        ORDER BY performedAt DESC
        """
    )
    fun observeBetween(
        sessionId: String,
        startDate: String,
        endDate: String
    ): Flow<List<WorkoutLogWithSets>>

    @Transaction
    @Query(
        """
        SELECT * FROM workout_logs
        WHERE sessionId = :sessionId
        ORDER BY performedAt DESC
        """
    )
    fun observeAll(sessionId: String): Flow<List<WorkoutLogWithSets>>

    @Transaction
    @Query(
        """
        SELECT * FROM workout_logs
        WHERE sessionId = :sessionId
        AND exerciseId = :exerciseId
        ORDER BY performedAt DESC
        LIMIT 1
        """
    )
    suspend fun latestByExercise(
        sessionId: String,
        exerciseId: String
    ): WorkoutLogWithSets?

    @Transaction
    @Query(
        """
        SELECT * FROM workout_logs
        WHERE sessionId = :sessionId
        AND plannedExerciseId = :plannedExerciseId
        ORDER BY performedAt DESC
        LIMIT 1
        """
    )
    suspend fun latestByPlannedExercise(
        sessionId: String,
        plannedExerciseId: String
    ): WorkoutLogWithSets?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: WorkoutLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetLogs(setLogs: List<WorkoutSetLogEntity>)

    @Transaction
    suspend fun upsertWithSets(log: WorkoutLogEntity, setLogs: List<WorkoutSetLogEntity>) {
        val workoutLogId = upsert(log)
        insertSetLogs(setLogs.map { it.copy(workoutLogId = workoutLogId) })
    }

    @Query(
        """
        SELECT id FROM workout_logs
        WHERE sessionId = :sessionId
        AND (
            plannedExerciseId IN (:plannedExerciseIds)
            OR plannedExerciseId LIKE :additionalExerciseIdPrefixPattern
        )
        """
    )
    suspend fun routineDayWorkoutLogIds(
        sessionId: String,
        plannedExerciseIds: List<String>,
        additionalExerciseIdPrefixPattern: String
    ): List<Long>

    @Query("DELETE FROM workout_set_logs WHERE workoutLogId IN (:workoutLogIds)")
    suspend fun deleteSetLogsByWorkoutLogIds(workoutLogIds: List<Long>)

    @Query("DELETE FROM workout_logs WHERE id IN (:workoutLogIds)")
    suspend fun deleteWorkoutLogsByIds(workoutLogIds: List<Long>)

    @Transaction
    suspend fun deleteRoutineDayLogs(
        sessionId: String,
        plannedExerciseIds: List<String>,
        additionalExerciseIdPrefixPattern: String
    ) {
        val workoutLogIds = routineDayWorkoutLogIds(
            sessionId = sessionId,
            plannedExerciseIds = plannedExerciseIds,
            additionalExerciseIdPrefixPattern = additionalExerciseIdPrefixPattern
        )
        if (workoutLogIds.isNotEmpty()) {
            deleteSetLogsByWorkoutLogIds(workoutLogIds)
            deleteWorkoutLogsByIds(workoutLogIds)
        }
    }
}
