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
class CustomExerciseDaoTest {
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
    fun observeActiveForOwnerKeepsCustomExercisesIsolatedByOwner() = runTest {
        val dao = database.customExerciseDao()
        dao.upsert(exercise(id = "custom-exercise-1", ownerSessionId = "local-default", name = "Mine"))
        dao.upsert(exercise(id = "custom-exercise-1", ownerSessionId = "friend-session", name = "Friend"))

        val localExercises = dao.observeActiveForOwner("local-default").first()
        val friendExercises = dao.observeActiveForOwner("friend-session").first()

        assertThat(localExercises.map { it.name }).containsExactly("Mine")
        assertThat(friendExercises.map { it.name }).containsExactly("Friend")
    }

    @Test
    fun observeActiveForOwnerExcludesArchivedExercisesAndMarksDeletePending() = runTest {
        val dao = database.customExerciseDao()
        dao.upsert(exercise(id = "custom-exercise-1", ownerSessionId = "local-default"))

        val updatedRows = dao.markPendingArchive(
            ownerSessionId = "local-default",
            id = "custom-exercise-1",
            archivedAt = "2026-06-17T00:00:00Z"
        )

        assertThat(updatedRows).isEqualTo(1)
        assertThat(dao.observeActiveForOwner("local-default").first()).isEmpty()
        assertThat(dao.pendingSyncForOwner("local-default").single().syncState)
            .isEqualTo(CUSTOM_EXERCISE_SYNC_PENDING_DELETE)
    }

    @Test
    fun pendingSyncForOwnerReturnsOnlyUnsyncedRowsForThatOwner() = runTest {
        val dao = database.customExerciseDao()
        dao.upsert(exercise(id = "pending-local", ownerSessionId = "local-default"))
        dao.upsert(
            exercise(
                id = "synced-local",
                ownerSessionId = "local-default",
                syncState = CUSTOM_EXERCISE_SYNCED
            )
        )
        dao.upsert(exercise(id = "pending-other", ownerSessionId = "other-session"))

        val pending = dao.pendingSyncForOwner("local-default")

        assertThat(pending.map { it.id }).containsExactly("pending-local")
    }

    @Test
    fun markPendingArchiveAndRemoveReferencesPrunesRoutinesAndWorkoutLogsForOwner() = runTest {
        val exerciseDao = database.customExerciseDao()
        val routineDao = database.customRoutineDao()
        val workoutLogDao = database.workoutLogDao()
        exerciseDao.upsert(exercise(id = "custom-exercise-1", ownerSessionId = "local-default"))
        routineDao.upsertFull(
            routine = routine(id = "custom-routine-1", sessionId = "local-default"),
            days = listOf(dayWrite("custom-routine-1", 0, "custom-exercise-1", "leg_press"))
        )
        routineDao.upsertFull(
            routine = routine(id = "custom-routine-2", sessionId = "other-session"),
            days = listOf(dayWrite("custom-routine-2", 0, "custom-exercise-1"))
        )
        workoutLogDao.upsertWithSets(
            log = workoutLog(clientLogId = "custom-log", sessionId = "local-default", exerciseId = "custom-exercise-1"),
            setLogs = listOf(WorkoutSetLogEntity(workoutLogId = 0, setIndex = 1, reps = 10, weightKg = null, durationMinutes = null))
        )
        workoutLogDao.upsertWithSets(
            log = workoutLog(clientLogId = "seed-log", sessionId = "local-default", exerciseId = "leg_press"),
            setLogs = emptyList()
        )

        val updatedRows = exerciseDao.markPendingArchiveAndRemoveReferences(
            ownerSessionId = "local-default",
            id = "custom-exercise-1",
            archivedAt = "2026-06-17T00:00:00Z"
        )

        assertThat(updatedRows).isEqualTo(1)
        val localRoutine = routineDao.observeForSession("local-default").first().single()
        assertThat(localRoutine.routine.syncState).isEqualTo(CUSTOM_ROUTINE_SYNC_PENDING_UPSERT)
        assertThat(localRoutine.days.single().exercises.map { it.exerciseId }).containsExactly("leg_press")
        val otherRoutine = routineDao.observeForSession("other-session").first().single()
        assertThat(otherRoutine.days.single().exercises.map { it.exerciseId }).containsExactly("custom-exercise-1")
        assertThat(workoutLogDao.observeAll("local-default").first().map { it.log.clientLogId })
            .containsExactly("seed-log")
    }

    private fun exercise(
        id: String,
        ownerSessionId: String,
        name: String = "Custom exercise",
        syncState: String = CUSTOM_EXERCISE_SYNC_PENDING_UPSERT
    ) = CustomExerciseEntity(
        id = id,
        ownerSessionId = ownerSessionId,
        source = "USER_CREATED",
        originExerciseId = null,
        name = name,
        primaryMuscleGroup = "FULL_BODY",
        secondaryMuscleGroups = "",
        equipment = "BODYWEIGHT",
        difficulty = "BEGINNER",
        imageKey = id,
        imageUri = null,
        summary = "summary",
        instructions = "step",
        safetyCues = "cue",
        defaultSets = 3,
        repRangeStart = 8,
        repRangeEnd = 12,
        defaultDurationMinutes = null,
        restSeconds = 90,
        createdAt = "2026-06-17T00:00:00Z",
        updatedAt = "2026-06-17T00:00:00Z",
        archivedAt = null,
        syncState = syncState
    )

    private fun routine(id: String, sessionId: String) = CustomRoutineEntity(
        id = id,
        sessionId = sessionId,
        name = "My routine",
        description = "",
        createdAt = "2026-06-17T00:00:00Z",
        updatedAt = "2026-06-17T00:00:00Z",
        syncState = CUSTOM_ROUTINE_SYNCED
    )

    private fun dayWrite(
        routineId: String,
        dayIndex: Int,
        vararg exerciseIds: String
    ): CustomRoutineDayWrite {
        val dayId = "$routineId-day-${dayIndex + 1}"
        return CustomRoutineDayWrite(
            day = CustomRoutineDayEntity(
                id = dayId,
                routineId = routineId,
                dayIndex = dayIndex,
                title = "${dayIndex + 1}일차",
                focus = "focus",
                primaryFocus = "FULL_BODY",
                secondaryFocuses = "",
                minRecoveryHours = 24
            ),
            exercises = exerciseIds.mapIndexed { slotIndex, exerciseId ->
                CustomRoutineExerciseEntity(
                    id = "$dayId-slot-${slotIndex + 1}",
                    dayId = dayId,
                    slotIndex = slotIndex,
                    exerciseId = exerciseId,
                    sets = 3,
                    repRangeStart = 8,
                    repRangeEnd = 12,
                    durationMinutes = null,
                    restSeconds = 90,
                    note = ""
                )
            }
        )
    }

    private fun workoutLog(
        clientLogId: String,
        sessionId: String,
        exerciseId: String
    ) = WorkoutLogEntity(
        clientLogId = clientLogId,
        sessionId = sessionId,
        plannedExerciseId = "planned-$exerciseId",
        exerciseId = exerciseId,
        performedDate = "2026-06-17",
        performedAt = "2026-06-17T09:00:00",
        sets = 1,
        reps = 10,
        weightKg = null,
        durationMinutes = null,
        memo = "",
        completed = true,
        syncPending = false
    )
}
