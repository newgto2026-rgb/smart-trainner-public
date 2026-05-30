package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST

interface SmartTrainnerApi {
    @GET("api/exercises")
    suspend fun getExercises(): ExerciseCatalogResponse

    @POST("api/plans/generate")
    suspend fun generatePlan(@Body request: GeneratePlanRequest): TrainingPlanResponse

    @GET("api/progress/summary")
    suspend fun getProgressSummary(
        @Header("x-smart-trainner-session-id") sessionId: String
    ): ProgressSummaryResponse

    @POST("api/sessions")
    suspend fun createSession(@Body request: CreateSessionRequest): UserSessionResponse

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

@Serializable
data class GeneratePlanRequest(
    val goal: String = "general",
    val daysPerWeek: Int = 3,
    val level: String = "beginner"
)

@Serializable
data class TrainingPlanResponse(
    val data: WeeklyPlanDto
)

@Serializable
data class WeeklyPlanDto(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val goal: String,
    val daysPerWeek: Int,
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
    val daysPerWeek: Int,
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
