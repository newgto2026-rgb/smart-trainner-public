package com.smarttrainner.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.room.Room
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.database.CUSTOM_EXERCISE_SYNCED
import com.smarttrainner.core.database.CUSTOM_EXERCISE_SYNC_PENDING_DELETE
import com.smarttrainner.core.database.CUSTOM_EXERCISE_SYNC_PENDING_UPDATE
import com.smarttrainner.core.database.CUSTOM_EXERCISE_SYNC_PENDING_UPSERT
import com.smarttrainner.core.database.CustomExerciseDao
import com.smarttrainner.core.database.SmartTrainnerDatabase
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.datastore.DEFAULT_USER_SESSION_ID
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.datastore.trainingDataStore
import com.smarttrainner.core.domain.TrainingSeedStore
import com.smarttrainner.core.model.CustomExerciseInput
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.network.CustomExerciseDto
import com.smarttrainner.core.network.CustomExerciseListResponse
import com.smarttrainner.core.network.CustomExerciseNetworkApi
import com.smarttrainner.core.network.CustomExerciseRequest
import com.smarttrainner.core.network.CustomExerciseResponse
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultExerciseRepositoryTest {
    private val clock = Clock.fixed(Instant.parse("2026-06-17T00:00:00Z"), ZoneOffset.UTC)
    private lateinit var context: Context
    private lateinit var database: SmartTrainnerDatabase
    private lateinit var dao: CustomExerciseDao
    private lateinit var networkApi: FakeCustomExerciseNetworkApi
    private lateinit var repository: DefaultExerciseRepository

    @Before
    fun setUp() = runTest {
        context = RuntimeEnvironment.getApplication()
        context.trainingDataStore.edit { it.clear() }
        database = Room.inMemoryDatabaseBuilder(
            context,
            SmartTrainnerDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.customExerciseDao()
        networkApi = FakeCustomExerciseNetworkApi()
        repository = DefaultExerciseRepository(
            seedStore = TrainingSeedStore(),
            customExerciseDao = dao,
            activeSessionResolver = ActiveSessionResolver(
                TrainingPreferencesDataSource(context = context, clock = clock)
            ),
            customExerciseNetworkApi = networkApi,
            clock = clock
        )
    }

    @After
    fun tearDown() = runTest {
        database.close()
        context.trainingDataStore.edit { it.clear() }
    }

    @Test
    fun saveCustomExerciseRetriesCreateForExistingPendingUpsert() = runTest {
        val input = customInput(id = ExerciseId("custom-exercise-offline"))
        networkApi.createFailure = IOException("offline")

        val firstResult = repository.saveCustomExercise(input)
        val secondResult = repository.saveCustomExercise(input.copy(name = "Offline row edited"))

        assertThat(firstResult.isSuccess).isTrue()
        assertThat(secondResult.isSuccess).isTrue()
        assertThat(networkApi.createRequests.map { it.name })
            .containsExactly("Offline row", "Offline row edited")
            .inOrder()
        assertThat(networkApi.updateRequests).isEmpty()
        assertThat(dao.getById(DEFAULT_USER_SESSION_ID, input.id!!.value)?.syncState)
            .isEqualTo(CUSTOM_EXERCISE_SYNC_PENDING_UPSERT)
    }

    @Test
    fun saveCustomExerciseUsesPendingUpdateForSyncedExistingExercise() = runTest {
        val exerciseId = ExerciseId("custom-exercise-synced")
        val input = customInput(id = exerciseId)
        dao.upsert(
            input.toEntity(
                id = exerciseId.value,
                ownerSessionId = DEFAULT_USER_SESSION_ID,
                createdAt = clock.instant().toString(),
                updatedAt = clock.instant().toString(),
                archivedAt = null,
                syncState = CUSTOM_EXERCISE_SYNCED
            )
        )
        networkApi.updateFailure = IOException("offline")

        val result = repository.saveCustomExercise(input.copy(name = "Edited synced row"))

        assertThat(result.isSuccess).isTrue()
        assertThat(networkApi.createRequests).isEmpty()
        assertThat(networkApi.updateRequests.map { it.request.name }).containsExactly("Edited synced row")
        assertThat(dao.getById(DEFAULT_USER_SESSION_ID, exerciseId.value)?.syncState)
            .isEqualTo(CUSTOM_EXERCISE_SYNC_PENDING_UPDATE)
    }

    @Test
    fun syncPendingTrainingDataUsesUpdateForPendingUpdateRows() = runTest {
        val exerciseId = ExerciseId("custom-exercise-update")
        dao.upsert(
            customInput(id = exerciseId).toEntity(
                id = exerciseId.value,
                ownerSessionId = DEFAULT_USER_SESSION_ID,
                createdAt = clock.instant().toString(),
                updatedAt = clock.instant().toString(),
                archivedAt = null,
                syncState = CUSTOM_EXERCISE_SYNC_PENDING_UPDATE
            )
        )

        val result = repository.syncPendingTrainingData()

        assertThat(result.isSuccess).isTrue()
        assertThat(networkApi.createRequests).isEmpty()
        assertThat(networkApi.updateRequests.map { it.id }).containsExactly(exerciseId.value)
        assertThat(dao.getById(DEFAULT_USER_SESSION_ID, exerciseId.value)?.syncState)
            .isEqualTo(CUSTOM_EXERCISE_SYNCED)
    }

    @Test
    fun syncPendingTrainingDataDoesNotOverwriteUnsyncedLocalRowsFromRemoteFetch() = runTest {
        val exerciseId = ExerciseId("custom-exercise-local-edit")
        val input = customInput(id = exerciseId, name = "Local edit")
        dao.upsert(
            input.toEntity(
                id = exerciseId.value,
                ownerSessionId = DEFAULT_USER_SESSION_ID,
                createdAt = clock.instant().toString(),
                updatedAt = clock.instant().toString(),
                archivedAt = null,
                syncState = CUSTOM_EXERCISE_SYNC_PENDING_UPDATE
            )
        )
        networkApi.updateFailure = IOException("offline")
        networkApi.remoteExercises = listOf(input.copy(name = "Remote copy").toDto())

        val result = repository.syncPendingTrainingData()

        assertThat(result.isFailure).isTrue()
        val local = dao.getById(DEFAULT_USER_SESSION_ID, exerciseId.value)
        assertThat(local?.name).isEqualTo("Local edit")
        assertThat(local?.syncState).isEqualTo(CUSTOM_EXERCISE_SYNC_PENDING_UPDATE)
    }

    @Test
    fun archiveCustomExerciseHidesExerciseAndKeepsPendingDeleteWhenOffline() = runTest {
        val exerciseId = ExerciseId("custom-exercise-synced")
        dao.upsert(
            customInput(id = exerciseId).toEntity(
                id = exerciseId.value,
                ownerSessionId = DEFAULT_USER_SESSION_ID,
                createdAt = clock.instant().toString(),
                updatedAt = clock.instant().toString(),
                archivedAt = null,
                syncState = CUSTOM_EXERCISE_SYNCED
            )
        )
        networkApi.archiveFailure = IOException("offline")

        val result = repository.archiveCustomExercise(exerciseId)

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.getExercise(exerciseId)).isNull()
        assertThat(networkApi.archiveRequests).containsExactly(exerciseId.value to DEFAULT_USER_SESSION_ID)
        assertThat(dao.getById(DEFAULT_USER_SESSION_ID, exerciseId.value)?.syncState)
            .isEqualTo(CUSTOM_EXERCISE_SYNC_PENDING_DELETE)
    }

    private fun customInput(
        id: ExerciseId?,
        name: String = "Offline row"
    ) = CustomExerciseInput(
        id = id,
        name = name,
        muscleGroup = MuscleGroup.BACK,
        equipment = EquipmentType.CABLE,
        difficulty = DifficultyLevel.INTERMEDIATE,
        imageUri = null,
        summary = "A custom row.",
        instructions = listOf("Pull toward the ribs."),
        safetyCues = listOf("Keep shoulders down."),
        defaultSets = 3,
        repRangeStart = 8,
        repRangeEnd = 12,
        defaultDurationMinutes = null,
        restSeconds = 90
    )

    private fun CustomExerciseInput.toDto() = CustomExerciseDto(
        id = id!!.value,
        ownerSessionId = DEFAULT_USER_SESSION_ID,
        name = name,
        muscleGroup = muscleGroup.name,
        muscleGroups = listOf(muscleGroup.name),
        equipment = equipment.name,
        difficulty = difficulty.name,
        imageUrl = imageUri,
        summary = summary,
        instructions = instructions,
        safetyCues = safetyCues,
        defaultSets = defaultSets,
        repRangeStart = repRangeStart,
        repRangeEnd = repRangeEnd,
        defaultDurationMinutes = defaultDurationMinutes,
        restSeconds = restSeconds,
        createdAt = clock.instant().toString(),
        updatedAt = clock.instant().toString()
    )
}

private data class UpdateRequest(
    val id: String,
    val sessionId: String,
    val request: CustomExerciseRequest
)

private class FakeCustomExerciseNetworkApi : CustomExerciseNetworkApi {
    val createRequests = mutableListOf<CustomExerciseRequest>()
    val updateRequests = mutableListOf<UpdateRequest>()
    val archiveRequests = mutableListOf<Pair<String, String>>()
    var remoteExercises: List<CustomExerciseDto> = emptyList()
    var createFailure: Throwable? = null
    var updateFailure: Throwable? = null
    var archiveFailure: Throwable? = null

    override suspend fun getCustomExercises(sessionId: String): CustomExerciseListResponse =
        CustomExerciseListResponse(remoteExercises, remoteExercises.size)

    override suspend fun createCustomExercise(
        sessionId: String,
        request: CustomExerciseRequest
    ): CustomExerciseResponse {
        createRequests += request
        createFailure?.let { throw it }
        return CustomExerciseResponse(request.toDto(ownerSessionId = sessionId))
    }

    override suspend fun updateCustomExercise(
        id: String,
        sessionId: String,
        request: CustomExerciseRequest
    ): CustomExerciseResponse {
        updateRequests += UpdateRequest(id = id, sessionId = sessionId, request = request)
        updateFailure?.let { throw it }
        return CustomExerciseResponse(request.toDto(ownerSessionId = sessionId))
    }

    override suspend fun archiveCustomExercise(id: String, sessionId: String) {
        archiveRequests += id to sessionId
        archiveFailure?.let { throw it }
    }

    private fun CustomExerciseRequest.toDto(ownerSessionId: String) = CustomExerciseDto(
        id = id,
        ownerSessionId = ownerSessionId,
        name = name,
        muscleGroup = muscleGroup,
        muscleGroups = muscleGroups,
        equipment = equipment,
        difficulty = difficulty,
        imageUrl = imageUrl,
        summary = summary,
        instructions = instructions,
        safetyCues = safetyCues,
        defaultSets = defaultSets,
        repRangeStart = repRangeStart,
        repRangeEnd = repRangeEnd,
        defaultDurationMinutes = defaultDurationMinutes,
        restSeconds = restSeconds,
        source = source,
        originExerciseId = originExerciseId,
        createdAt = "2026-06-17T00:00:00Z",
        updatedAt = "2026-06-17T00:00:00Z",
        archivedAt = archivedAt
    )
}
