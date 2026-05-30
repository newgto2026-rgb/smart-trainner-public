package com.smarttrainner.feature.routine.impl

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanId
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WorkoutDayPlan
import java.time.LocalDate
import org.junit.Test

class RoutineContinuationPolicyTest {
    @Test
    fun nextPlannedExerciseAfterSaved_returnsNextIncompleteExerciseInSameDay() {
        val first = plannedExercise("back_pull")
        val skippedCompleted = plannedExercise("back_row")
        val next = plannedExercise("lat_pulldown")
        val state = RoutineUiState(
            plan = weeklyPlan(
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 5, 18),
                    title = "Day 1",
                    focus = "Back",
                    exercises = listOf(first, skippedCompleted, next),
                    dayNumber = 1,
                    primaryFocus = RoutineFocus.BACK,
                    secondaryFocuses = emptyList(),
                    minRecoveryHours = 24
                )
            ),
            completedPlannedExerciseIds = setOf(skippedCompleted.id)
        )

        assertThat(state.nextPlannedExerciseAfterSaved(first)?.id).isEqualTo(next.id)
    }

    @Test
    fun nextPlannedExerciseAfterSaved_doesNotContinueIntoAnotherDay() {
        val current = plannedExercise("back_pull")
        val nextDayExercise = plannedExercise("leg_press")
        val state = RoutineUiState(
            plan = weeklyPlan(
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 5, 18),
                    title = "Day 1",
                    focus = "Back",
                    exercises = listOf(current),
                    dayNumber = 1,
                    primaryFocus = RoutineFocus.BACK,
                    secondaryFocuses = emptyList(),
                    minRecoveryHours = 24
                ),
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 5, 19),
                    title = "Day 2",
                    focus = "Legs",
                    exercises = listOf(nextDayExercise),
                    dayNumber = 2,
                    primaryFocus = RoutineFocus.LOWER_BODY,
                    secondaryFocuses = emptyList(),
                    minRecoveryHours = 24
                )
            )
        )

        assertThat(state.nextPlannedExerciseAfterSaved(current)).isNull()
    }

    @Test
    fun recordablePlannedExerciseFor_returnsNullWhenCustomBuilderIsVisible() {
        val planned = plannedExercise("back_pull")
        val state = RoutineUiState(
            plan = weeklyPlan(
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 5, 18),
                    title = "Day 1",
                    focus = "Back",
                    exercises = listOf(planned),
                    dayNumber = 1,
                    primaryFocus = RoutineFocus.BACK,
                    secondaryFocuses = emptyList(),
                    minRecoveryHours = 24
                )
            ),
            customRoutineBuilder = CustomRoutineBuilderState(visible = true)
        )

        assertThat(state.recordablePlannedExerciseFor(planned.exercise.id)).isNull()
    }

    @Test
    fun recordablePlannedExerciseFor_returnsMatchingPlannedExerciseWhenBuilderIsHidden() {
        val planned = plannedExercise("back_pull")
        val state = RoutineUiState(
            plan = weeklyPlan(
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 5, 18),
                    title = "Day 1",
                    focus = "Back",
                    exercises = listOf(planned),
                    dayNumber = 1,
                    primaryFocus = RoutineFocus.BACK,
                    secondaryFocuses = emptyList(),
                    minRecoveryHours = 24
                )
            )
        )

        assertThat(state.recordablePlannedExerciseFor(planned.exercise.id)?.id).isEqualTo(planned.id)
    }
}

private fun weeklyPlan(vararg days: WorkoutDayPlan) = WeeklyPlan(
    id = PlanId("plan"),
    templateId = "template",
    name = "Template",
    weekStartDate = LocalDate.of(2026, 5, 18),
    days = days.toList()
)

private fun plannedExercise(id: String) = PlannedExercise(
    id = PlannedExerciseId("planned_$id"),
    exercise = exercise(id),
    sets = 3,
    repRange = 8..12,
    durationMinutes = null,
    restSeconds = 90,
    note = ""
)

private fun exercise(id: String) = Exercise(
    id = ExerciseId(id),
    name = id,
    muscleGroup = MuscleGroup.BACK,
    equipment = EquipmentType.MACHINE,
    difficulty = DifficultyLevel.INTERMEDIATE,
    imageKey = id,
    summary = "",
    instructions = emptyList(),
    safetyCues = emptyList(),
    defaultSets = 3,
    defaultRepRange = 8..12,
    defaultDurationMinutes = null,
    restSeconds = 90
)
