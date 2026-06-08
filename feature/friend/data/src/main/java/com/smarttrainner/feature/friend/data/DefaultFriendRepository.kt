package com.smarttrainner.feature.friend.data

import com.smarttrainner.core.database.FriendDao
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.network.FriendNetworkApi
import com.smarttrainner.core.network.SendFriendRequestRequest
import com.smarttrainner.feature.friend.domain.FriendConnection
import com.smarttrainner.feature.friend.domain.FriendRepository
import com.smarttrainner.feature.friend.domain.FriendRequest
import com.smarttrainner.feature.friend.domain.FriendRequestDirection
import com.smarttrainner.feature.friend.domain.FriendRequestId
import com.smarttrainner.feature.friend.domain.FriendRequestStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@Singleton
class DefaultFriendRepository @Inject constructor(
    private val friendDao: FriendDao,
    private val activeSessionResolver: ActiveSessionResolver,
    private val friendNetworkApi: FriendNetworkApi
) : FriendRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeFriends(): Flow<List<FriendConnection>> =
        activeSessionResolver.observeSessionId().flatMapLatest { sessionId ->
            friendDao.observeConnections(sessionId).map { connections ->
                connections.map { it.toModel() }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeIncomingRequests(): Flow<List<FriendRequest>> =
        activeSessionResolver.observeSessionId().flatMapLatest { sessionId ->
            friendDao.observeRequests(
                ownerSessionId = sessionId,
                direction = FriendRequestDirection.Incoming.storageValue,
                status = FriendRequestStatus.Pending.storageValue
            ).map { requests ->
                requests.map { it.toModel() }
            }
        }

    override suspend fun refresh(): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        refreshForSession(sessionId)
    }

    override suspend fun sendRequest(nickname: String): Result<FriendRequest> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        val request = friendNetworkApi.sendFriendRequest(
            sessionId = sessionId,
            request = SendFriendRequestRequest(nickname = nickname.trim())
        ).data
        refreshAfterMutation(sessionId)
        request.toModel()
    }

    override suspend fun acceptRequest(id: FriendRequestId): Result<FriendConnection> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        val friend = friendNetworkApi.acceptFriendRequest(sessionId, id.value).data
        refreshAfterMutation(sessionId)
        friend.toEntity(sessionId).toModel()
    }

    override suspend fun declineRequest(id: FriendRequestId): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        friendNetworkApi.declineFriendRequest(sessionId, id.value)
        refreshAfterMutation(sessionId)
        Unit
    }

    override suspend fun removeFriend(friendSessionId: UserSessionId): Result<Unit> = runCatching {
        val sessionId = activeSessionResolver.sessionId()
        friendNetworkApi.removeFriend(sessionId, friendSessionId.value)
        refreshAfterMutation(sessionId)
        Unit
    }

    private suspend fun refreshAfterMutation(sessionId: String) {
        runCatching { refreshForSession(sessionId) }
    }

    private suspend fun refreshForSession(sessionId: String) = coroutineScope {
        val friendsDeferred = async { friendNetworkApi.getFriends(sessionId).data }
        val incomingDeferred = async {
            friendNetworkApi.getFriendRequests(
                sessionId = sessionId,
                box = FriendRequestDirection.Incoming.storageValue
            ).data
        }
        val outgoingDeferred = async {
            friendNetworkApi.getFriendRequests(
                sessionId = sessionId,
                box = FriendRequestDirection.Outgoing.storageValue
            ).data
        }

        val friends = friendsDeferred.await()
        val incoming = incomingDeferred.await()
        val outgoing = outgoingDeferred.await()

        friendDao.replaceConnections(
            ownerSessionId = sessionId,
            connections = friends.map { it.toEntity(sessionId) }
        )
        friendDao.replaceRequests(
            ownerSessionId = sessionId,
            direction = FriendRequestDirection.Incoming.storageValue,
            requests = incoming.map { it.toEntity(sessionId, FriendRequestDirection.Incoming) }
        )
        friendDao.replaceRequests(
            ownerSessionId = sessionId,
            direction = FriendRequestDirection.Outgoing.storageValue,
            requests = outgoing.map { it.toEntity(sessionId, FriendRequestDirection.Outgoing) }
        )
    }
}
