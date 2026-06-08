package com.smarttrainner.feature.friend.domain

import com.smarttrainner.core.model.UserSessionId

@JvmInline
value class FriendConnectionId(val value: String)

@JvmInline
value class FriendRequestId(val value: String)

data class SocialUser(
    val sessionId: UserSessionId,
    val displayName: String,
    val nickname: String,
    val avatarUrl: String? = null
)

data class FriendConnection(
    val id: FriendConnectionId,
    val friend: SocialUser,
    val createdAt: String,
    val updatedAt: String
)

data class FriendRequest(
    val id: FriendRequestId,
    val requester: SocialUser,
    val receiver: SocialUser,
    val status: FriendRequestStatus,
    val createdAt: String,
    val updatedAt: String,
    val respondedAt: String? = null
)

enum class FriendRequestStatus {
    Pending,
    Accepted,
    Declined
}

enum class FriendRequestDirection {
    Incoming,
    Outgoing
}
