package com.smarttrainner.feature.friend.data

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.feature.friend.domain.FriendRequestStatus
import org.junit.Test

class FriendMappersTest {
    @Test
    fun requestStatusMappingIsCaseInsensitive() {
        assertThat("accepted".toFriendRequestStatus()).isEqualTo(FriendRequestStatus.Accepted)
        assertThat("DECLINED".toFriendRequestStatus()).isEqualTo(FriendRequestStatus.Declined)
        assertThat("Rejected".toFriendRequestStatus()).isEqualTo(FriendRequestStatus.Declined)
        assertThat("pending".toFriendRequestStatus()).isEqualTo(FriendRequestStatus.Pending)
    }
}
