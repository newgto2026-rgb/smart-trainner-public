package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface WorkoutLogNetworkApi {
    @POST("api/workout-logs")
    suspend fun createWorkoutLog(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Body request: WorkoutLogBackupRequest
    ): WorkoutLogBackupResponse
}

@Serializable
data class WorkoutLogBackupRequest(
    val date: String,
    val exerciseId: String,
    val plannedExerciseId: String? = null,
    val sets: List<WorkoutSetLogBackupRequest>,
    val notes: String? = null
)

@Serializable
data class WorkoutSetLogBackupRequest(
    val setIndex: Int,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val durationMinutes: Int? = null,
    val completed: Boolean = true
)

@Serializable
data class WorkoutLogBackupResponse(
    val data: WorkoutLogBackupDto
)

@Serializable
data class WorkoutLogBackupDto(
    val id: String,
    val sessionId: String
)
