package com.smarttrainner.feature.friend.impl

import androidx.compose.runtime.Immutable
import com.smarttrainner.feature.friend.domain.FriendConnection
import com.smarttrainner.feature.friend.domain.FriendRequest
import com.smarttrainner.feature.friend.domain.FriendRequestId

@Immutable
internal data class FriendUiState(
    val friends: List<FriendConnection> = emptyList(),
    val incomingRequests: List<FriendRequest> = emptyList(),
    val nicknameInput: String = "",
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val actionRequestId: FriendRequestId? = null,
    val loadFailed: Boolean = false,
    val message: FriendMessage? = null
)

internal enum class FriendMessage {
    EmptyNickname,
    RequestSent,
    RequestAccepted,
    RequestDeclined,
    RequestFailed,
    LoadFailed
}
