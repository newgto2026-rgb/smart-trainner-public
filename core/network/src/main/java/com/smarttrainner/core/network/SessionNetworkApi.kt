package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface SessionNetworkApi {
    @POST("api/sessions")
    suspend fun createSession(@Body request: CreateSessionRequest): UserSessionResponse
}

@Serializable
data class CreateSessionRequest(
    val id: String = "local-default",
    val displayName: String = "Local Athlete",
    val email: String? = null,
    val provider: String = "local"
)

@Serializable
data class UserSessionResponse(
    val data: UserSessionDto
)

@Serializable
data class UserSessionDto(
    val id: String,
    val displayName: String,
    val email: String? = null,
    val provider: String,
    val createdAt: String,
    val updatedAt: String,
    val linkedAt: String? = null
)
