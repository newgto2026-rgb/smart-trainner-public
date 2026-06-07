package com.smarttrainner.feature.friend.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.feature.friend.domain.AcceptFriendRequestUseCase
import com.smarttrainner.feature.friend.domain.DeclineFriendRequestUseCase
import com.smarttrainner.feature.friend.domain.FriendRequestId
import com.smarttrainner.feature.friend.domain.ObserveFriendsUseCase
import com.smarttrainner.feature.friend.domain.ObserveIncomingFriendRequestsUseCase
import com.smarttrainner.feature.friend.domain.RefreshFriendsUseCase
import com.smarttrainner.feature.friend.domain.SendFriendRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
internal class FriendViewModel @Inject constructor(
    observeFriends: ObserveFriendsUseCase,
    observeIncomingRequests: ObserveIncomingFriendRequestsUseCase,
    private val refreshFriends: RefreshFriendsUseCase,
    private val sendFriendRequest: SendFriendRequestUseCase,
    private val acceptFriendRequest: AcceptFriendRequestUseCase,
    private val declineFriendRequest: DeclineFriendRequestUseCase
) : ViewModel() {
    private val commandState = MutableStateFlow(FriendUiState())

    val uiState: StateFlow<FriendUiState> = combine(
        observeFriends(),
        observeIncomingRequests(),
        commandState
    ) { friends, incomingRequests, command ->
        command.copy(
            friends = friends,
            incomingRequests = incomingRequests
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FriendUiState()
    )

    init {
        refresh()
    }

    fun onAction(action: FriendAction) {
        when (action) {
            is FriendAction.NicknameChanged -> commandState.update {
                it.copy(
                    nicknameInput = action.nickname.take(MAX_NICKNAME_INPUT_LENGTH),
                    message = null
                )
            }
            FriendAction.SendRequestClick -> sendRequest()
            is FriendAction.AcceptRequestClick -> acceptRequest(action.id)
            is FriendAction.DeclineRequestClick -> declineRequest(action.id)
            FriendAction.RetryClick -> refresh()
            FriendAction.MessageShown -> commandState.update { it.copy(message = null) }
        }
    }

    private fun refresh() {
        commandState.update { it.copy(isLoading = true, loadFailed = false) }
        viewModelScope.launch {
            refreshFriends().fold(
                onSuccess = {
                    commandState.update {
                        it.copy(isLoading = false, loadFailed = false)
                    }
                },
                onFailure = {
                    commandState.update {
                        it.copy(
                            isLoading = false,
                            loadFailed = true,
                            message = FriendMessage.LoadFailed
                        )
                    }
                }
            )
        }
    }

    private fun sendRequest() {
        val nickname = commandState.value.nicknameInput.trim()
        if (nickname.isEmpty()) {
            commandState.update { it.copy(message = FriendMessage.EmptyNickname) }
            return
        }
        commandState.update { it.copy(isSending = true, message = null) }
        viewModelScope.launch {
            sendFriendRequest(nickname).fold(
                onSuccess = {
                    commandState.update {
                        it.copy(
                            nicknameInput = "",
                            isSending = false,
                            message = FriendMessage.RequestSent
                        )
                    }
                },
                onFailure = {
                    commandState.update {
                        it.copy(isSending = false, message = FriendMessage.RequestFailed)
                    }
                }
            )
        }
    }

    private fun acceptRequest(id: FriendRequestId) {
        commandState.update { it.copy(actionRequestId = id, message = null) }
        viewModelScope.launch {
            acceptFriendRequest(id).fold(
                onSuccess = {
                    commandState.update {
                        it.copy(actionRequestId = null, message = FriendMessage.RequestAccepted)
                    }
                },
                onFailure = {
                    commandState.update {
                        it.copy(actionRequestId = null, message = FriendMessage.RequestFailed)
                    }
                }
            )
        }
    }

    private fun declineRequest(id: FriendRequestId) {
        commandState.update { it.copy(actionRequestId = id, message = null) }
        viewModelScope.launch {
            declineFriendRequest(id).fold(
                onSuccess = {
                    commandState.update {
                        it.copy(actionRequestId = null, message = FriendMessage.RequestDeclined)
                    }
                },
                onFailure = {
                    commandState.update {
                        it.copy(actionRequestId = null, message = FriendMessage.RequestFailed)
                    }
                }
            )
        }
    }
}

private const val MAX_NICKNAME_INPUT_LENGTH = 24
