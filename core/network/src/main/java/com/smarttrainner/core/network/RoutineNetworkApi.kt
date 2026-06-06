package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface RoutineNetworkApi {
    @GET("api/routines")
    suspend fun getCustomRoutines(
        @Header("x-smart-trainner-session-id") sessionId: String
    ): CustomRoutineListResponse

    @POST("api/routines")
    suspend fun createCustomRoutine(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Body request: CustomRoutineRequest
    ): CustomRoutineResponse

    @PATCH("api/routines/{id}")
    suspend fun updateCustomRoutine(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Path("id") id: String,
        @Body request: CustomRoutineRequest
    ): CustomRoutineResponse

    @DELETE("api/routines/{id}")
    suspend fun deleteCustomRoutine(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Path("id") id: String
    )

    @GET("api/routines/selected")
    suspend fun getSelectedCustomRoutine(
        @Header("x-smart-trainner-session-id") sessionId: String
    ): SelectedCustomRoutineResponse

    @POST("api/routines/{id}/select")
    suspend fun selectCustomRoutine(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Path("id") id: String
    ): CustomRoutineSelectionResponse
}

@Serializable
data class CustomRoutineListResponse(
    val data: List<CustomRoutineDto>,
    val count: Int
)

@Serializable
data class CustomRoutineResponse(
    val data: CustomRoutineDto
)

@Serializable
data class SelectedCustomRoutineResponse(
    val data: CustomRoutineDto? = null
)

@Serializable
data class CustomRoutineSelectionResponse(
    val data: CustomRoutineSelectionDto
)

@Serializable
data class CustomRoutineSelectionDto(
    val sessionId: String,
    val routineId: String,
    val selectedAt: String,
    val routine: CustomRoutineDto
)

@Serializable
data class CustomRoutineRequest(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val days: List<CustomRoutineDayRequest>
)

@Serializable
data class CustomRoutineDayRequest(
    val day: Int,
    val title: String? = null,
    val focus: String,
    val primaryFocus: String? = null,
    val secondaryFocuses: List<String> = emptyList(),
    val minRecoveryHours: Int = 24,
    val exercises: List<CustomRoutineExerciseRequest>
)

@Serializable
data class CustomRoutineExerciseRequest(
    val exerciseId: String,
    val sets: Int,
    val repRangeStart: Int? = null,
    val repRangeEnd: Int? = null,
    val durationMinutes: Int? = null,
    val restSeconds: Int = 90,
    val note: String? = null
)

@Serializable
data class CustomRoutineDto(
    val id: String,
    val sessionId: String,
    val name: String,
    val description: String? = null,
    val cycleLength: Int,
    val days: List<CustomRoutineDayDto>,
    val isSelected: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
    val selectedAt: String? = null
)

@Serializable
data class CustomRoutineDayDto(
    val day: Int,
    val title: String? = null,
    val focus: String,
    val primaryFocus: String? = null,
    val secondaryFocuses: List<String> = emptyList(),
    val minRecoveryHours: Int = 24,
    val exercises: List<CustomRoutineExerciseDto>
)

@Serializable
data class CustomRoutineExerciseDto(
    val exerciseId: String,
    val name: String,
    val sets: Int,
    val repRangeStart: Int? = null,
    val repRangeEnd: Int? = null,
    val durationMinutes: Int? = null,
    val restSeconds: Int = 90,
    val note: String? = null
)
