package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface PlanGenerationNetworkApi {
    @POST("api/plans/generate")
    suspend fun generatePlan(@Body request: GeneratePlanRequest): TrainingPlanResponse
}

@Serializable
data class GeneratePlanRequest(
    val goal: String = "general",
    val cycleLength: Int = 3,
    val level: String = "beginner"
)

@Serializable
data class TrainingPlanResponse(
    val data: CyclePlanDto
)

@Serializable
data class CyclePlanDto(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val goal: String,
    val cycleLength: Int,
    val level: String,
    val days: List<PlanDayDto>
)

@Serializable
data class PlanDayDto(
    val day: Int,
    val focus: String,
    val exercises: List<PlanExerciseDto>
)

@Serializable
data class PlanExerciseDto(
    val exerciseId: String,
    val name: String,
    val sets: Int? = null,
    val reps: Int? = null,
    val durationMinutes: Int? = null
)
