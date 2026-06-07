package com.smarttrainner.core.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.CurrentRoutineCycle
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.routineAdditionalExerciseIdPrefix
import com.smarttrainner.core.model.routineDayInstanceId
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoutineCycleProjectionUseCasesTest {
    private val resolver = ResolveCurrentRoutineCycleUseCase()

    @Test
    fun observeCurrentRoutineCycle_prefersCycleStartOverRoutineStart() = runTest {
        val repository = FakeRoutineCycleRepository(
            progress = progress(
                startedAt = Instant.parse("2026-05-01T00:00:00Z"),
                cycleStartedAt = Instant.parse("2026-05-25T00:00:00Z")
            )
        )
        val useCase = ObserveCurrentRoutineCycleUseCase(
            routineProgressRepository = repository,
            cyclePlanRepository = repository,
            workoutLogRepository = repository,
            resolveCurrentRoutineCycle = resolver,
            clock = Clock.fixed(Instant.parse("2026-06-02T00:00:00Z"), ZoneOffset.UTC)
        )

        val currentCycle = useCase(ZoneOffset.UTC).first()

        assertThat(repository.requestedTemplateIds).containsExactly("balanced")
        assertThat(repository.requestedCycleStartDates).containsExactly(LocalDate.of(2026, 5, 25))
        assertThat(currentCycle.plan.cycleStartDate).isEqualTo(LocalDate.of(2026, 5, 25))
    }

    @Test
    fun observeCurrentRoutineCycle_usesClockWhenProgressStartIsMissing() = runTest {
        val repository = FakeRoutineCycleRepository(
            progress = progress(startedAt = null, cycleStartedAt = null)
        )
        val useCase = ObserveCurrentRoutineCycleUseCase(
            routineProgressRepository = repository,
            cyclePlanRepository = repository,
            workoutLogRepository = repository,
            resolveCurrentRoutineCycle = resolver,
            clock = Clock.fixed(Instant.parse("2026-06-02T00:00:00Z"), ZoneOffset.UTC)
        )

        val currentCycle = useCase(ZoneOffset.UTC).first()

        assertThat(repository.requestedCycleStartDates).containsExactly(LocalDate.of(2026, 6, 2))
        assertThat(currentCycle.plan.cycleStartDate).isEqualTo(LocalDate.of(2026, 6, 2))
    }

    @Test
    fun resolveCurrentRoutineCycle_filtersLogsToCurrentTemplateAndCycle() {
        val plan = plan(
            templateId = "balanced",
            cycleStartDate = LocalDate.of(2026, 5, 20)
        )
        val dayOneId = routineDayInstanceId("balanced", cycleNumber = 4, dayNumber = 1)
        val dayTwo = plan.days[1]
        val progress = progress(
            dayIndex = 1,
            cycleNumber = 4,
            cycleStartedAt = Instant.parse("2026-05-20T00:00:00Z"),
            routineDayDates = mapOf(
                dayOneId to LocalDate.of(2026, 5, 20),
                routineDayInstanceId("balanced", 4, 2) to LocalDate.of(2026, 5, 22)
            )
        )
        val currentDayLegacyLog = log(
            id = 1,
            planned = dayTwo.exercises.first(),
            performedAt = LocalDateTime.of(2026, 5, 22, 12, 0)
        )
        val currentCycleInstanceLog = log(
            id = 2,
            planned = plan.days.first().exercises.first(),
            performedAt = LocalDateTime.of(2026, 5, 20, 12, 0),
            routineDayInstanceId = dayOneId
        )
        val previousCycleLog = log(
            id = 3,
            planned = plan.days.first().exercises.first(),
            performedAt = LocalDateTime.of(2026, 5, 18, 12, 0),
            routineDayInstanceId = routineDayInstanceId("balanced", 3, 1)
        )
        val otherTemplateLog = log(
            id = 4,
            planned = plan.days.first().exercises.first(),
            performedAt = LocalDateTime.of(2026, 5, 22, 12, 0),
            routineDayInstanceId = routineDayInstanceId("other", 4, 1)
        )
        val staleLegacyLog = log(
            id = 5,
            planned = dayTwo.exercises.first(),
            performedAt = LocalDateTime.of(2026, 5, 19, 12, 0)
        )

        val currentCycle = resolver(
            progress = progress,
            plan = plan,
            logs = listOf(
                currentDayLegacyLog,
                currentCycleInstanceLog,
                previousCycleLog,
                otherTemplateLog,
                staleLegacyLog
            ),
            zone = ZoneOffset.UTC
        )

        assertThat(currentCycle.currentCycleLogs.map { it.id.value })
            .containsExactly(1L, 2L)
        assertThat(currentCycle.currentDayCompletedPlannedExerciseIds)
            .containsExactly(dayTwo.exercises.first().id)
        assertThat(currentCycle.currentRoutineDayDate).isEqualTo(LocalDate.of(2026, 5, 22))
        assertThat(currentCycle.previousRoutineDayDate).isEqualTo(LocalDate.of(2026, 5, 20))
    }

    @Test
    fun resolveCurrentRoutineCycle_ignoresPreexistingResidueAfterRoutineSwitch() {
        val plan = plan(
            templateId = "beginner",
            cycleStartDate = LocalDate.of(2026, 5, 24)
        )
        val progress = progress(
            templateId = "beginner",
            cycleNumber = 4,
            dayIndex = 0,
            lastCompletedDayIndex = 0,
            lastCompletedCycleNumber = 3,
            lastCompletedAt = Instant.parse("2026-05-23T12:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-24T00:00:00Z"),
            routineDayDates = mapOf(
                routineDayInstanceId("custom", 4, 1) to LocalDate.of(2026, 5, 22),
                routineDayInstanceId("beginner", 3, 1) to LocalDate.of(2026, 5, 20)
            )
        )
        val oldTemplateResidue = log(
            id = 1,
            planned = plan.days.first().exercises.first(),
            performedAt = LocalDateTime.of(2026, 5, 22, 12, 0),
            routineDayInstanceId = routineDayInstanceId("custom", 4, 1)
        )
        val previousCycleResidue = log(
            id = 2,
            planned = plan.days.first().exercises.first(),
            performedAt = LocalDateTime.of(2026, 5, 20, 12, 0),
            routineDayInstanceId = routineDayInstanceId("beginner", 3, 1)
        )
        val staleLegacyResidue = log(
            id = 3,
            planned = plan.days.first().exercises.first(),
            performedAt = LocalDateTime.of(2026, 5, 23, 12, 0)
        )

        val currentCycle = resolver(
            progress = progress,
            plan = plan,
            logs = listOf(oldTemplateResidue, previousCycleResidue, staleLegacyResidue),
            zone = ZoneOffset.UTC
        )

        assertThat(currentCycle.currentCycleLogs).isEmpty()
        assertThat(currentCycle.currentRoutineDayDate).isNull()
        assertThat(currentCycle.previousRoutineDayDate).isNull()
        assertThat(currentCycle.latestCompletion).isNull()
        assertThat(currentCycle.allLogs.map { it.id.value })
            .containsExactly(1L, 2L, 3L)
    }

    @Test
    fun resolveCurrentRoutineCycle_handlesEmptyPlanWithoutLeakingLogs() {
        val emptyPlan = plan(
            templateId = "balanced",
            cycleStartDate = LocalDate.of(2026, 5, 20)
        ).copy(days = emptyList())
        val progress = progress(
            dayIndex = 9,
            cycleNumber = 4,
            cycleStartedAt = Instant.parse("2026-05-20T00:00:00Z")
        )
        val legacyLog = log(
            id = 1,
            planned = plannedExercise(
                id = PlannedExerciseId("legacy-chest"),
                exerciseId = "chest_press"
            ),
            performedAt = LocalDateTime.of(2026, 5, 21, 12, 0)
        )

        val currentCycle = resolver(
            progress = progress,
            plan = emptyPlan,
            logs = listOf(legacyLog),
            zone = ZoneOffset.UTC
        )

        assertThat(currentCycle.currentDayIndex).isEqualTo(0)
        assertThat(currentCycle.currentDay).isNull()
        assertThat(currentCycle.currentRoutineDayInstanceId).isNull()
        assertThat(currentCycle.currentCycleLogs).isEmpty()
        assertThat(currentCycle.currentDayCompletedPlannedExerciseIds).isEmpty()
    }

    @Test
    fun resolveCurrentRoutineCycle_includesCurrentCycleAdditionalLogsOnly() {
        val plan = plan(
            templateId = "balanced",
            cycleStartDate = LocalDate.of(2026, 5, 20)
        )
        val progress = progress(
            templateId = "balanced",
            cycleNumber = 4,
            cycleStartedAt = Instant.parse("2026-05-20T00:00:00Z")
        )
        val additionalExercise = plannedExercise(
            id = PlannedExerciseId(
                routineAdditionalExerciseIdPrefix(
                    templateId = "balanced",
                    cycleNumber = 4,
                    dayNumber = 1
                ) + "lateral_raise"
            ),
            exerciseId = "lateral_raise"
        )
        val currentAdditionalLog = log(
            id = 1,
            planned = additionalExercise,
            performedAt = LocalDateTime.of(2026, 5, 20, 12, 0)
        )
        val staleAdditionalLog = log(
            id = 2,
            planned = additionalExercise,
            performedAt = LocalDateTime.of(2026, 5, 19, 12, 0)
        )
        val unrelatedLegacyLog = log(
            id = 3,
            planned = plannedExercise(
                id = PlannedExerciseId("unrelated-legacy"),
                exerciseId = "row"
            ),
            performedAt = LocalDateTime.of(2026, 5, 20, 12, 0)
        )

        val currentCycle = resolver(
            progress = progress,
            plan = plan,
            logs = listOf(currentAdditionalLog, staleAdditionalLog, unrelatedLegacyLog),
            zone = ZoneOffset.UTC
        )

        assertThat(currentCycle.currentCycleLogs.map { it.id.value })
            .containsExactly(1L)
    }

    @Test
    fun resolveCurrentRoutineCycle_exposesLatestCompletionOnlyForCurrentCycle() {
        val plan = plan(templateId = "balanced", cycleStartDate = LocalDate.of(2026, 5, 20))
        val currentCycleProgress = progress(
            dayIndex = 1,
            cycleNumber = 4,
            lastCompletedDayIndex = 0,
            lastCompletedCycleNumber = 4,
            lastCompletedAt = Instant.parse("2026-05-20T12:00:00Z")
        )
        val nextCycleProgress = progress(
            dayIndex = 0,
            cycleNumber = 5,
            lastCompletedDayIndex = 1,
            lastCompletedCycleNumber = 4,
            lastCompletedAt = Instant.parse("2026-05-21T12:00:00Z")
        )

        val currentCycle = resolver(currentCycleProgress, plan, emptyList(), ZoneOffset.UTC)
        val nextCycle = resolver(nextCycleProgress, plan, emptyList(), ZoneOffset.UTC)

        assertThat(currentCycle.latestCompletion?.cycleNumber).isEqualTo(4)
        assertThat(currentCycle.latestCompletion?.dayNumber).isEqualTo(1)
        assertThat(nextCycle.latestCompletion).isNull()
    }
}

private class FakeRoutineCycleRepository(
    progress: RoutineProgress
) : RoutineProgressRepository,
    CyclePlanRepository,
    WorkoutLogRepository {
    private val progress = MutableStateFlow(progress)
    val requestedTemplateIds = mutableListOf<String>()
    val requestedCycleStartDates = mutableListOf<LocalDate>()

    override fun observeRoutineProgress(): Flow<RoutineProgress> = progress

    override fun observeCurrentCyclePlan(
        templateId: String,
        cycleStartDate: LocalDate
    ): Flow<CyclePlan> {
        requestedTemplateIds += templateId
        requestedCycleStartDates += cycleStartDate
        return flowOf(plan(templateId, cycleStartDate))
    }

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = flowOf(emptyList())

    override fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>> = flowOf(emptyList())
}

private fun progress(
    templateId: String = "balanced",
    dayIndex: Int = 0,
    cycleNumber: Int = 1,
    lastCompletedDayIndex: Int? = null,
    lastCompletedCycleNumber: Int? = null,
    lastCompletedAt: Instant? = null,
    startedAt: Instant? = Instant.parse("2026-05-20T00:00:00Z"),
    cycleStartedAt: Instant? = Instant.parse("2026-05-20T00:00:00Z"),
    routineDayDates: Map<String, LocalDate> = emptyMap()
): RoutineProgress = RoutineProgress(
    templateId = templateId,
    dayIndex = dayIndex,
    lastCompletedDayIndex = lastCompletedDayIndex,
    lastCompletedAt = lastCompletedAt,
    cycleNumber = cycleNumber,
    lastCompletedCycleNumber = lastCompletedCycleNumber,
    startedAt = startedAt,
    cycleStartedAt = cycleStartedAt,
    routineDayDates = routineDayDates
)

private fun plan(
    templateId: String,
    cycleStartDate: LocalDate
): CyclePlan {
    val chest = plannedExercise(
        id = PlannedExerciseId("${cycleStartDate}_chest_press"),
        exerciseId = "chest_press"
    )
    val row = plannedExercise(
        id = PlannedExerciseId("${cycleStartDate.plusDays(1)}_row"),
        exerciseId = "row"
    )
    return CyclePlan(
        id = PlanId("$templateId-$cycleStartDate"),
        templateId = templateId,
        name = templateId,
        cycleStartDate = cycleStartDate,
        days = listOf(
            WorkoutDayPlan(
                date = cycleStartDate,
                title = "Day 1",
                focus = "Push",
                exercises = listOf(chest),
                dayNumber = 1
            ),
            WorkoutDayPlan(
                date = cycleStartDate.plusDays(1),
                title = "Day 2",
                focus = "Pull",
                exercises = listOf(row),
                dayNumber = 2
            )
        )
    )
}

private fun plannedExercise(
    id: PlannedExerciseId,
    exerciseId: String
): PlannedExercise {
    val exercise = Exercise(
        id = ExerciseId(exerciseId),
        name = exerciseId,
        muscleGroup = MuscleGroup.CHEST,
        equipment = EquipmentType.MACHINE,
        difficulty = DifficultyLevel.BEGINNER,
        imageKey = exerciseId,
        summary = "",
        instructions = emptyList(),
        safetyCues = emptyList(),
        defaultSets = 3,
        defaultRepRange = 8..12,
        defaultDurationMinutes = null,
        restSeconds = 90
    )
    return PlannedExercise(
        id = id,
        exercise = exercise,
        sets = 3,
        repRange = 8..12,
        durationMinutes = null,
        restSeconds = 90,
        note = ""
    )
}

private fun log(
    id: Long,
    planned: PlannedExercise,
    performedAt: LocalDateTime,
    routineDayInstanceId: String? = null
): WorkoutLog = WorkoutLog(
    id = WorkoutLogId(id),
    sessionId = UserSessionId("local-default"),
    plannedExerciseId = planned.id,
    exerciseId = planned.exercise.id,
    performedAt = performedAt,
    sets = 3,
    reps = 10,
    weightKg = 50.0,
    durationMinutes = null,
    memo = "",
    completed = true,
    routineDayInstanceId = routineDayInstanceId
)
