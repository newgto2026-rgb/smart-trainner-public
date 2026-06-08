package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendNetworkApi {
    @GET("api/friends")
    suspend fun getFriends(
        @Header("x-smart-trainner-session-id") sessionId: String
    ): FriendListResponse

    @GET("api/friend-requests")
    suspend fun getFriendRequests(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Query("box") box: String
    ): FriendRequestListResponse

    @POST("api/friend-requests")
    suspend fun sendFriendRequest(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Body request: SendFriendRequestRequest
    ): FriendRequestResponse

    @POST("api/friend-requests/{id}/accept")
    suspend fun acceptFriendRequest(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Path("id") id: String
    ): FriendResponse

    @POST("api/friend-requests/{id}/decline")
    suspend fun declineFriendRequest(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Path("id") id: String
    ): FriendRequestResponse

    @DELETE("api/friends/{friendSessionId}")
    suspend fun removeFriend(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Path("friendSessionId") friendSessionId: String
    )
}

@Serializable
data class FriendListResponse(
    val data: List<FriendDto>,
    val count: Int
)

@Serializable
data class FriendRequestListResponse(
    val data: List<FriendRequestDto>,
    val count: Int
)

@Serializable
data class FriendResponse(
    val data: FriendDto
)

@Serializable
data class FriendRequestResponse(
    val data: FriendRequestDto
)

@Serializable
data class SocialUserDto(
    val sessionId: String,
    val displayName: String,
    val nickname: String,
    val avatarUrl: String? = null
)

@Serializable
data class FriendDto(
    val id: String,
    val friend: SocialUserDto,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class FriendRequestDto(
    val id: String,
    val requester: SocialUserDto,
    val receiver: SocialUserDto,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val respondedAt: String? = null
)

@Serializable
data class SendFriendRequestRequest(
    val nickname: String
)
