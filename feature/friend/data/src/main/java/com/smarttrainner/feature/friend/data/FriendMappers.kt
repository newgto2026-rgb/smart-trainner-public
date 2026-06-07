package com.smarttrainner.feature.friend.data

import com.smarttrainner.core.database.FriendConnectionEntity
import com.smarttrainner.core.database.FriendRequestEntity
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.network.FriendDto
import com.smarttrainner.core.network.FriendRequestDto
import com.smarttrainner.core.network.SocialUserDto
import com.smarttrainner.feature.friend.domain.FriendConnection
import com.smarttrainner.feature.friend.domain.FriendConnectionId
import com.smarttrainner.feature.friend.domain.FriendRequest
import com.smarttrainner.feature.friend.domain.FriendRequestDirection
import com.smarttrainner.feature.friend.domain.FriendRequestId
import com.smarttrainner.feature.friend.domain.FriendRequestStatus
import com.smarttrainner.feature.friend.domain.SocialUser

internal fun FriendDto.toEntity(ownerSessionId: String): FriendConnectionEntity =
    FriendConnectionEntity(
        ownerSessionId = ownerSessionId,
        id = id,
        friendSessionId = friend.sessionId,
        friendNickname = friend.nickname,
        friendDisplayName = friend.displayName,
        friendAvatarUrl = friend.avatarUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

internal fun FriendConnectionEntity.toModel(): FriendConnection =
    FriendConnection(
        id = FriendConnectionId(id),
        friend = SocialUser(
            sessionId = UserSessionId(friendSessionId),
            displayName = friendDisplayName,
            nickname = friendNickname,
            avatarUrl = friendAvatarUrl
        ),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

internal fun FriendRequestDto.toEntity(
    ownerSessionId: String,
    direction: FriendRequestDirection
): FriendRequestEntity =
    FriendRequestEntity(
        ownerSessionId = ownerSessionId,
        id = id,
        requesterSessionId = requester.sessionId,
        requesterNickname = requester.nickname,
        requesterDisplayName = requester.displayName,
        requesterAvatarUrl = requester.avatarUrl,
        receiverSessionId = receiver.sessionId,
        receiverNickname = receiver.nickname,
        receiverDisplayName = receiver.displayName,
        receiverAvatarUrl = receiver.avatarUrl,
        direction = direction.storageValue,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        respondedAt = respondedAt
    )

internal fun FriendRequestDto.toModel(): FriendRequest =
    FriendRequest(
        id = FriendRequestId(id),
        requester = requester.toModel(),
        receiver = receiver.toModel(),
        status = status.toFriendRequestStatus(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        respondedAt = respondedAt
    )

internal fun FriendRequestEntity.toModel(): FriendRequest =
    FriendRequest(
        id = FriendRequestId(id),
        requester = SocialUser(
            sessionId = UserSessionId(requesterSessionId),
            displayName = requesterDisplayName,
            nickname = requesterNickname,
            avatarUrl = requesterAvatarUrl
        ),
        receiver = SocialUser(
            sessionId = UserSessionId(receiverSessionId),
            displayName = receiverDisplayName,
            nickname = receiverNickname,
            avatarUrl = receiverAvatarUrl
        ),
        status = status.toFriendRequestStatus(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        respondedAt = respondedAt
    )

private fun SocialUserDto.toModel(): SocialUser =
    SocialUser(
        sessionId = UserSessionId(sessionId),
        displayName = displayName,
        nickname = nickname,
        avatarUrl = avatarUrl
    )

internal val FriendRequestDirection.storageValue: String
    get() = when (this) {
        FriendRequestDirection.Incoming -> "incoming"
        FriendRequestDirection.Outgoing -> "outgoing"
    }

internal fun String.toFriendRequestStatus(): FriendRequestStatus =
    when (uppercase()) {
        "ACCEPTED" -> FriendRequestStatus.Accepted
        "DECLINED",
        "REJECTED" -> FriendRequestStatus.Declined
        else -> FriendRequestStatus.Pending
    }

internal val FriendRequestStatus.storageValue: String
    get() = when (this) {
        FriendRequestStatus.Pending -> "PENDING"
        FriendRequestStatus.Accepted -> "ACCEPTED"
        FriendRequestStatus.Declined -> "DECLINED"
    }
