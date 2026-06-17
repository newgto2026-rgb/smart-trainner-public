package com.smarttrainner.core.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CustomExerciseNetworkApi {
    @GET("api/custom-exercises")
    suspend fun getCustomExercises(
        @Query("sessionId") sessionId: String
    ): CustomExerciseListResponse

    @POST("api/custom-exercises")
    suspend fun createCustomExercise(
        @Query("sessionId") sessionId: String,
        @Body request: CustomExerciseRequest
    ): CustomExerciseResponse

    @PUT("api/custom-exercises/{id}")
    suspend fun updateCustomExercise(
        @Path("id") id: String,
        @Query("sessionId") sessionId: String,
        @Body request: CustomExerciseRequest
    ): CustomExerciseResponse

    @DELETE("api/custom-exercises/{id}")
    suspend fun archiveCustomExercise(
        @Path("id") id: String,
        @Query("sessionId") sessionId: String
    )
}

@Serializable
data class CustomExerciseListResponse(
    val data: List<CustomExerciseDto>,
    val count: Int
)

@Serializable
data class CustomExerciseResponse(
    val data: CustomExerciseDto
)

@Serializable
data class CustomExerciseRequest(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val muscleGroups: List<String> = emptyList(),
    val equipment: String,
    val difficulty: String,
    val imageUrl: String? = null,
    val summary: String? = null,
    val instructions: List<String>,
    val safetyCues: List<String>,
    val defaultSets: Int,
    val repRangeStart: Int? = null,
    val repRangeEnd: Int? = null,
    val defaultDurationMinutes: Int? = null,
    val restSeconds: Int,
    val source: String = "USER_CREATED",
    val originExerciseId: String? = null,
    val archivedAt: String? = null
)

@Serializable
data class CustomExerciseDto(
    val id: String,
    val ownerSessionId: String,
    val name: String,
    val muscleGroup: String,
    val muscleGroups: List<String> = emptyList(),
    val equipment: String,
    val difficulty: String,
    val imageUrl: String? = null,
    val summary: String? = null,
    val instructions: List<String>,
    val safetyCues: List<String>,
    val defaultSets: Int,
    val repRangeStart: Int? = null,
    val repRangeEnd: Int? = null,
    val defaultDurationMinutes: Int? = null,
    val restSeconds: Int,
    val source: String = "USER_CREATED",
    val originExerciseId: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val archivedAt: String? = null
)
