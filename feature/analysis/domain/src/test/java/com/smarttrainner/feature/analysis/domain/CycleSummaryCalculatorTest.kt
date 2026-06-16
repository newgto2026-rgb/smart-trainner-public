package com.smarttrainner.feature.analysis.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.CurrentRoutineCycle
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.ExerciseLoadType
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.model.UserSessionId
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Test

class CycleSummaryCalculatorTest {
    private val calculator = CycleSummaryCalculator()
    private val cycleStart = LocalDate.of(2026, 5, 18)

    @Test
    fun calculate_countsCompletionVolumeAndMuscleBalance() {
        val exercise = exercise("leg_press", MuscleGroup.LOWER_BODY)
        val planned = PlannedExercise(
            id = PlannedExerciseId("2026-05-18_leg_press"),
            exercise = exercise,
            sets = 3,
            repRange = 10..12,
            durationMinutes = null,
            restSeconds = 90,
            note = ""
        )
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "beginner",
            name = "초보 3일 사이클",
            cycleStartDate = cycleStart,
            days = listOf(
                WorkoutDayPlan(
                    date = cycleStart,
                    title = "전신 A",
                    focus = "전신",
                    exercises = listOf(planned)
                )
            )
        )
        val logs = listOf(
            WorkoutLog(
                id = WorkoutLogId(1),
                sessionId = UserSessionId("local-default"),
                plannedExerciseId = planned.id,
                exerciseId = exercise.id,
                performedAt = LocalDateTime.of(2026, 5, 18, 20, 0),
                sets = 4,
                reps = null,
                weightKg = null,
                durationMinutes = null,
                memo = "",
                completed = true,
                setEntries = listOf(
                    WorkoutSetLog(order = 1, reps = 10, weightKg = 70.0, durationMinutes = null),
                    WorkoutSetLog(order = 2, reps = 10, weightKg = 80.0, durationMinutes = null),
                    WorkoutSetLog(order = 3, reps = 8, weightKg = 90.0, durationMinutes = null),
                    WorkoutSetLog(order = 4, reps = 8, weightKg = 90.0, durationMinutes = null)
                )
            )
        )

        val result = calculate(
            plan = plan,
            logs = logs,
            progress = progress(templateId = "beginner"),
            zone = ZoneOffset.UTC
        )

        assertThat(result.plannedExerciseCount).isEqualTo(1)
        assertThat(result.completedExerciseCount).isEqualTo(1)
        assertThat(result.completionRate).isEqualTo(100)
        assertThat(result.totalSets).isEqualTo(4)
        assertThat(result.totalVolumeKg).isEqualTo(2940.0)
        assertThat(result.muscleBalance[MuscleGroup.LOWER_BODY]).isEqualTo(1)
    }

    @Test
    fun calculate_usesEffectiveVolumeForAssistedLoadExercises() {
        val planned = plannedExercise(
            id = "assisted_pullup",
            muscleGroup = MuscleGroup.BACK,
            loadType = ExerciseLoadType.ASSISTANCE_LOAD
        )
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "assisted",
            name = "Assisted cycle",
            cycleStartDate = cycleStart,
            days = listOf(
                WorkoutDayPlan(
                    date = cycleStart,
                    title = "Pull",
                    focus = "Back",
                    exercises = listOf(planned)
                )
            )
        )
        val logs = listOf(
            WorkoutLog(
                id = WorkoutLogId(1),
                sessionId = UserSessionId("local-default"),
                plannedExerciseId = planned.id,
                exerciseId = planned.exercise.id,
                performedAt = LocalDateTime.of(2026, 5, 18, 20, 0),
                sets = 1,
                reps = 5,
                weightKg = 60.0,
                durationMinutes = null,
                memo = "",
                completed = true
            )
        )

        val result = calculate(
            plan = plan,
            logs = logs,
            progress = progress(templateId = "assisted"),
            zone = ZoneOffset.UTC,
            bodyWeightKg = 80.0
        )

        assertThat(result.totalVolumeKg).isEqualTo(100.0)
    }

    @Test
    fun calculate_emptyLogsReturnsCoachingPrompt() {
        val plan = CyclePlan(
                id = PlanId("plan"),
                templateId = "intro",
                name = "입문",
                cycleStartDate = cycleStart,
                days = emptyList()
        )
        val result = calculate(
            plan = plan,
            logs = emptyList(),
            progress = progress(templateId = "intro"),
            zone = ZoneOffset.UTC
        )

        assertThat(result.completionRate).isEqualTo(0)
        assertThat(result.insight).contains("사이클")
    }

    @Test
    fun calculate_promptsForFirstRecordWhenPlanHasExercises() {
        val planned = plannedExercise(id = "leg_press", muscleGroup = MuscleGroup.LOWER_BODY)
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "intro",
            name = "입문",
            cycleStartDate = cycleStart,
            days = listOf(
                WorkoutDayPlan(
                    date = cycleStart,
                    title = "전신",
                    focus = "전신",
                    exercises = listOf(planned)
                )
            )
        )

        val result = calculate(
            plan = plan,
            logs = emptyList(),
            progress = progress(templateId = "intro"),
            zone = ZoneOffset.UTC
        )

        assertThat(result.plannedExerciseCount).isEqualTo(1)
        assertThat(result.completedExerciseCount).isEqualTo(0)
        assertThat(result.insight).contains("첫 기록")
    }

    @Test
    fun calculate_countsSecondaryMusclesInMuscleBalance() {
        val exercise = exercise(
            id = "conventional_deadlift",
            muscleGroup = MuscleGroup.FULL_BODY,
            muscleGroups = listOf(
                MuscleGroup.FULL_BODY,
                MuscleGroup.LOWER_BODY,
                MuscleGroup.BACK,
                MuscleGroup.CORE
            )
        )
        val planned = PlannedExercise(
            id = PlannedExerciseId("2026-05-18_conventional_deadlift"),
            exercise = exercise,
            sets = 3,
            repRange = 5..8,
            durationMinutes = null,
            restSeconds = 150,
            note = ""
        )
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "custom",
            name = "전신 루틴",
            cycleStartDate = cycleStart,
            days = listOf(
                WorkoutDayPlan(
                    date = cycleStart,
                    title = "전신",
                    focus = "전신",
                    exercises = listOf(planned)
                )
            )
        )

        val result = calculate(
            plan = plan,
            logs = listOf(completedLog(id = 1, planned = planned)),
            progress = progress(templateId = "custom"),
            zone = ZoneOffset.UTC
        )

        assertThat(result.muscleBalance[MuscleGroup.FULL_BODY]).isEqualTo(1)
        assertThat(result.muscleBalance[MuscleGroup.LOWER_BODY]).isEqualTo(1)
        assertThat(result.muscleBalance[MuscleGroup.BACK]).isEqualTo(1)
        assertThat(result.muscleBalance[MuscleGroup.CORE]).isEqualTo(1)
    }

    @Test
    fun calculate_excludesFullBodyFromWeakestMuscleInsight() {
        val completedGroups = listOf(
            MuscleGroup.LOWER_BODY,
            MuscleGroup.BACK,
            MuscleGroup.CHEST,
            MuscleGroup.SHOULDERS,
            MuscleGroup.BICEPS,
            MuscleGroup.TRICEPS,
            MuscleGroup.FOREARMS,
            MuscleGroup.CORE
        )
        val completedExercises = completedGroups.map { group ->
            plannedExercise(id = "completed_${group.name}", muscleGroup = group)
        }
        val extraExercises = (1..4).map { index ->
            plannedExercise(id = "extra_lower_$index", muscleGroup = MuscleGroup.LOWER_BODY)
        }
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "balanced",
            name = "균형 루틴",
            cycleStartDate = cycleStart,
            days = listOf(
                WorkoutDayPlan(
                    date = cycleStart,
                    title = "균형",
                    focus = "전신",
                    exercises = completedExercises + extraExercises
                )
            )
        )
        val logs = completedExercises.mapIndexed { index, planned ->
            completedLog(id = index.toLong() + 1, planned = planned)
        }

        val result = calculate(
            plan = plan,
            logs = logs,
            progress = progress(templateId = "balanced"),
            zone = ZoneOffset.UTC
        )

        assertThat(result.completionRate).isLessThan(80)
        assertThat(result.insight).doesNotContain(MuscleGroup.FULL_BODY.displayName)
        assertThat(result.insight).contains(MuscleGroup.LOWER_BODY.displayName)
    }

    @Test
    fun calculate_usesCurrentCycleLogsAndCapsCompletionToPlanSize() {
        val first = plannedExercise(id = "leg_press", muscleGroup = MuscleGroup.LOWER_BODY)
        val second = plannedExercise(id = "row", muscleGroup = MuscleGroup.BACK)
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "balanced",
            name = "균형 루틴",
            cycleStartDate = cycleStart,
            days = listOf(
                WorkoutDayPlan(
                    date = cycleStart,
                    title = "1일차",
                    focus = "전신",
                    exercises = listOf(first, second)
                )
            )
        )
        val progress = RoutineProgress(
            templateId = "balanced",
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            cycleNumber = 2,
            startedAt = Instant.parse("2026-05-01T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-20T00:00:00Z")
        )
        val logs = listOf(
            completedLog(
                id = 2,
                planned = first,
                performedAt = LocalDateTime.of(2026, 5, 20, 20, 0)
            ),
            completedLog(
                id = 3,
                planned = second,
                performedAt = LocalDateTime.of(2026, 5, 21, 20, 0)
            ),
            completedLog(
                id = 4,
                planned = first.copy(id = PlannedExerciseId("routine-added|balanced|cycle2|day1|extra")),
                performedAt = LocalDateTime.of(2026, 5, 21, 21, 0)
            )
        )

        val result = calculate(
            plan = plan,
            logs = logs,
            progress = progress,
            zone = ZoneOffset.UTC
        )

        assertThat(result.plannedExerciseCount).isEqualTo(2)
        assertThat(result.completedExerciseCount).isEqualTo(2)
        assertThat(result.totalSets).isEqualTo(9)
        assertThat(result.muscleBalance[MuscleGroup.LOWER_BODY]).isEqualTo(2)
        assertThat(result.muscleBalance[MuscleGroup.BACK]).isEqualTo(1)
    }

    @Test
    fun calculate_usesProvidedCurrentCycleLogsWhenCycleStartIsMissing() {
        val first = plannedExercise(id = "leg_press", muscleGroup = MuscleGroup.LOWER_BODY)
        val second = plannedExercise(id = "row", muscleGroup = MuscleGroup.BACK)
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "balanced",
            name = "균형 루틴",
            cycleStartDate = LocalDate.of(2026, 5, 20),
            days = listOf(
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 5, 20),
                    title = "1일차",
                    focus = "전신",
                    exercises = listOf(first, second)
                )
            )
        )
        val progress = RoutineProgress(
            templateId = "balanced",
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            cycleNumber = 1,
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = null
        )
        val logs = listOf(
            completedLog(
                id = 2,
                planned = second,
                performedAt = LocalDateTime.of(2026, 5, 20, 20, 0)
            )
        )

        val result = calculate(
            plan = plan,
            logs = logs,
            progress = progress,
            zone = ZoneOffset.UTC
        )

        assertThat(result.completedExerciseCount).isEqualTo(1)
        assertThat(result.totalSets).isEqualTo(3)
        assertThat(result.muscleBalance[MuscleGroup.LOWER_BODY]).isNull()
        assertThat(result.muscleBalance[MuscleGroup.BACK]).isEqualTo(1)
    }

    @Test
    fun calculate_includesCurrentCycleRoutineDayLogsWhenCycleStartWasOverwrittenLater() {
        val first = plannedExercise(id = "deadlift", muscleGroup = MuscleGroup.BACK)
        val second = plannedExercise(id = "lat_pulldown", muscleGroup = MuscleGroup.BACK)
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "custom-template",
            name = "내 마음대로",
            cycleStartDate = LocalDate.of(2026, 6, 1),
            days = listOf(
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 6, 1),
                    title = "1일차",
                    focus = "당기는 날",
                    exercises = listOf(first, second)
                )
            )
        )
        val progress = RoutineProgress(
            templateId = "custom-template",
            dayIndex = 1,
            lastCompletedDayIndex = 0,
            lastCompletedAt = Instant.parse("2026-06-01T12:00:00Z"),
            cycleNumber = 1,
            lastCompletedCycleNumber = 1,
            startedAt = Instant.parse("2026-06-03T04:02:20.308Z"),
            cycleStartedAt = Instant.parse("2026-06-03T04:02:20.308Z")
        )
        val routineDayInstanceId = "routine-day|custom-template|cycle1|day1"
        val logs = listOf(
            completedLog(
                id = 1,
                planned = first,
                performedAt = LocalDateTime.of(2026, 6, 1, 12, 0),
                routineDayInstanceId = routineDayInstanceId
            ),
            completedLog(
                id = 2,
                planned = second,
                performedAt = LocalDateTime.of(2026, 6, 1, 12, 0),
                routineDayInstanceId = routineDayInstanceId
            )
        )

        val result = calculate(
            plan = plan,
            logs = logs,
            progress = progress,
            zone = ZoneOffset.UTC
        )

        assertThat(result.completedExerciseCount).isEqualTo(2)
        assertThat(result.totalSets).isEqualTo(6)
        assertThat(result.completionRate).isEqualTo(100)
    }

    @Test
    fun calculate_countsOnlyProvidedCurrentCycleSnapshotLogs() {
        val first = plannedExercise(id = "deadlift", muscleGroup = MuscleGroup.BACK)
        val second = plannedExercise(id = "lat_pulldown", muscleGroup = MuscleGroup.BACK)
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "custom-template",
            name = "내 마음대로",
            cycleStartDate = LocalDate.of(2026, 6, 1),
            days = listOf(
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 6, 1),
                    title = "1일차",
                    focus = "당기는 날",
                    exercises = listOf(first, second)
                )
            )
        )
        val progress = RoutineProgress(
            templateId = "custom-template",
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            cycleNumber = 2,
            startedAt = Instant.parse("2026-06-01T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-06-01T00:00:00Z")
        )

        val result = calculate(
            plan = plan,
            logs = listOf(
                completedLog(
                    id = 1,
                    planned = first,
                    performedAt = LocalDateTime.of(2026, 6, 1, 12, 0),
                    routineDayInstanceId = "routine-day|custom-template|cycle2|day1"
                ),
                completedLog(
                    id = 2,
                    planned = second,
                    performedAt = LocalDateTime.of(2026, 6, 1, 13, 0)
                )
            ),
            progress = progress,
            zone = ZoneOffset.UTC
        )

        assertThat(result.completedExerciseCount).isEqualTo(2)
        assertThat(result.totalSets).isEqualTo(6)
    }

    @Test
    fun calculate_usesPlanStartWhenProgressStartIsMissingAndIgnoresIncompleteLogs() {
        val plank = plannedExercise(id = "plank", muscleGroup = MuscleGroup.CORE)
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "bodyweight",
            name = "맨몸",
            cycleStartDate = cycleStart,
            days = listOf(
                WorkoutDayPlan(
                    date = cycleStart,
                    title = "코어",
                    focus = "코어",
                    exercises = listOf(plank)
                )
            )
        )
        val completed = completedLog(
            id = 1,
            planned = plank,
            performedAt = LocalDateTime.of(2026, 5, 18, 20, 0)
        ).copy(
            setEntries = emptyList(),
            durationMinutes = 12
        )
        val incomplete = completedLog(
            id = 2,
            planned = plank,
            performedAt = LocalDateTime.of(2026, 5, 18, 21, 0)
        ).copy(completed = false)

        val result = calculate(
            plan = plan,
            logs = listOf(completed, incomplete),
            progress = progress(
                templateId = "bodyweight",
                cycleStartedAt = null,
                startedAt = null
            ),
            zone = ZoneOffset.UTC
        )

        assertThat(result.completedExerciseCount).isEqualTo(1)
        assertThat(result.totalSets).isEqualTo(3)
        assertThat(result.totalMinutes).isEqualTo(12)
    }

    @Test
    fun calculate_countsDurationSetEntriesAndFallsBackToPrimaryMuscleWhenSecondaryGroupsAreEmpty() {
        val plank = PlannedExercise(
            id = PlannedExerciseId("2026-05-18_plank"),
            exercise = exercise(
                id = "plank",
                muscleGroup = MuscleGroup.CORE,
                muscleGroups = emptyList()
            ),
            sets = 2,
            repRange = null,
            durationMinutes = 5,
            restSeconds = 60,
            note = ""
        )
        val squat = plannedExercise(id = "squat", muscleGroup = MuscleGroup.LOWER_BODY)
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "bodyweight",
            name = "맨몸",
            cycleStartDate = cycleStart,
            days = listOf(
                WorkoutDayPlan(
                    date = cycleStart,
                    title = "전신",
                    focus = "전신",
                    exercises = listOf(plank, squat)
                )
            )
        )
        val completed = completedLog(id = 1, planned = plank).copy(
            reps = null,
            weightKg = null,
            setEntries = listOf(
                WorkoutSetLog(order = 1, reps = null, weightKg = null, durationMinutes = 4),
                WorkoutSetLog(order = 2, reps = null, weightKg = null, durationMinutes = 5)
            )
        )

        val result = calculate(
            plan = plan,
            logs = listOf(completed),
            progress = progress(templateId = "bodyweight"),
            zone = ZoneOffset.UTC
        )

        assertThat(result.completionRate).isEqualTo(50)
        assertThat(result.totalMinutes).isEqualTo(9)
        assertThat(result.totalVolumeKg).isEqualTo(0.0)
        assertThat(result.muscleBalance[MuscleGroup.CORE]).isEqualTo(1)
        assertThat(result.insight).contains("꾸준히")
    }

    @Test
    fun calculate_usesPlanStartAsStreakAnchorWhenProgressDatesAreMissing() {
        val plank = plannedExercise(id = "plank", muscleGroup = MuscleGroup.CORE)
        val plan = CyclePlan(
            id = PlanId("plan"),
            templateId = "bodyweight",
            name = "맨몸",
            cycleStartDate = cycleStart,
            days = listOf(
                WorkoutDayPlan(
                    date = cycleStart,
                    title = "코어",
                    focus = "코어",
                    exercises = listOf(plank)
                )
            )
        )

        val result = calculate(
            plan = plan,
            logs = emptyList(),
            progress = progress(
                templateId = "bodyweight",
                cycleStartedAt = null,
                startedAt = null
            ),
            zone = ZoneOffset.UTC
        )

        assertThat(result.streakDays).isEqualTo(0)
        assertThat(result.insight).contains("첫 기록")
    }

    private fun progress(
        templateId: String,
        cycleNumber: Int = 1,
        cycleStartedAt: Instant? = cycleStart.atStartOfDay().toInstant(ZoneOffset.UTC),
        startedAt: Instant? = cycleStartedAt
    ): RoutineProgress = RoutineProgress(
        templateId = templateId,
        dayIndex = 0,
        lastCompletedDayIndex = null,
        lastCompletedAt = null,
        cycleNumber = cycleNumber,
        startedAt = startedAt,
        cycleStartedAt = cycleStartedAt
    )

    private fun calculate(
        plan: CyclePlan,
        logs: List<WorkoutLog>,
        progress: RoutineProgress,
        zone: ZoneId,
        bodyWeightKg: Double? = null
    ) = CurrentRoutineCycle(
        progress = progress,
        plan = plan,
        currentDayIndex = progress.dayIndex.coerceInPlan(plan),
        currentDay = plan.days.getOrNull(progress.dayIndex.coerceInPlan(plan)),
        currentRoutineDayInstanceId = null,
        currentRoutineDayDate = null,
        previousRoutineDayInstanceId = null,
        previousRoutineDayDate = null,
        currentCycleLogs = logs,
        allLogs = logs,
        currentCyclePlannedExerciseIds = plan.days
            .flatMap { day -> day.exercises.map { it.id } }
            .toSet(),
        currentDayCompletedPlannedExerciseIds = emptySet(),
        latestCompletion = null
    ).let { currentCycle ->
        if (bodyWeightKg == null) {
            calculator.calculate(currentCycle = currentCycle, zone = zone)
        } else {
            calculator.calculate(
                currentCycle = currentCycle,
                zone = zone,
                bodyWeightKg = bodyWeightKg
            )
        }
    }

    private fun Int.coerceInPlan(plan: CyclePlan): Int =
        if (plan.days.isEmpty()) {
            0
        } else {
            coerceIn(0, plan.days.lastIndex)
        }

    private fun plannedExercise(
        id: String,
        muscleGroup: MuscleGroup,
        loadType: ExerciseLoadType = ExerciseLoadType.EXTERNAL_LOAD
    ): PlannedExercise = PlannedExercise(
        id = PlannedExerciseId("2026-05-18_$id"),
        exercise = exercise(id, muscleGroup, loadType = loadType),
        sets = 3,
        repRange = 10..12,
        durationMinutes = null,
        restSeconds = 90,
        note = ""
    )

    private fun completedLog(
        id: Long,
        planned: PlannedExercise,
        performedAt: LocalDateTime = LocalDateTime.of(2026, 5, 18, 20, 0),
        routineDayInstanceId: String? = null
    ): WorkoutLog = WorkoutLog(
        id = WorkoutLogId(id),
        sessionId = UserSessionId("local-default"),
        plannedExerciseId = planned.id,
        exerciseId = planned.exercise.id,
        performedAt = performedAt,
        sets = 3,
        reps = 10,
        weightKg = 10.0,
        durationMinutes = null,
        memo = "",
        completed = true,
        routineDayInstanceId = routineDayInstanceId
    )

    private fun exercise(
        id: String,
        muscleGroup: MuscleGroup,
        muscleGroups: List<MuscleGroup> = listOf(muscleGroup),
        loadType: ExerciseLoadType = ExerciseLoadType.EXTERNAL_LOAD
    ): Exercise = Exercise(
        id = ExerciseId(id),
        name = id,
        muscleGroup = muscleGroup,
        muscleGroups = muscleGroups,
        equipment = EquipmentType.MACHINE,
        difficulty = DifficultyLevel.BEGINNER,
        imageKey = id,
        summary = "",
        instructions = emptyList(),
        safetyCues = emptyList(),
        defaultSets = 3,
        defaultRepRange = 10..12,
        defaultDurationMinutes = null,
        restSeconds = 90,
        loadType = loadType
    )
}
