package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface ExerciseCatalogNetworkApi {
    @GET("api/exercises")
    suspend fun getExercises(): ExerciseCatalogResponse
}

@Serializable
data class ExerciseCatalogResponse(
    val data: List<ExerciseDto>,
    val count: Int
)

@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val steps: List<String>,
    val stepImages: List<ExerciseStepImageDto>,
    val cautions: List<String>,
    val imageKey: String,
    val imageUrl: String? = null,
    val muscleGroups: List<String>,
    val equipment: List<String>,
    val difficulty: String,
    val defaultSets: Int? = null,
    val defaultReps: Int? = null,
    val defaultDurationMinutes: Int? = null
)

@Serializable
data class ExerciseStepImageDto(
    val step: Int,
    val phase: String,
    val imageKey: String,
    val alt: String
)
