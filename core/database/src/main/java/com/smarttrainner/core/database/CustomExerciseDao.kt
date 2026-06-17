package com.smarttrainner.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}
