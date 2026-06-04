package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface RoutineProgressNetworkApi {
    @GET("api/routine-progress")
    suspend fun getRoutineProgress(
        @Header("x-smart-trainner-session-id") sessionId: String
    ): RoutineProgressResponse

    @GET("api/routine-progress/cycles")
    suspend fun getRoutineCycleCompletions(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Query("templateId") templateId: String? = null
    ): RoutineCycleCompletionsResponse

    @POST("api/routine-progress/start")
    suspend fun startRoutineProgress(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Body request: RoutineProgressStartRequest
    ): RoutineProgressRequiredResponse

    @POST("api/routine-progress/switch-template")
    suspend fun switchRoutineTemplate(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Body request: RoutineProgressSwitchTemplateRequest
    ): RoutineProgressRequiredResponse

    @POST("api/routine-progress/complete-day")
    suspend fun completeRoutineDay(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Body request: RoutineProgressCompleteDayRequest
    ): RoutineProgressRequiredResponse

    @POST("api/routine-progress/cancel-latest")
    suspend fun cancelLatestRoutineDayCompletion(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Body request: RoutineProgressCancelLatestRequest
    ): RoutineProgressCancelLatestResponse

    @POST("api/routine-progress/sync")
    suspend fun syncRoutineProgress(
        @Header("x-smart-trainner-session-id") sessionId: String,
        @Body request: RoutineProgressSyncRequest
    ): RoutineProgressSyncResponse
}

@Serializable
data class RoutineProgressResponse(
    val data: RoutineProgressDto? = null
)

@Serializable
data class RoutineCycleCompletionsResponse(
    val data: List<RoutineCycleCompletionDto> = emptyList(),
    val count: Int = data.size
)

@Serializable
data class RoutineProgressRequiredResponse(
    val data: RoutineProgressDto
)

@Serializable
data class RoutineProgressCancelLatestResponse(
    val data: RoutineProgressCancelLatestDto
)

@Serializable
data class RoutineProgressCancelLatestDto(
    val progress: RoutineProgressDto,
    val deletedWorkoutLogCount: Int
)

@Serializable
data class RoutineProgressSyncResponse(
    val data: RoutineProgressSyncDto
)

@Serializable
data class RoutineProgressSyncDto(
    val status: RoutineProgressSyncStatus,
    val progress: RoutineProgressDto,
    val conflict: RoutineProgressConflictDto? = null
)

@Serializable
enum class RoutineProgressSyncStatus {
    ADOPTED_LOCAL,
    IN_SYNC,
    SERVER_WINS
}

@Serializable
data class RoutineProgressConflictDto(
    val reason: String,
    val localCycleNumber: Int,
    val serverCycleNumber: Int,
    val localDayIndex: Int,
    val serverDayIndex: Int
)

@Serializable
data class RoutineProgressDto(
    val sessionId: String,
    val templateId: String,
    val dayIndex: Int,
    val cycleNumber: Int,
    val startedAt: String,
    val cycleStartedAt: String? = null,
    val lastCompletedDayIndex: Int? = null,
    val lastCompletedAt: String? = null,
    val lastCompletedCycleNumber: Int? = null,
    val lastCompletedPreviousCycleStartedAt: String? = null,
    val revision: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class RoutineCycleCompletionDto(
    val id: String,
    val sessionId: String,
    val templateId: String,
    val cycleNumber: Int,
    val startedAt: String,
    val completedAt: String,
    val durationDays: Int,
    val completedDayIndex: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class RoutineProgressStartRequest(
    val templateId: String,
    val startedAt: String? = null,
    val cycleStartedAt: String? = null
)

@Serializable
data class RoutineProgressSwitchTemplateRequest(
    val templateId: String,
    val dayIndex: Int
)

@Serializable
data class RoutineProgressCompleteDayRequest(
    val templateId: String,
    val completedDayIndex: Int,
    val nextDayIndex: Int,
    val completedAt: String,
    val newCycleStartedAt: String? = null,
    val completedCycleDurationDays: Int? = null
)

@Serializable
data class RoutineProgressCancelLatestRequest(
    val templateId: String,
    val restoredDayIndex: Int,
    val restoredCycleNumber: Int,
    val restoredCycleStartedAt: String? = null,
    val remainingLastCompletedDayIndex: Int? = null,
    val remainingLastCompletedAt: String? = null,
    val remainingLastCompletedCycleNumber: Int? = null,
    val remainingLastCompletedPreviousCycleStartedAt: String? = null,
    val routineDayInstanceId: String? = null,
    val plannedExerciseIds: List<String>,
    val additionalExerciseIdPrefix: String
)

@Serializable
data class RoutineProgressSyncRequest(
    val templateId: String,
    val dayIndex: Int,
    val cycleNumber: Int,
    val startedAt: String? = null,
    val cycleStartedAt: String? = null,
    val lastCompletedDayIndex: Int? = null,
    val lastCompletedAt: String? = null,
    val lastCompletedCycleNumber: Int? = null,
    val lastCompletedPreviousCycleStartedAt: String? = null
)
