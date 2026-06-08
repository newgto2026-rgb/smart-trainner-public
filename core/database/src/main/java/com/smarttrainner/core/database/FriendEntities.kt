package com.smarttrainner.core.database

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "friend_connections",
    primaryKeys = ["ownerSessionId", "id"],
    indices = [
        Index("ownerSessionId"),
        Index(value = ["ownerSessionId", "friendSessionId"], unique = true)
    ]
)
data class FriendConnectionEntity(
    val ownerSessionId: String,
    val id: String,
    val friendSessionId: String,
    val friendNickname: String,
    val friendDisplayName: String,
    val friendAvatarUrl: String?,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "friend_requests",
    primaryKeys = ["ownerSessionId", "id"],
    indices = [
        Index("ownerSessionId"),
        Index(value = ["ownerSessionId", "direction", "status"])
    ]
)
data class FriendRequestEntity(
    val ownerSessionId: String,
    val id: String,
    val requesterSessionId: String,
    val requesterNickname: String,
    val requesterDisplayName: String,
    val requesterAvatarUrl: String?,
    val receiverSessionId: String,
    val receiverNickname: String,
    val receiverDisplayName: String,
    val receiverAvatarUrl: String?,
    val direction: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val respondedAt: String?
)
