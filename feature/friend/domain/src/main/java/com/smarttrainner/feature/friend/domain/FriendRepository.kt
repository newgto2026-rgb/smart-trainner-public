package com.smarttrainner.feature.friend.domain

import com.smarttrainner.core.model.UserSessionId
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    fun observeFriends(): Flow<List<FriendConnection>>
    fun observeIncomingRequests(): Flow<List<FriendRequest>>
    suspend fun refresh(): Result<Unit>
    suspend fun sendRequest(nickname: String): Result<FriendRequest>
    suspend fun acceptRequest(id: FriendRequestId): Result<FriendConnection>
    suspend fun declineRequest(id: FriendRequestId): Result<Unit>
    suspend fun removeFriend(friendSessionId: UserSessionId): Result<Unit>
}
