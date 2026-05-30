package com.smarttrainner.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomRoutineDao {
    @Transaction
    @Query(
        """
        SELECT * FROM custom_routines
        WHERE sessionId = :sessionId
        ORDER BY updatedAt DESC
        """
    )
    fun observeForSession(sessionId: String): Flow<List<CustomRoutineWithDays>>

    @Transaction
    @Query(
        """
        SELECT * FROM custom_routines
        WHERE id = :routineId AND sessionId = :sessionId
        """
    )
    suspend fun getById(sessionId: String, routineId: String): CustomRoutineWithDays?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutine(routine: CustomRoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<CustomRoutineDayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<CustomRoutineExerciseEntity>)

    @Query("DELETE FROM custom_routine_days WHERE routineId = :routineId")
    suspend fun deleteDaysForRoutine(routineId: String)

    @Query("DELETE FROM custom_routines WHERE id = :routineId AND sessionId = :sessionId")
    suspend fun deleteRoutine(sessionId: String, routineId: String): Int

    @Transaction
    suspend fun upsertFull(
        routine: CustomRoutineEntity,
        days: List<CustomRoutineDayWrite>
    ) {
        upsertRoutine(routine)
        deleteDaysForRoutine(routine.id)
        insertDays(days.map { it.day })
        insertExercises(days.flatMap { it.exercises })
    }
}
