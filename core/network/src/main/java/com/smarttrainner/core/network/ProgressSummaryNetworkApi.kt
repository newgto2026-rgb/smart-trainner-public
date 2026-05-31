package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Header

interface ProgressSummaryNetworkApi {
    @GET("api/progress/summary")
    suspend fun getProgressSummary(
        @Header("x-smart-trainner-session-id") sessionId: String
    ): ProgressSummaryResponse
}

@Serializable
data class ProgressSummaryResponse(
    val data: ProgressSummaryDto
)

@Serializable
data class ProgressSummaryDto(
    val sessionId: String = "local-default",
    val totalLogs: Int,
    val totalVolumeKg: Double,
    val totalDurationMinutes: Int,
    val activeDays: Int,
    val byExercise: List<ProgressExerciseDto>
)

@Serializable
data class ProgressExerciseDto(
    val exerciseId: String,
    val name: String,
    val logs: Int,
    val volumeKg: Double,
    val durationMinutes: Int
)
