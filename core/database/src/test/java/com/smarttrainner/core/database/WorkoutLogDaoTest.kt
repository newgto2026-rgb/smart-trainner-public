package com.smarttrainner.core.database

import androidx.room.Room
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class WorkoutLogDaoTest {
    private lateinit var database: SmartTrainnerDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            SmartTrainnerDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertWithSetsStoresVariableSetRows() = runTest {
        database.workoutLogDao().upsertWithSets(
            log = WorkoutLogEntity(
                clientLogId = "client-log-1",
                sessionId = "local-default",
                plannedExerciseId = "2026-05-20_leg_press",
                exerciseId = "leg_press",
                performedDate = "2026-05-20",
                performedAt = "2026-05-20T09:00:00",
                sets = 4,
                reps = 10,
                weightKg = 20.0,
                durationMinutes = null,
                memo = "",
                completed = true
            ),
            setLogs = listOf(
                WorkoutSetLogEntity(workoutLogId = 0, setIndex = 1, reps = 10, weightKg = 20.0, durationMinutes = null, restSeconds = 60),
                WorkoutSetLogEntity(workoutLogId = 0, setIndex = 2, reps = 10, weightKg = 25.0, durationMinutes = null, restSeconds = 90),
                WorkoutSetLogEntity(workoutLogId = 0, setIndex = 3, reps = 8, weightKg = 30.0, durationMinutes = null, restSeconds = 120),
                WorkoutSetLogEntity(workoutLogId = 0, setIndex = 4, reps = 8, weightKg = 32.5, durationMinutes = null, restSeconds = 150)
            )
        )

        val result = database.workoutLogDao()
            .observeBetween(
                sessionId = "local-default",
                startDate = "2026-05-20",
                endDate = "2026-05-20"
            )
            .first()

        assertThat(result).hasSize(1)
        assertThat(result.single().setLogs.map { it.setIndex }).containsExactly(1, 2, 3, 4).inOrder()
        assertThat(result.single().setLogs.map { it.weightKg }).containsExactly(20.0, 25.0, 30.0, 32.5).inOrder()
        assertThat(result.single().setLogs.map { it.restSeconds }).containsExactly(60, 90, 120, 150).inOrder()
    }

    @Test
    fun observeBetweenFiltersBySession() = runTest {
        val log = WorkoutLogEntity(
            clientLogId = "client-log-1",
            sessionId = "local-default",
            plannedExerciseId = "2026-05-20_leg_press",
            exerciseId = "leg_press",
            performedDate = "2026-05-20",
            performedAt = "2026-05-20T09:00:00",
            sets = 1,
            reps = 10,
            weightKg = 20.0,
            durationMinutes = null,
            memo = "",
            completed = true
        )
        database.workoutLogDao().upsertWithSets(log, emptyList())
        database.workoutLogDao().upsertWithSets(
            log.copy(clientLogId = "client-log-2", sessionId = "google-user-1"),
            emptyList()
        )

        val localLogs = database.workoutLogDao()
            .observeBetween(
                sessionId = "local-default",
                startDate = "2026-05-20",
                endDate = "2026-05-20"
            )
            .first()

        assertThat(localLogs).hasSize(1)
        assertThat(localLogs.single().log.sessionId).isEqualTo("local-default")
    }

    @Test
    fun latestByExerciseReturnsMostRecentMatchingLogWithSets() = runTest {
        val oldLog = WorkoutLogEntity(
            clientLogId = "client-log-1",
            sessionId = "local-default",
            plannedExerciseId = "2026-05-20_leg_press",
            exerciseId = "leg_press",
            performedDate = "2026-05-20",
            performedAt = "2026-05-20T09:00:00",
            sets = 1,
            reps = 10,
            weightKg = 20.0,
            durationMinutes = null,
            memo = "",
            completed = true
        )
        val latestLog = oldLog.copy(
            clientLogId = "client-log-2",
            plannedExerciseId = "2026-05-27_leg_press",
            performedDate = "2026-05-27",
            performedAt = "2026-05-27T09:00:00",
            reps = 8,
            weightKg = 30.0
        )
        database.workoutLogDao().upsertWithSets(
            oldLog,
            listOf(WorkoutSetLogEntity(workoutLogId = 0, setIndex = 1, reps = 10, weightKg = 20.0, durationMinutes = null))
        )
        database.workoutLogDao().upsertWithSets(
            latestLog,
            listOf(WorkoutSetLogEntity(workoutLogId = 0, setIndex = 1, reps = 8, weightKg = 30.0, durationMinutes = null, restSeconds = 120))
        )

        val result = database.workoutLogDao().latestByExercise(
            sessionId = "local-default",
            exerciseId = "leg_press"
        )

        assertThat(result?.log?.plannedExerciseId).isEqualTo("2026-05-27_leg_press")
        assertThat(result?.setLogs?.single()?.reps).isEqualTo(8)
        assertThat(result?.setLogs?.single()?.weightKg).isEqualTo(30.0)
        assertThat(result?.setLogs?.single()?.restSeconds).isEqualTo(120)
    }

    @Test
    fun observeLatestByExerciseForSessionReturnsMostRecentLogPerExercise() = runTest {
        val oldLegLog = WorkoutLogEntity(
            clientLogId = "client-log-1",
            sessionId = "local-default",
            plannedExerciseId = "2026-05-20_leg_press",
            exerciseId = "leg_press",
            performedDate = "2026-05-20",
            performedAt = "2026-05-20T09:00:00",
            sets = 1,
            reps = 10,
            weightKg = 20.0,
            durationMinutes = null,
            memo = "",
            completed = true
        )
        val latestLegLog = oldLegLog.copy(
            clientLogId = "client-log-2",
            plannedExerciseId = "2026-05-27_leg_press",
            performedDate = "2026-05-27",
            performedAt = "2026-05-27T09:00:00",
            reps = 8,
            weightKg = 30.0
        )
        val chestLog = oldLegLog.copy(
            clientLogId = "client-log-3",
            plannedExerciseId = "2026-05-25_chest_press",
            exerciseId = "chest_press",
            performedDate = "2026-05-25",
            performedAt = "2026-05-25T09:00:00",
            reps = 12,
            weightKg = 25.0
        )
        database.workoutLogDao().upsertWithSets(oldLegLog, emptyList())
        database.workoutLogDao().upsertWithSets(
            latestLegLog,
            listOf(WorkoutSetLogEntity(workoutLogId = 0, setIndex = 1, reps = 8, weightKg = 30.0, durationMinutes = null, restSeconds = 120))
        )
        database.workoutLogDao().upsertWithSets(chestLog, emptyList())
        database.workoutLogDao().upsertWithSets(
            oldLegLog.copy(clientLogId = "client-log-4", sessionId = "google-user-1"),
            emptyList()
        )

        val result = database.workoutLogDao()
            .observeLatestByExerciseForSession(sessionId = "local-default")
            .first()

        assertThat(result.map { it.log.clientLogId }).containsExactly("client-log-2", "client-log-3").inOrder()
        assertThat(result.single { it.log.clientLogId == "client-log-2" }.setLogs.single().restSeconds).isEqualTo(120)
    }

    @Test
    fun samePlannedExerciseCanKeepMultiplePerformedLogs() = runTest {
        val firstLog = WorkoutLogEntity(
            clientLogId = "client-log-1",
            sessionId = "local-default",
            plannedExerciseId = "routine-day-1-leg_press",
            exerciseId = "leg_press",
            performedDate = "2026-05-20",
            performedAt = "2026-05-20T09:00:00",
            sets = 1,
            reps = 10,
            weightKg = 20.0,
            durationMinutes = null,
            memo = "",
            completed = true
        )
        val secondLog = firstLog.copy(
            clientLogId = "client-log-2",
            performedDate = "2026-05-21",
            performedAt = "2026-05-21T09:00:00",
            reps = 12,
            weightKg = 22.5
        )
        database.workoutLogDao().upsertWithSets(firstLog, emptyList())
        database.workoutLogDao().upsertWithSets(secondLog, emptyList())

        val result = database.workoutLogDao()
            .observeAll(sessionId = "local-default")
            .first()

        assertThat(result.map { it.log.clientLogId }).containsExactly("client-log-2", "client-log-1").inOrder()
    }

    @Test
    fun deleteRoutineDayLogsWithInstanceDoesNotDeleteLegacyPlannedIds() = runTest {
        val currentInstanceLog = WorkoutLogEntity(
            clientLogId = "client-log-1",
            sessionId = "local-default",
            plannedExerciseId = "2026-05-20_leg_press",
            routineDayInstanceId = "routine-day|template|cycle1|day1",
            exerciseId = "leg_press",
            performedDate = "2026-05-20",
            performedAt = "2026-05-20T09:00:00",
            sets = 1,
            reps = 10,
            weightKg = 20.0,
            durationMinutes = null,
            memo = "",
            completed = true
        )
        val otherInstanceLog = currentInstanceLog.copy(
            clientLogId = "client-log-2",
            routineDayInstanceId = "routine-day|template|cycle2|day1",
            performedAt = "2026-05-20T10:00:00"
        )
        val legacyLog = currentInstanceLog.copy(
            clientLogId = "client-log-3",
            routineDayInstanceId = null,
            performedAt = "2026-05-20T11:00:00"
        )
        database.workoutLogDao().upsertWithSets(currentInstanceLog, emptyList())
        database.workoutLogDao().upsertWithSets(otherInstanceLog, emptyList())
        database.workoutLogDao().upsertWithSets(legacyLog, emptyList())

        database.workoutLogDao().deleteRoutineDayLogs(
            sessionId = "local-default",
            routineDayInstanceId = "routine-day|template|cycle1|day1",
            plannedExerciseIds = listOf("2026-05-20_leg_press"),
            additionalExerciseIdPrefixPattern = "routine-added|template|cycle1|day1|%"
        )

        val result = database.workoutLogDao()
            .observeAll(sessionId = "local-default")
            .first()

        assertThat(result.map { it.log.clientLogId }).containsExactly("client-log-3", "client-log-2").inOrder()
    }

    @Test
    fun deleteRoutineDayLogsWithoutInstanceFallsBackToLegacyPlannedIds() = runTest {
        val legacyLog = WorkoutLogEntity(
            clientLogId = "client-log-1",
            sessionId = "local-default",
            plannedExerciseId = "2026-05-20_leg_press",
            routineDayInstanceId = null,
            exerciseId = "leg_press",
            performedDate = "2026-05-20",
            performedAt = "2026-05-20T09:00:00",
            sets = 1,
            reps = 10,
            weightKg = 20.0,
            durationMinutes = null,
            memo = "",
            completed = true
        )
        val instanceLog = legacyLog.copy(
            clientLogId = "client-log-2",
            routineDayInstanceId = "routine-day|template|cycle2|day1",
            performedAt = "2026-05-20T10:00:00"
        )
        database.workoutLogDao().upsertWithSets(legacyLog, emptyList())
        database.workoutLogDao().upsertWithSets(instanceLog, emptyList())

        database.workoutLogDao().deleteRoutineDayLogs(
            sessionId = "local-default",
            routineDayInstanceId = null,
            plannedExerciseIds = listOf("2026-05-20_leg_press"),
            additionalExerciseIdPrefixPattern = "routine-added|template|cycle1|day1|%"
        )

        val result = database.workoutLogDao()
            .observeAll(sessionId = "local-default")
            .first()

        assertThat(result.map { it.log.clientLogId }).containsExactly("client-log-2")
    }
}
