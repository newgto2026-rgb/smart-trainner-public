package com.smarttrainner.feature.friend.domain

import com.smarttrainner.core.model.UserSessionId
import javax.inject.Inject

class ObserveFriendsUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    operator fun invoke() = repository.observeFriends()
}

class ObserveIncomingFriendRequestsUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    operator fun invoke() = repository.observeIncomingRequests()
}

class RefreshFriendsUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend operator fun invoke() = repository.refresh()
}

class SendFriendRequestUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(nickname: String) = repository.sendRequest(nickname)
}

class AcceptFriendRequestUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(id: FriendRequestId) = repository.acceptRequest(id)
}

class DeclineFriendRequestUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(id: FriendRequestId) = repository.declineRequest(id)
}

class RemoveFriendUseCase @Inject constructor(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(friendSessionId: UserSessionId) =
        repository.removeFriend(friendSessionId)
}
