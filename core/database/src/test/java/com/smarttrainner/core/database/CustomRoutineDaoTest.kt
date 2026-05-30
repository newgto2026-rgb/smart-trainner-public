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
class CustomRoutineDaoTest {
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
    fun upsertFullStoresRoutineDaysAndExercises() = runTest {
        database.customRoutineDao().upsertFull(
            routine = routine("custom-1", "local-default"),
            days = listOf(
                dayWrite("custom-1", dayIndex = 0, "squat", "squat"),
                dayWrite("custom-1", dayIndex = 1, "press")
            )
        )

        val routines = database.customRoutineDao().observeForSession("local-default").first()

        assertThat(routines).hasSize(1)
        assertThat(routines.single().days).hasSize(2)
        assertThat(routines.single().days[0].exercises.map { it.slotIndex }).containsExactly(0, 1)
        assertThat(routines.single().days[0].exercises.map { it.exerciseId }).containsExactly("squat", "squat")
    }

    @Test
    fun observeForSessionKeepsRoutinesIsolatedBySession() = runTest {
        database.customRoutineDao().upsertFull(routine("custom-1", "local-default"), listOf(dayWrite("custom-1", 0, "squat")))
        database.customRoutineDao().upsertFull(routine("custom-2", "google-user-1"), listOf(dayWrite("custom-2", 0, "press")))

        val localRoutines = database.customRoutineDao().observeForSession("local-default").first()

        assertThat(localRoutines.map { it.routine.id }).containsExactly("custom-1")
    }

    @Test
    fun upsertFullReplacesExistingDays() = runTest {
        database.customRoutineDao().upsertFull(
            routine = routine("custom-1", "local-default"),
            days = listOf(dayWrite("custom-1", 0, "squat"), dayWrite("custom-1", 1, "press"))
        )

        database.customRoutineDao().upsertFull(
            routine = routine("custom-1", "local-default"),
            days = listOf(dayWrite("custom-1", 0, "row"))
        )

        val routine = database.customRoutineDao().observeForSession("local-default").first().single()
        assertThat(routine.days).hasSize(1)
        assertThat(routine.days.single().exercises.single().exerciseId).isEqualTo("row")
    }

    private fun routine(id: String, sessionId: String) = CustomRoutineEntity(
        id = id,
        sessionId = sessionId,
        name = "My routine",
        description = "",
        createdAt = "2026-05-29T00:00:00Z",
        updatedAt = "2026-05-29T00:00:00Z"
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
}
