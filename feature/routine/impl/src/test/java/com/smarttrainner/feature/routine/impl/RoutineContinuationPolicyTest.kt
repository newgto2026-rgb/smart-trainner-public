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
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.WorkoutDayPlan
import java.time.LocalDate
import org.junit.Test

class RoutineContinuationPolicyTest {
    @Test
    fun nextPlannedExerciseAfterSaved_returnsNextIncompleteExerciseInSameDay() {
        val first = plannedExercise("back_pull")
        val skippedCompleted = plannedExercise("back_row")
        val next = plannedExercise("lat_pulldown")
        val day = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 18),
            title = "Day 1",
            focus = "Back",
            exercises = listOf(first, skippedCompleted, next),
            dayNumber = 1,
            primaryFocus = RoutineFocus.BACK,
            secondaryFocuses = emptyList(),
            minRecoveryHours = 24
        )
        val state = RoutineUiState(
            plan = cyclePlan(day),
            nextRoutineDayUi = day.toNextRoutineDayUiModel(
                template = null,
                dayIndex = 0,
                cycleNumber = 1,
                routineDayDate = null,
                previousRoutineDayDate = null,
                completedIds = emptySet()
            ),
            completedPlannedExerciseIds = setOf(skippedCompleted.id)
        )

        assertThat(state.nextPlannedExerciseAfterSaved(first)?.id).isEqualTo(next.id)
    }

    @Test
    fun nextPlannedExerciseAfterSaved_doesNotContinueIntoAnotherDay() {
        val current = plannedExercise("back_pull")
        val nextDayExercise = plannedExercise("leg_press")
        val currentDay = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 18),
            title = "Day 1",
            focus = "Back",
            exercises = listOf(current),
            dayNumber = 1,
            primaryFocus = RoutineFocus.BACK,
            secondaryFocuses = emptyList(),
            minRecoveryHours = 24
        )
        val state = RoutineUiState(
            plan = cyclePlan(
                currentDay,
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
            ),
            nextRoutineDayUi = currentDay.toNextRoutineDayUiModel(
                template = null,
                dayIndex = 0,
                cycleNumber = 1,
                routineDayDate = null,
                previousRoutineDayDate = null,
                completedIds = emptySet()
            )
        )

        assertThat(state.nextPlannedExerciseAfterSaved(current)).isNull()
    }

    @Test
    fun nextPlannedExerciseAfterSaved_ignoresExerciseOutsideCurrentRoutineDay() {
        val staleExercise = plannedExercise("back_pull")
        val currentExercise = plannedExercise("leg_press")
        val currentDay = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 19),
            title = "Day 2",
            focus = "Legs",
            exercises = listOf(currentExercise),
            dayNumber = 2,
            primaryFocus = RoutineFocus.LOWER_BODY,
            secondaryFocuses = emptyList(),
            minRecoveryHours = 24
        )
        val state = RoutineUiState(
            plan = cyclePlan(
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 5, 18),
                    title = "Day 1",
                    focus = "Back",
                    exercises = listOf(staleExercise),
                    dayNumber = 1,
                    primaryFocus = RoutineFocus.BACK,
                    secondaryFocuses = emptyList(),
                    minRecoveryHours = 24
                ),
                currentDay
            ),
            nextRoutineDayUi = currentDay.toNextRoutineDayUiModel(
                template = null,
                dayIndex = 1,
                cycleNumber = 1,
                routineDayDate = null,
                previousRoutineDayDate = null,
                completedIds = emptySet()
            )
        )

        assertThat(state.nextPlannedExerciseAfterSaved(staleExercise)).isNull()
    }

    @Test
    fun recordablePlannedExerciseFor_returnsNullWhenCustomBuilderIsVisible() {
        val planned = plannedExercise("back_pull")
        val state = RoutineUiState(
            plan = cyclePlan(
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
    fun recordablePlannedExerciseFor_returnsMatchingCurrentDayExerciseWhenBuilderIsHidden() {
        val planned = plannedExercise("back_pull")
        val day = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 18),
            title = "Day 1",
            focus = "Back",
            exercises = listOf(planned),
            dayNumber = 1,
            primaryFocus = RoutineFocus.BACK,
            secondaryFocuses = emptyList(),
            minRecoveryHours = 24
        )
        val state = RoutineUiState(
            plan = cyclePlan(day),
            nextRoutineDayUi = day.toNextRoutineDayUiModel(
                template = null,
                dayIndex = 0,
                cycleNumber = 1,
                routineDayDate = null,
                previousRoutineDayDate = null,
                completedIds = emptySet()
            )
        )

        assertThat(state.recordablePlannedExerciseFor(planned.exercise.id)?.id).isEqualTo(planned.id)
    }

    @Test
    fun recordablePlannedExerciseFor_returnsNullForCompletedCurrentDayExercise() {
        val planned = plannedExercise("back_pull")
        val day = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 18),
            title = "Day 1",
            focus = "Back",
            exercises = listOf(planned),
            dayNumber = 1,
            primaryFocus = RoutineFocus.BACK,
            secondaryFocuses = emptyList(),
            minRecoveryHours = 24
        )
        val state = RoutineUiState(
            plan = cyclePlan(day),
            nextRoutineDayUi = day.toNextRoutineDayUiModel(
                template = null,
                dayIndex = 0,
                cycleNumber = 1,
                routineDayDate = null,
                previousRoutineDayDate = null,
                completedIds = setOf(planned.id)
            ),
            completedPlannedExerciseIds = setOf(planned.id)
        )

        assertThat(state.recordablePlannedExerciseFor(planned.exercise.id)).isNull()
    }

    @Test
    fun withSessionProgress_marksRecordedExerciseCompleteBeforeRepositoryRefresh() {
        val first = plannedExercise("back_pull")
        val second = plannedExercise("back_row")
        val day = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 18),
            title = "Day 1",
            focus = "Back",
            exercises = listOf(first, second),
            dayNumber = 1,
            primaryFocus = RoutineFocus.BACK,
            secondaryFocuses = emptyList(),
            minRecoveryHours = 24
        )
        val state = RoutineUiState(
            plan = cyclePlan(day),
            nextRoutineDayUi = day.toNextRoutineDayUiModel(
                template = null,
                dayIndex = 0,
                cycleNumber = 1,
                routineDayDate = null,
                previousRoutineDayDate = null,
                completedIds = emptySet()
            )
        ).withSessionProgress(
            recordedPlannedExerciseIds = setOf(first.id),
            skippedPlannedExerciseIds = emptySet()
        )

        assertThat(state.completedPlannedExerciseIds).containsExactly(first.id)
        assertThat(state.nextRoutineDayUi?.completedExerciseCount).isEqualTo(1)
        assertThat(state.nextRoutineDayUi?.startExercise?.id).isEqualTo(second.id)
        assertThat(state.isPlanExerciseCompleted(dayIndex = 0, plannedExercise = first)).isTrue()
    }

    @Test
    fun nextPlannedExerciseAfterSaved_skipsSessionSkippedExercise() {
        val first = plannedExercise("back_pull")
        val skipped = plannedExercise("back_row")
        val third = plannedExercise("lat_pulldown")
        val day = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 18),
            title = "Day 1",
            focus = "Back",
            exercises = listOf(first, skipped, third),
            dayNumber = 1,
            primaryFocus = RoutineFocus.BACK,
            secondaryFocuses = emptyList(),
            minRecoveryHours = 24
        )
        val state = RoutineUiState(
            plan = cyclePlan(day),
            nextRoutineDayUi = day.toNextRoutineDayUiModel(
                template = null,
                dayIndex = 0,
                cycleNumber = 1,
                routineDayDate = null,
                previousRoutineDayDate = null,
                completedIds = emptySet()
            ),
            skippedPlannedExerciseIds = setOf(skipped.id)
        )

        assertThat(state.nextPlannedExerciseAfterSaved(first)?.id).isEqualTo(third.id)
        assertThat(
            state.isPlanExerciseSkipped(
                dayIndex = 0,
                plannedExercise = skipped,
                hasRecordedLog = false
            )
        ).isTrue()
    }

    @Test
    fun isPlanExerciseCompleted_returnsTrueForCompletedPreviousRoutineDay() {
        val completed = plannedExercise("back_pull")
        val current = plannedExercise("chest_press")
        val state = RoutineUiState(
            plan = cyclePlan(
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 5, 18),
                    title = "Day 1",
                    focus = "Back",
                    exercises = listOf(completed),
                    dayNumber = 1,
                    primaryFocus = RoutineFocus.BACK,
                    secondaryFocuses = emptyList(),
                    minRecoveryHours = 24
                ),
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 5, 19),
                    title = "Day 2",
                    focus = "Chest",
                    exercises = listOf(current),
                    dayNumber = 2,
                    primaryFocus = RoutineFocus.CHEST,
                    secondaryFocuses = emptyList(),
                    minRecoveryHours = 24
                )
            ),
            activeRoutineProgress = RoutineProgress(
                templateId = "template",
                dayIndex = 1,
                lastCompletedDayIndex = 0,
                lastCompletedAt = null,
                cycleNumber = 1,
                lastCompletedCycleNumber = 1
            )
        )

        assertThat(state.isPlanExerciseCompleted(dayIndex = 0, plannedExercise = completed)).isTrue()
        assertThat(state.isPlanExerciseCompleted(dayIndex = 1, plannedExercise = current)).isFalse()
    }

    @Test
    fun isPlanExerciseSkipped_returnsTrueForCompletedRoutineDayWithoutRecordedLog() {
        val skipped = plannedExercise("back_pull")
        val state = RoutineUiState(
            plan = cyclePlan(
                WorkoutDayPlan(
                    date = LocalDate.of(2026, 5, 18),
                    title = "Day 1",
                    focus = "Back",
                    exercises = listOf(skipped),
                    dayNumber = 1,
                    primaryFocus = RoutineFocus.BACK,
                    secondaryFocuses = emptyList(),
                    minRecoveryHours = 24
                )
            ),
            activeRoutineProgress = RoutineProgress(
                templateId = "template",
                dayIndex = 1,
                lastCompletedDayIndex = 0,
                lastCompletedAt = null,
                cycleNumber = 1,
                lastCompletedCycleNumber = 1
            )
        )

        assertThat(
            state.isPlanExerciseSkipped(
                dayIndex = 0,
                plannedExercise = skipped,
                hasRecordedLog = false
            )
        ).isTrue()
    }
}

private fun cyclePlan(vararg days: WorkoutDayPlan) = CyclePlan(
    id = PlanId("plan"),
    templateId = "template",
    name = "Template",
    cycleStartDate = LocalDate.of(2026, 5, 18),
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
