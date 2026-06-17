package com.smarttrainner.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.room.Room
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.database.SmartTrainnerDatabase
import com.smarttrainner.core.database.WorkoutLogDao
import com.smarttrainner.core.database.WorkoutLogEntity
import com.smarttrainner.core.database.WorkoutLogSetWrite
import com.smarttrainner.core.database.WorkoutLogWithSets
import com.smarttrainner.core.database.WorkoutSetLogEntity
import com.smarttrainner.core.datastore.ActiveSessionResolver
import com.smarttrainner.core.datastore.DEFAULT_USER_SESSION_ID
import com.smarttrainner.core.datastore.TrainingPreferencesDataSource
import com.smarttrainner.core.datastore.trainingDataStore
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.network.WorkoutLogDto
import com.smarttrainner.core.network.WorkoutLogListResponse
import com.smarttrainner.core.network.WorkoutLogNetworkApi
import com.smarttrainner.core.network.WorkoutLogRequest
import com.smarttrainner.core.network.WorkoutLogResponse
import com.smarttrainner.core.network.WorkoutSetLogDto
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultWorkoutLogRepositoryTest {
    private val clock = Clock.fixed(Instant.parse("2026-06-17T00:00:00Z"), ZoneOffset.UTC)
    private lateinit var context: Context
    private lateinit var database: SmartTrainnerDatabase
    private lateinit var dao: WorkoutLogDao
    private lateinit var networkApi: FakeWorkoutLogNetworkApi
    private lateinit var repository: DefaultWorkoutLogRepository

    @Before
    fun setUp() = runTest {
        context = RuntimeEnvironment.getApplication()
        context.trainingDataStore.edit { it.clear() }
        database = Room.inMemoryDatabaseBuilder(
            context,
            SmartTrainnerDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.workoutLogDao()
        networkApi = FakeWorkoutLogNetworkApi()
        repository = DefaultWorkoutLogRepository(
            workoutLogDao = dao,
            activeSessionResolver = ActiveSessionResolver(
                TrainingPreferencesDataSource(context = context, clock = clock)
            ),
            workoutLogNetworkApi = networkApi
        )
    }

    @After
    fun tearDown() = runTest {
        database.close()
        context.trainingDataStore.edit { it.clear() }
    }

    @Test
    fun saveWorkoutLog_omitsBlankPlannedExerciseIdForManualCalendarRecord() = runTest {
        val result = repository.saveWorkoutLog(
            workoutInput(
                plannedExerciseId = PlannedExerciseId(""),
                performedAt = LocalDateTime.parse("2026-06-17T12:00:00")
            )
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(networkApi.createRequests.single().plannedExerciseId).isNull()
        assertThat(dao.observeAll(DEFAULT_USER_SESSION_ID).first()).hasSize(1)
    }

    @Test
    fun updateWorkoutLog_preservesClientLogIdAndReplacesSets() = runTest {
        val originalInput = workoutInput(
            plannedExerciseId = PlannedExerciseId("routine-day-1-leg_press"),
            performedAt = LocalDateTime.parse("2026-06-17T12:00:00"),
            setEntries = listOf(
                WorkoutSetLog(order = 1, reps = 10, weightKg = 80.0, durationMinutes = null, restSeconds = 90),
                WorkoutSetLog(order = 2, reps = 8, weightKg = 85.0, durationMinutes = null, restSeconds = 120)
            )
        )
        assertThat(repository.saveWorkoutLog(originalInput).isSuccess).isTrue()
        val localBefore = dao.observeAll(DEFAULT_USER_SESSION_ID).first().single()
        val originalClientLogId = localBefore.log.clientLogId
        networkApi.createRequests.clear()

        val editedInput = originalInput.copy(
            sets = 1,
            reps = 12,
            weightKg = 90.0,
            memo = "edited",
            setEntries = listOf(
                WorkoutSetLog(order = 1, reps = 12, weightKg = 90.0, durationMinutes = null, restSeconds = 150)
            )
        )
        val result = repository.updateWorkoutLog(WorkoutLogId(localBefore.log.id), editedInput)

        assertThat(result.isSuccess).isTrue()
        assertThat(networkApi.createRequests.single().id).isEqualTo(originalClientLogId)
        val localAfter = dao.observeAll(DEFAULT_USER_SESSION_ID).first().single()
        assertThat(localAfter.log.id).isEqualTo(localBefore.log.id)
        assertThat(localAfter.log.clientLogId).isEqualTo(originalClientLogId)
        assertThat(localAfter.log.memo).isEqualTo("edited")
        assertThat(localAfter.log.syncPending).isFalse()
        assertThat(localAfter.setLogs.map { it.reps }).containsExactly(12)
        assertThat(localAfter.setLogs.map { it.restSeconds }).containsExactly(150)
    }

    @Test
    fun updateWorkoutLog_keepsPendingSyncWhenNetworkFails() = runTest {
        assertThat(repository.saveWorkoutLog(workoutInput()).isSuccess).isTrue()
        val localBefore = dao.observeAll(DEFAULT_USER_SESSION_ID).first().single()
        val originalClientLogId = localBefore.log.clientLogId
        networkApi.createRequests.clear()
        networkApi.createFailure = IOException("offline")

        val result = repository.updateWorkoutLog(
            id = WorkoutLogId(localBefore.log.id),
            input = workoutInput(memo = "offline edit")
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(networkApi.createRequests.single().id).isEqualTo(originalClientLogId)
        val localAfter = dao.observeAll(DEFAULT_USER_SESSION_ID).first().single()
        assertThat(localAfter.log.clientLogId).isEqualTo(originalClientLogId)
        assertThat(localAfter.log.memo).isEqualTo("offline edit")
        assertThat(localAfter.log.syncPending).isTrue()
    }

    @Test
    fun saveWorkoutLog_succeedsWhenMarkSyncedFailsAfterNetworkSuccess() = runTest {
        val failingDao = MarkSyncedFailingWorkoutLogDao()
        val repo = DefaultWorkoutLogRepository(
            workoutLogDao = failingDao,
            activeSessionResolver = ActiveSessionResolver(
                TrainingPreferencesDataSource(context = context, clock = clock)
            ),
            workoutLogNetworkApi = networkApi
        )

        val result = repo.saveWorkoutLog(workoutInput())

        assertThat(result.isSuccess).isTrue()
        assertThat(networkApi.createRequests).hasSize(1)
        assertThat(failingDao.markSyncedAttempts).isEqualTo(1)
        assertThat(failingDao.upsertedLog?.syncPending).isTrue()
    }

    private fun workoutInput(
        plannedExerciseId: PlannedExerciseId = PlannedExerciseId("routine-day-1-leg_press"),
        performedAt: LocalDateTime = LocalDateTime.parse("2026-06-17T12:00:00"),
        memo: String = "heavy day",
        setEntries: List<WorkoutSetLog> = listOf(
            WorkoutSetLog(order = 1, reps = 10, weightKg = 80.0, durationMinutes = null, restSeconds = 90)
        )
    ) = WorkoutLogInput(
        plannedExerciseId = plannedExerciseId,
        exerciseId = ExerciseId("leg_press"),
        performedAt = performedAt,
        sets = setEntries.size,
        reps = setEntries.firstOrNull()?.reps,
        weightKg = setEntries.firstOrNull()?.weightKg,
        durationMinutes = setEntries.sumOf { it.durationMinutes ?: 0 }.takeIf { it > 0 },
        memo = memo,
        completed = true,
        setEntries = setEntries,
        routineDayInstanceId = null
    )
}

private class FakeWorkoutLogNetworkApi : WorkoutLogNetworkApi {
    val createRequests = mutableListOf<WorkoutLogRequest>()
    var createFailure: IOException? = null

    override suspend fun getWorkoutLogs(sessionId: String): WorkoutLogListResponse =
        WorkoutLogListResponse(data = emptyList(), count = 0)

    override suspend fun createWorkoutLog(
        sessionId: String,
        request: WorkoutLogRequest
    ): WorkoutLogResponse {
        createRequests += request
        createFailure?.let { throw it }
        return WorkoutLogResponse(data = request.toDto(sessionId))
    }
}

private class MarkSyncedFailingWorkoutLogDao : WorkoutLogDao {
    var upsertedLog: WorkoutLogEntity? = null
    var markSyncedAttempts = 0

    override fun observeBetween(
        sessionId: String,
        startDate: String,
        endDate: String
    ): Flow<List<WorkoutLogWithSets>> = flowOf(emptyList())

    override fun observeAll(sessionId: String): Flow<List<WorkoutLogWithSets>> = flowOf(emptyList())

    override fun observeLatestByExerciseForSession(sessionId: String): Flow<List<WorkoutLogWithSets>> =
        flowOf(emptyList())

    override suspend fun pendingSyncLogs(sessionId: String): List<WorkoutLogWithSets> = emptyList()

    override suspend fun pendingSyncClientLogIds(sessionId: String): List<String> = emptyList()

    override suspend fun latestByExercise(sessionId: String, exerciseId: String): WorkoutLogWithSets? = null

    override suspend fun latestByPlannedExercise(
        sessionId: String,
        plannedExerciseId: String
    ): WorkoutLogWithSets? = null

    override suspend fun byId(sessionId: String, id: Long): WorkoutLogWithSets? = null

    override suspend fun upsert(log: WorkoutLogEntity): Long {
        upsertedLog = log.copy(id = 1L)
        return 1L
    }

    override suspend fun insertSetLogs(setLogs: List<WorkoutSetLogEntity>) = Unit

    override suspend fun markSynced(sessionId: String, clientLogId: String) {
        markSyncedAttempts += 1
        throw IOException("mark synced failed")
    }

    override suspend fun updateRoutineDayLogDate(
        sessionId: String,
        routineDayInstanceId: String,
        performedDate: String,
        performedAt: String
    ) = Unit

    override suspend fun routineDayWorkoutLogIds(
        sessionId: String,
        routineDayInstanceId: String?,
        plannedExerciseIds: List<String>,
        additionalExerciseIdPrefixPattern: String
    ): List<Long> = emptyList()

    override suspend fun routineCycleWorkoutLogIds(
        sessionId: String,
        routineDayInstancePrefixPattern: String,
        plannedExerciseIds: List<String>,
        additionalExerciseIdPrefixPattern: String
    ): List<Long> = emptyList()

    override suspend fun deleteSetLogsByWorkoutLogIds(workoutLogIds: List<Long>) = Unit

    override suspend fun deleteWorkoutLogsByIds(workoutLogIds: List<Long>) = Unit

    override suspend fun upsertAllWithSets(logsWithSets: List<WorkoutLogSetWrite>) = Unit
}

private fun WorkoutLogRequest.toDto(sessionId: String) = WorkoutLogDto(
    id = id,
    sessionId = sessionId,
    date = date,
    exerciseId = exerciseId,
    plannedExerciseId = plannedExerciseId,
    routineDayInstanceId = routineDayInstanceId,
    sets = sets.map {
        WorkoutSetLogDto(
            setIndex = it.setIndex,
            reps = it.reps,
            weightKg = it.weightKg,
            durationMinutes = it.durationMinutes,
            restSeconds = it.restSeconds,
            completed = it.completed
        )
    },
    notes = notes,
    createdAt = "2026-06-17T00:00:00Z",
    updatedAt = "2026-06-17T00:00:00Z"
)
