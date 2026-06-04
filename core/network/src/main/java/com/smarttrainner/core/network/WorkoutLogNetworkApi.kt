package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface WorkoutLogNetworkApi {
    @GET("api/workout-logs")
    suspend fun getWorkoutLogs(
        @Header("x-smart-trainner-session-id") sessionId: String
    ): WorkoutLogListResponse

    @POST("api/workout-logs")
    suspend fun createWorkoutLog(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Body request: WorkoutLogRequest
    ): WorkoutLogResponse
}

@Serializable
data class WorkoutLogListResponse(
    val data: List<WorkoutLogDto>,
    val count: Int
)

@Serializable
data class WorkoutLogResponse(
    val data: WorkoutLogDto
)

@Serializable
data class WorkoutLogDto(
    val id: String,
    val sessionId: String,
    val date: String,
    val exerciseId: String,
    val plannedExerciseId: String? = null,
    val routineDayInstanceId: String? = null,
    val sets: List<WorkoutSetLogDto>,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class WorkoutSetLogDto(
    val setIndex: Int,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val durationMinutes: Int? = null,
    val restSeconds: Int? = null,
    val completed: Boolean = true
)

@Serializable
data class WorkoutLogRequest(
    val id: String,
    val date: String,
    val exerciseId: String,
    val plannedExerciseId: String? = null,
    val routineDayInstanceId: String? = null,
    val sets: List<WorkoutSetLogRequest>,
    val notes: String? = null
)

@Serializable
data class WorkoutSetLogRequest(
    val setIndex: Int,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val durationMinutes: Int? = null,
    val restSeconds: Int? = null,
    val completed: Boolean = true
)
