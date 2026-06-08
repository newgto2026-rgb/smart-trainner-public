package com.smarttrainner.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query(
        """
        SELECT *
        FROM friend_connections
        WHERE ownerSessionId = :ownerSessionId
        ORDER BY friendNickname COLLATE NOCASE ASC
        """
    )
    fun observeConnections(ownerSessionId: String): Flow<List<FriendConnectionEntity>>

    @Query(
        """
        SELECT *
        FROM friend_requests
        WHERE ownerSessionId = :ownerSessionId
        AND direction = :direction
        AND status = :status
        ORDER BY createdAt DESC
        """
    )
    fun observeRequests(
        ownerSessionId: String,
        direction: String,
        status: String
    ): Flow<List<FriendRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConnections(connections: List<FriendConnectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRequests(requests: List<FriendRequestEntity>)

    @Query("DELETE FROM friend_connections WHERE ownerSessionId = :ownerSessionId")
    suspend fun clearConnections(ownerSessionId: String)

    @Query(
        """
        DELETE FROM friend_requests
        WHERE ownerSessionId = :ownerSessionId
        AND direction = :direction
        """
    )
    suspend fun clearRequests(ownerSessionId: String, direction: String)

    @Transaction
    suspend fun replaceConnections(
        ownerSessionId: String,
        connections: List<FriendConnectionEntity>
    ) {
        clearConnections(ownerSessionId)
        upsertConnections(connections)
    }

    @Transaction
    suspend fun replaceRequests(
        ownerSessionId: String,
        direction: String,
        requests: List<FriendRequestEntity>
    ) {
        clearRequests(ownerSessionId, direction)
        upsertRequests(requests)
    }
}
