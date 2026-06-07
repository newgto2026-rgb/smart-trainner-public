package com.smarttrainner.feature.friend.impl

import com.smarttrainner.feature.friend.domain.FriendRequestId

internal sealed interface FriendAction {
    data class NicknameChanged(val nickname: String) : FriendAction
    data object SendRequestClick : FriendAction
    data class AcceptRequestClick(val id: FriendRequestId) : FriendAction
    data class DeclineRequestClick(val id: FriendRequestId) : FriendAction
    data object RetryClick : FriendAction
    data object MessageShown : FriendAction
}
