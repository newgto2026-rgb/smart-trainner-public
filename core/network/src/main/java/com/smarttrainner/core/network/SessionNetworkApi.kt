package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path
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

    @GET("api/sessions/{sessionId}/device")
    suspend fun validateSessionDevice(
        @Path("sessionId") sessionId: String,
        @Header("x-smart-trainner-device-id") deviceId: String
    ): SessionDeviceStatusResponse

    @PATCH("api/sessions/{sessionId}/profile")
    suspend fun updateSessionProfile(
        @Path("sessionId") sessionId: String,
        @Body request: SessionProfileRequest
    ): UserSessionResponse
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
    val nickname: String? = null,
    val sessionId: String? = null,
    val profile: SessionProfileRequest? = null,
    val deviceId: String,
    val deviceName: String? = null,
    val forceDeviceLogin: Boolean = false
)

@Serializable
data class SessionProfileRequest(
    val nickname: String? = null,
    val gender: String? = null,
    val recordedDate: String? = null,
    val heightCm: Int,
    val weightKg: Double
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
data class SessionDeviceStatusResponse(
    val data: SessionDeviceStatusDto
)

@Serializable
data class SessionDeviceStatusDto(
    val valid: Boolean,
    val sessionId: String,
    val activeDevice: ActiveDeviceDto? = null
)

@Serializable
data class ActiveDeviceDto(
    val id: String,
    val name: String? = null,
    val updatedAt: String
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
    val profile: UserProfileDto = UserProfileDto(),
    val createdAt: String,
    val updatedAt: String,
    val linkedAt: String? = null
)

@Serializable
data class UserProfileDto(
    val gender: String? = null,
    val bodyMeasurements: List<BodyMeasurementDto> = emptyList()
)

@Serializable
data class BodyMeasurementDto(
    val recordedDate: String,
    val heightCm: Int,
    val weightKg: Double
)
