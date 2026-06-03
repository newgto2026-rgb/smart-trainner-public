package com.smarttrainner.feature.analysis.domain

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.model.UserSessionId
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Test

class WeeklySummaryCalculatorTest {
    private val calculator = WeeklySummaryCalculator()
    private val weekStart = LocalDate.of(2026, 5, 18)

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
        val plan = WeeklyPlan(
            id = PlanId("plan"),
            templateId = "beginner",
            name = "초보 주 3회",
            weekStartDate = weekStart,
            days = listOf(
                WorkoutDayPlan(
                    date = weekStart,
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

        val result = calculator.calculate(weekStart, plan, logs)

        assertThat(result.plannedExerciseCount).isEqualTo(1)
        assertThat(result.completedExerciseCount).isEqualTo(1)
        assertThat(result.completionRate).isEqualTo(100)
        assertThat(result.totalSets).isEqualTo(4)
        assertThat(result.totalVolumeKg).isEqualTo(2940.0)
        assertThat(result.muscleBalance[MuscleGroup.LOWER_BODY]).isEqualTo(1)
    }

    @Test
    fun calculate_emptyLogsReturnsCoachingPrompt() {
        val result = calculator.calculate(
            weekStartDate = weekStart,
            plan = WeeklyPlan(
                id = PlanId("plan"),
                templateId = "intro",
                name = "입문",
                weekStartDate = weekStart,
                days = emptyList()
            ),
            logs = emptyList()
        )

        assertThat(result.completionRate).isEqualTo(0)
        assertThat(result.insight).contains("플랜")
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
        val plan = WeeklyPlan(
            id = PlanId("plan"),
            templateId = "custom",
            name = "전신 루틴",
            weekStartDate = weekStart,
            days = listOf(
                WorkoutDayPlan(
                    date = weekStart,
                    title = "전신",
                    focus = "전신",
                    exercises = listOf(planned)
                )
            )
        )

        val result = calculator.calculate(
            weekStartDate = weekStart,
            plan = plan,
            logs = listOf(completedLog(id = 1, planned = planned))
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
        val plan = WeeklyPlan(
            id = PlanId("plan"),
            templateId = "balanced",
            name = "균형 루틴",
            weekStartDate = weekStart,
            days = listOf(
                WorkoutDayPlan(
                    date = weekStart,
                    title = "균형",
                    focus = "전신",
                    exercises = completedExercises + extraExercises
                )
            )
        )
        val logs = completedExercises.mapIndexed { index, planned ->
            completedLog(id = index.toLong() + 1, planned = planned)
        }

        val result = calculator.calculate(weekStart, plan, logs)

        assertThat(result.completionRate).isLessThan(80)
        assertThat(result.insight).doesNotContain(MuscleGroup.FULL_BODY.displayName)
        assertThat(result.insight).contains(MuscleGroup.LOWER_BODY.displayName)
    }

    @Test
    fun calculateCycle_usesLogsSinceCycleStartAndCapsCompletionToPlanSize() {
        val first = plannedExercise(id = "leg_press", muscleGroup = MuscleGroup.LOWER_BODY)
        val second = plannedExercise(id = "row", muscleGroup = MuscleGroup.BACK)
        val plan = WeeklyPlan(
            id = PlanId("plan"),
            templateId = "balanced",
            name = "균형 루틴",
            weekStartDate = weekStart,
            days = listOf(
                WorkoutDayPlan(
                    date = weekStart,
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
                id = 1,
                planned = first,
                performedAt = LocalDateTime.of(2026, 5, 19, 20, 0)
            ),
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

        val result = calculator.calculateCycle(
            weekStartDate = weekStart,
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

    private fun plannedExercise(
        id: String,
        muscleGroup: MuscleGroup
    ): PlannedExercise = PlannedExercise(
        id = PlannedExerciseId("2026-05-18_$id"),
        exercise = exercise(id, muscleGroup),
        sets = 3,
        repRange = 10..12,
        durationMinutes = null,
        restSeconds = 90,
        note = ""
    )

    private fun completedLog(
        id: Long,
        planned: PlannedExercise,
        performedAt: LocalDateTime = LocalDateTime.of(2026, 5, 18, 20, 0)
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
        completed = true
    )

    private fun exercise(
        id: String,
        muscleGroup: MuscleGroup,
        muscleGroups: List<MuscleGroup> = listOf(muscleGroup)
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
        restSeconds = 90
    )
}
