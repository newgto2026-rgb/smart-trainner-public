package com.smarttrainner.feature.friend.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.ui.SmartTrainnerSectionTitle
import com.smarttrainner.feature.friend.domain.FriendConnection
import com.smarttrainner.feature.friend.domain.FriendRequest

@Composable
internal fun FriendAddCard(
    nickname: String,
    isSending: Boolean,
    message: FriendMessage?,
    onNicknameChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.SurfaceRaised,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        modifier = modifier
            .fillMaxWidth()
            .testTag("friend_add_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = SmartTrainnerColors.Coral,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.friend_add_title),
                    color = SmartTrainnerColors.Ink,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = onNicknameChanged,
                    label = { Text(stringResource(R.string.friend_nickname_label)) },
                    placeholder = { Text(stringResource(R.string.friend_nickname_placeholder)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    trailingIcon = {
                        if (nickname.isNotEmpty()) {
                            IconButton(onClick = { onNicknameChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("friend_nickname_input")
                )
                Button(
                    onClick = onSendClick,
                    enabled = !isSending,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.testTag("friend_send_request")
                ) {
                    Text(
                        text = if (isSending) {
                            stringResource(R.string.friend_sending_request)
                        } else {
                            stringResource(R.string.friend_send_request)
                        }
                    )
                }
            }
            val helper = when (message) {
                FriendMessage.EmptyNickname -> stringResource(R.string.friend_error_empty_nickname)
                FriendMessage.RequestSent -> stringResource(R.string.friend_request_sent)
                FriendMessage.RequestAccepted -> stringResource(R.string.friend_request_accepted)
                FriendMessage.RequestDeclined -> stringResource(R.string.friend_request_declined)
                FriendMessage.RequestFailed -> stringResource(R.string.friend_error_send_failed)
                FriendMessage.LoadFailed,
                null -> stringResource(R.string.friend_add_helper)
            }
            Text(
                text = helper,
                color = if (message == FriendMessage.RequestFailed || message == FriendMessage.EmptyNickname) {
                    SmartTrainnerColors.Coral
                } else {
                    SmartTrainnerColors.Muted
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
internal fun FriendSectionTitle(text: String, modifier: Modifier = Modifier) {
    SmartTrainnerSectionTitle(
        text = text,
        modifier = modifier.padding(top = 2.dp)
    )
}

@Composable
internal fun FriendStatusCard(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.SurfaceRaised,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = text,
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            if (actionLabel != null && onActionClick != null) {
                OutlinedButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
internal fun FriendRequestRow(
    request: FriendRequest,
    busy: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.SurfaceRaised,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        modifier = modifier
            .fillMaxWidth()
            .testTag("friend_request_${request.id.value}")
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 72.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FriendAvatar(request.requester.nickname)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.requester.nickname,
                    color = SmartTrainnerColors.Ink,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.friend_request_subtitle),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = onDecline,
                enabled = !busy,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.friend_decline))
            }
            Button(
                onClick = onAccept,
                enabled = !busy,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.friend_accept))
            }
        }
    }
}

@Composable
internal fun FriendRow(
    friend: FriendConnection,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.SurfaceRaised,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        modifier = modifier
            .fillMaxWidth()
            .testTag("friend_row_${friend.friend.sessionId.value}")
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FriendAvatar(friend.friend.nickname)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.friend.nickname,
                    color = SmartTrainnerColors.Ink,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.friend_connected_subtitle),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = SmartTrainnerColors.Muted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun FriendAvatar(
    nickname: String,
    modifier: Modifier = Modifier
) {
    val initial = nickname.ifBlank { "?" }.take(1).uppercase()
    Surface(
        shape = CircleShape,
        color = SmartTrainnerColors.CoralSoft,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
    ) {
        Row(
            modifier = Modifier
                .background(SmartTrainnerColors.CoralSoft)
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = initial,
                color = SmartTrainnerColors.Coral,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
