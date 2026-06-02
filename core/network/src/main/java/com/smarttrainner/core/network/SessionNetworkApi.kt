package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SessionNetworkApi {
    @POST("api/sessions")
    suspend fun createSession(@Body request: CreateSessionRequest): UserSessionResponse

    @GET("api/sessions/nickname")
    suspend fun checkNicknameAvailability(
        @Query("nickname") nickname: String,
        @Query("sessionId") sessionId: String? = null
    ): NicknameAvailabilityResponse

    @POST("api/auth/google")
    suspend fun signInWithGoogle(@Body request: GoogleSignInRequest): UserSessionResponse
}

@Serializable
data class CreateSessionRequest(
    val id: String = "local-default",
    val displayName: String = "Local Athlete",
    val nickname: String = "local-athlete",
    val email: String? = null,
    val provider: String = "local"
)

@Serializable
data class GoogleSignInRequest(
    val idToken: String,
    val nickname: String,
    val sessionId: String? = null
)

@Serializable
data class UserSessionResponse(
    val data: UserSessionDto
)

@Serializable
data class NicknameAvailabilityResponse(
    val data: NicknameAvailabilityDto
)

@Serializable
data class NicknameAvailabilityDto(
    val nickname: String,
    val available: Boolean
)

@Serializable
data class UserSessionDto(
    val id: String,
    val displayName: String,
    val nickname: String,
    val email: String? = null,
    val provider: String,
    val createdAt: String,
    val updatedAt: String,
    val linkedAt: String? = null
)
