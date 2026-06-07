package com.smarttrainner.feature.friend.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.ui.SmartTrainnerScreenChrome
import com.smarttrainner.core.ui.SmartTrainnerScreenScaffold
import com.smarttrainner.feature.friend.api.FriendFeatureEntry
import javax.inject.Inject

class FriendFeatureEntryImpl @Inject constructor() : FriendFeatureEntry {
    @Composable
    override fun Route() {
        val viewModel: FriendViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val message = state.message
        SmartTrainnerScreenScaffold(
            chrome = SmartTrainnerScreenChrome(
                title = stringResource(R.string.friend_route_title),
                subtitle = stringResource(
                    R.string.friend_route_subtitle,
                    state.incomingRequests.size,
                    state.friends.size
                )
            )
        ) {
            item {
                FriendAddCard(
                    nickname = state.nicknameInput,
                    isSending = state.isSending,
                    message = message,
                    onNicknameChanged = { viewModel.onAction(FriendAction.NicknameChanged(it)) },
                    onSendClick = { viewModel.onAction(FriendAction.SendRequestClick) }
                )
            }
            if (state.loadFailed) {
                item {
                    FriendStatusCard(
                        text = stringResource(R.string.friend_load_error),
                        actionLabel = stringResource(R.string.friend_retry),
                        onActionClick = { viewModel.onAction(FriendAction.RetryClick) }
                    )
                }
            }
            if (state.incomingRequests.isNotEmpty()) {
                item {
                    FriendSectionTitle(text = stringResource(R.string.friend_incoming_title))
                }
                state.incomingRequests.forEach { request ->
                    item(key = "request_${request.id.value}") {
                        FriendRequestRow(
                            request = request,
                            busy = state.actionRequestId == request.id,
                            onAccept = {
                                viewModel.onAction(FriendAction.AcceptRequestClick(request.id))
                            },
                            onDecline = {
                                viewModel.onAction(FriendAction.DeclineRequestClick(request.id))
                            }
                        )
                    }
                }
            }
            item {
                FriendSectionTitle(text = stringResource(R.string.friend_list_title))
            }
            when {
                state.isLoading && state.friends.isEmpty() -> item {
                    FriendStatusCard(text = stringResource(R.string.friend_loading))
                }
                state.friends.isEmpty() -> item {
                    FriendStatusCard(text = stringResource(R.string.friend_list_empty))
                }
                else -> state.friends.forEach { friend ->
                    item(key = "friend_${friend.id.value}") {
                        FriendRow(friend = friend)
                    }
                }
            }
        }
    }
}
