package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PushTokenNetworkApi {
    @POST("api/push-tokens")
    suspend fun registerPushToken(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Body request: PushTokenRegistrationRequest
    ): PushTokenRegistrationResponse
}

@Serializable
data class PushTokenRegistrationRequest(
    val token: String,
    val platform: String = "android",
    val appVersion: String? = null
)

@Serializable
data class PushTokenRegistrationResponse(
    val data: PushTokenRegistrationDto
)

@Serializable
data class PushTokenRegistrationDto(
    val sessionId: String,
    val deviceId: String,
    val platform: String,
    val updatedAt: String
)
