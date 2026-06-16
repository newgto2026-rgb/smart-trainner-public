package com.smarttrainner.feature.routine.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.ObserveCurrentRoutineCycleUseCase
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObserveTrainingExperienceUseCase
import com.smarttrainner.core.domain.RecommendExercisePrescriptionUseCase
import com.smarttrainner.core.domain.ResolveCurrentRoutineCycleUseCase
import com.smarttrainner.core.domain.RoutineProgressRepository
import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.domain.CyclePlanRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanId
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TemplateExercise
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.CycleSummary
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.model.routineDayInstanceId
import com.smarttrainner.core.model.routineDayInstancePrefix
import com.smarttrainner.feature.routine.domain.AdvanceRoutineDayUseCase
import com.smarttrainner.feature.routine.domain.CompleteRoutineDayUseCase
import com.smarttrainner.feature.routine.domain.CancelLatestRoutineDayCompletionUseCase
import com.smarttrainner.feature.routine.domain.ObservePlanTemplatesUseCase
import com.smarttrainner.feature.routine.domain.RecommendRoutineUseCase
import com.smarttrainner.feature.routine.domain.RoutineCompletionSnapshot
import com.smarttrainner.feature.routine.domain.RoutinePlanCatalogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressCommandRepository
import com.smarttrainner.feature.routine.domain.SaveCustomRoutineUseCase
import com.smarttrainner.feature.routine.domain.SetRoutineDayDateUseCase
import com.smarttrainner.feature.routine.domain.SwitchRoutineTemplateUseCase
import com.smarttrainner.feature.routine.domain.ValidateCustomRoutineUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixedInstant = Instant.parse("2026-05-24T12:00:00Z")
    private val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    private val repository = FakeTrainingRepository()
    private val sessionRepository = FakeSessionRepository()

    @Before
    fun setUp() {
        repository.reset()
        sessionRepository.reset()
    }

    @Test
    fun uiState_combinesProgressAndPlanIntoNextRoutineDay() = runTest {
        repository.progress.value = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 1,
            lastCompletedDayIndex = null,
            lastCompletedAt = null
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()

            assertThat(state.nextRoutineDayUi?.dayNumber).isEqualTo(2)
            assertThat(state.nextRoutineDayUi?.primaryFocus).isEqualTo(RoutineFocus.CHEST)
            assertThat(state.nextRoutineDayUi?.startExercise?.exercise?.id?.value).isEqualTo("chest_press")
            assertThat(state.today).isEqualTo(LocalDate.of(2026, 5, 24))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_usesActiveRoutineAsCurrentPlanWhenCatalogSelectionDiffers() = runTest {
        val customTemplate = planTemplate(
            id = "custom-selected-only",
            name = "Selected custom",
            source = RoutineSource.CUSTOM,
            cycleLength = 1,
            days = listOf(templateDay(0, "Custom Chest", RoutineFocus.CHEST, "chest_press"))
        )
        repository.setTemplates(repository.templatesForTest() + customTemplate)
        repository.selectPlanTemplate(customTemplate.id)
        repository.progress.value = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 2,
            lastCompletedDayIndex = null,
            lastCompletedAt = null
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            var state = awaitItem()
            while (state.nextRoutineDayUi?.dayNumber != 3) {
                state = awaitItem()
            }

            assertThat(repository.requestedPlanTemplateIds).contains("intermediate-body-part-4day")
            assertThat(state.selectedTemplateId).isEqualTo("intermediate-body-part-4day")
            assertThat(state.plan?.templateId).isEqualTo("intermediate-body-part-4day")
            assertThat(state.nextRoutineDayUi?.routineTemplate?.id)
                .isEqualTo("intermediate-body-part-4day")
            assertThat(state.nextRoutineDayUi?.startExercise?.exercise?.id?.value).isEqualTo("leg_press")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun requestStartWorkout_requiresRoutineDayDateBeforeStarting() = runTest {
        val viewModel = viewModel()
        var startedExercise: PlannedExercise? = null

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.nextRoutineDayUi == null) {
                state = awaitItem()
            }

            viewModel.requestStartWorkout { startedExercise = it }
            advanceUntilIdle()

            assertThat(startedExercise).isNull()
            assertThat(viewModel.uiState.value.routineDayDatePicker?.reason)
                .isEqualTo(RoutineDayDatePickerReason.START_WORKOUT)

            viewModel.selectRoutineDayDate(
                date = LocalDate.of(2026, 5, 24),
                onWorkoutStarted = { startedExercise = it },
                onRecordSelected = {}
            )
            advanceUntilIdle()

            assertThat(startedExercise?.routineDayDate).isEqualTo(LocalDate.of(2026, 5, 24))
            assertThat(startedExercise?.routineDayInstanceId).isNotNull()
            assertThat(startedExercise?.id?.value).contains("2026-05-24")
            assertThat(viewModel.uiState.value.routineDayDatePicker).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun requestRecordSelected_appliesRoutineDayDateToAdditionalExerciseAfterDateSelection() = runTest {
        val viewModel = viewModel()
        var selectedExercise: PlannedExercise? = null

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.nextRoutineDayUi?.previewExercises.isNullOrEmpty()) {
                state = awaitItem()
            }
            val routineDay = state.nextRoutineDayUi ?: error("Expected routine day")
            val baseExercise = routineDay.previewExercises.first()
            val additionalExercise = baseExercise.copy(
                id = PlannedExerciseId("additional-exercise"),
                routineDayDate = null
            )

            viewModel.requestRecordSelected(additionalExercise) { selectedExercise = it }
            advanceUntilIdle()
            viewModel.selectRoutineDayDate(
                date = LocalDate.of(2026, 5, 24),
                onWorkoutStarted = {},
                onRecordSelected = { selectedExercise = it }
            )
            advanceUntilIdle()

            assertThat(selectedExercise?.id).isEqualTo(additionalExercise.id)
            assertThat(selectedExercise?.routineDayDate).isEqualTo(LocalDate.of(2026, 5, 24))
            assertThat(selectedExercise?.routineDayInstanceId).isEqualTo(routineDay.routineDayInstanceId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun requestRecordSelected_ignoresCompletedPreviousRoutineDayExercise() = runTest {
        val completedDate = LocalDate.of(2026, 5, 23)
        repository.progress.value = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 1,
            lastCompletedDayIndex = 0,
            lastCompletedAt = Instant.parse("2026-05-23T12:00:00Z"),
            cycleNumber = 1,
            lastCompletedCycleNumber = 1,
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-23T00:00:00Z")
        )
        repository.assignRoutineDayDate(
            dayIndex = 0,
            cycleNumber = 1,
            date = completedDate
        )
        val completedDayExercise = repository.plannedExercise(dayIndex = 0, exerciseIndex = 0)
        val viewModel = viewModel()
        var selectedExercise: PlannedExercise? = null

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.nextRoutineDayUi?.dayNumber != 2) {
                state = awaitItem()
            }

            viewModel.requestRecordSelected(completedDayExercise) { selectedExercise = it }
            advanceUntilIdle()

            assertThat(selectedExercise).isNull()
            assertThat(viewModel.uiState.value.routineDayDatePicker).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun completeCurrentRoutineDay_exposesLatestCompletionAfterFirstDay() = runTest {
        repository.assignCurrentRoutineDayDate(LocalDate.of(2026, 5, 24))
        val viewModel = viewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.nextRoutineDayUi?.dayNumber != 1) {
                state = awaitItem()
            }

            viewModel.completeCurrentRoutineDay()
            advanceUntilIdle()

            var completed = awaitItem()
            while (completed.latestRoutineDayCompletion == null) {
                completed = awaitItem()
            }
            assertThat(completed.activeRoutineProgress?.dayIndex).isEqualTo(1)
            assertThat(completed.latestRoutineDayCompletion?.dayNumber).isEqualTo(1)
            assertThat(completed.latestRoutineDayCompletion?.cycleNumber).isEqualTo(1)
            assertThat(completed.nextRoutineDayUi?.dayNumber).isEqualTo(2)
            assertThat(completed.isPlanExerciseCompleted(0, repository.plannedExercise(0))).isTrue()
            assertThat(completed.isPlanExerciseCompleted(1, repository.plannedExercise(1))).isFalse()
            assertThat(completed.recordablePlannedExerciseFor(repository.plannedExercise(0).exercise.id)).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_hidesStaleLatestCompletionWhenCurrentProgressIsNotAdvanced() = runTest {
        repository.progress.value = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 0,
            lastCompletedDayIndex = 0,
            lastCompletedAt = Instant.parse("2026-06-01T12:00:00Z"),
            cycleNumber = 1,
            lastCompletedCycleNumber = 1,
            startedAt = Instant.parse("2026-06-03T04:02:20.308Z"),
            cycleStartedAt = Instant.parse("2026-06-03T04:02:20.308Z")
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.nextRoutineDayUi == null) {
                state = awaitItem()
            }

            assertThat(state.nextRoutineDayUi?.dayNumber).isEqualTo(1)
            assertThat(state.latestRoutineDayCompletion).isNull()

            viewModel.requestCancelLatestRoutineDay()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.showCancelLatestRoutineDayDialog).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_hidesPreviousCycleCompletionAfterCycleAdvances() = runTest {
        repository.progress.value = RoutineProgress(
            templateId = "beginner-full-body-3day",
            dayIndex = 0,
            lastCompletedDayIndex = 2,
            lastCompletedAt = Instant.parse("2026-06-01T12:00:00Z"),
            cycleNumber = 2,
            lastCompletedCycleNumber = 1,
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-06-01T12:00:00Z")
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.nextRoutineDayUi == null) {
                state = awaitItem()
            }

            assertThat(state.nextRoutineDayUi?.cycleNumber).isEqualTo(2)
            assertThat(state.nextRoutineDayUi?.dayNumber).isEqualTo(1)
            assertThat(state.latestRoutineDayCompletion).isNull()

            viewModel.requestCancelLatestRoutineDay()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.showCancelLatestRoutineDayDialog).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_refreshesCyclePlanWhenProgressCycleStartChanges() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            assertThat(awaitItem().plan?.cycleStartDate).isEqualTo(LocalDate.of(2026, 5, 18))

            repository.progress.value = repository.progress.value.copy(
                cycleNumber = 2,
                cycleStartedAt = Instant.parse("2026-05-25T00:00:00Z")
            )
            runCurrent()

            var refreshed = awaitItem()
            while (refreshed.plan?.cycleStartDate != LocalDate.of(2026, 5, 25)) {
                refreshed = awaitItem()
            }
            assertThat(refreshed.plan?.cycleStartDate).isEqualTo(LocalDate.of(2026, 5, 25))
            assertThat(repository.requestedPlanCycleStartDates)
                .containsAtLeast(LocalDate.of(2026, 5, 18), LocalDate.of(2026, 5, 25))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun nextRoutineDayUi_includesEveryPlannedExercise() {
        val exercises = listOf(
            plannedExercise("exercise_1"),
            plannedExercise("exercise_2"),
            plannedExercise("exercise_3"),
            plannedExercise("exercise_4")
        )
        val day = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 24),
            title = "1일차",
            focus = "가슴 집중",
            exercises = exercises,
            dayNumber = 1,
            primaryFocus = RoutineFocus.CHEST,
            secondaryFocuses = emptyList(),
            minRecoveryHours = 24
        )

        val uiModel = day.toNextRoutineDayUiModel(
            template = null,
            dayIndex = 0,
            cycleNumber = 1,
            routineDayDate = null,
            previousRoutineDayDate = null,
            completedIds = emptySet()
        )

        assertThat(uiModel.previewExercises.map { it.id }).containsExactlyElementsIn(exercises.map { it.id }).inOrder()
    }

    @Test
    fun nextRoutineDayUi_usesEstimatedMinutesFromPlannedExercises() {
        val day = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 24),
            title = "1일차",
            focus = "가슴 집중",
            exercises = listOf(plannedExercise("exercise_1")),
            dayNumber = 1,
            primaryFocus = RoutineFocus.CHEST,
            secondaryFocuses = emptyList(),
            minRecoveryHours = 24
        )
        val template = PlanTemplate(
            id = "template",
            name = "Template",
            level = PlanLevel.INTERMEDIATE,
            cycleLength = 1,
            description = "",
            days = listOf(templateDay(0, "가슴 집중", RoutineFocus.CHEST, "exercise_1")),
            sessionMinutes = 45
        )

        val uiModel = day.toNextRoutineDayUiModel(
            template = template,
            dayIndex = 0,
            cycleNumber = 1,
            routineDayDate = null,
            previousRoutineDayDate = null,
            completedIds = emptySet()
        )

        assertThat(uiModel.sessionMinutes).isEqualTo(8)
    }

    @Test
    fun nextRoutineDayUi_ignoresEmptyTemplateDays() {
        val day = WorkoutDayPlan(
            date = LocalDate.of(2026, 5, 24),
            title = "1일차",
            focus = "가슴 집중",
            exercises = listOf(plannedExercise("exercise_1")),
            dayNumber = 1,
            primaryFocus = RoutineFocus.CHEST,
            secondaryFocuses = emptyList(),
            minRecoveryHours = 24
        )
        val emptyTemplate = PlanTemplate(
            id = "empty-template",
            name = "Empty",
            level = PlanLevel.BEGINNER,
            cycleLength = 0,
            description = "",
            days = emptyList(),
            sessionMinutes = 45,
            source = RoutineSource.SYSTEM
        )

        val uiModel = day.toNextRoutineDayUiModel(
            template = emptyTemplate,
            dayIndex = 0,
            cycleNumber = 1,
            routineDayDate = null,
            previousRoutineDayDate = null,
            completedIds = emptySet()
        )

        assertThat(uiModel.nextPrimaryFocus).isNull()
    }

    @Test
    fun uiState_filtersCompletedExerciseIdsToActiveRoutineCycle() = runTest {
        repository.progress.value = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 0,
            lastCompletedDayIndex = 3,
            lastCompletedAt = fixedInstant,
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = fixedInstant
        )
        repository.setLogs(
            listOf(
                repository.completedLog(
                    dayIndex = 0,
                    performedAt = LocalDateTime.of(2026, 5, 24, 11, 0)
                )
            )
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()

            assertThat(state.completedPlannedExerciseIds).isEmpty()
            assertThat(state.nextRoutineDayUi?.completedExerciseCount).isEqualTo(0)
            assertThat(state.plan?.cycleStartDate).isEqualTo(LocalDate.of(2026, 5, 24))
            assertThat(state.nextRoutineDayUi?.startExercise?.id)
                .isEqualTo(state.plan?.days?.firstOrNull()?.exercises?.firstOrNull()?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_doesNotRestoreCurrentScheduleDateFromWorkoutLog() = runTest {
        val routineDayInstanceId = routineDayInstanceId(
            templateId = "intermediate-body-part-4day",
            cycleNumber = 1,
            dayNumber = 1
        )
        repository.setLogs(
            listOf(
                repository.completedLog(
                    dayIndex = 0,
                    performedAt = LocalDateTime.of(2026, 5, 24, 12, 0),
                    routineDayInstanceId = routineDayInstanceId
                )
            )
        )
        repository.progress.value = repository.progress.value.copy(
            routineDayDates = emptyMap()
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.nextRoutineDayUi == null) {
                state = awaitItem()
            }

            assertThat(state.nextRoutineDayUi?.routineDayInstanceId).isEqualTo(routineDayInstanceId)
            assertThat(state.nextRoutineDayUi?.routineDayDate).isNull()
            assertThat(state.routineDayDatePicker).isNull()

            viewModel.requestStartWorkout {}
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.routineDayDatePicker?.reason)
                .isEqualTo(RoutineDayDatePickerReason.START_WORKOUT)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun requestCompleteCurrentRoutineDay_requiresConfirmationBeforeWrappingAfterLastDay() = runTest {
        repository.progress.value = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 3,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-20T00:00:00Z")
        )
        repository.assignCurrentRoutineDayDate(LocalDate.of(2026, 5, 24))
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            assertThat(awaitItem().nextRoutineDayUi?.dayNumber).isEqualTo(4)

            viewModel.requestCompleteCurrentRoutineDay(
                skippedPlannedExerciseIds = emptySet(),
                justRecordedPlannedExerciseIds = emptySet()
            )
            advanceUntilIdle()

            var confirm = awaitItem()
            while (confirm.routineCompletionConfirm == null) {
                confirm = awaitItem()
            }
            assertThat(confirm.activeRoutineProgress?.cycleNumber).isEqualTo(1)
            assertThat(confirm.activeRoutineProgress?.dayIndex).isEqualTo(3)
            assertThat(confirm.routineCompletionConfirm?.reason)
                .isEqualTo(RoutineCompletionConfirmReason.CYCLE_COMPLETE)
            assertThat(confirm.routineCompletionConfirm?.unrecordedExercises?.map { it.exercise.id.value })
                .containsExactly("shoulder_raise")

            viewModel.confirmCompleteCurrentRoutineDay()
            advanceUntilIdle()

            var advanced = awaitItem()
            while (advanced.activeRoutineProgress?.cycleNumber != 2) {
                advanced = awaitItem()
            }
            assertThat(advanced.activeRoutineProgress?.dayIndex).isEqualTo(0)
            assertThat(advanced.activeRoutineProgress?.cycleStartedAt).isEqualTo(fixedInstant)
            assertThat(advanced.activeRoutineProgress?.lastCompletedDayIndex).isNull()
            assertThat(advanced.activeRoutineProgress?.lastCompletedAt).isNull()
            assertThat(advanced.latestRoutineDayCompletion).isNull()
            assertThat(advanced.nextRoutineDayUi?.dayNumber).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun requestCompleteCurrentRoutineDay_requiresConfirmationBeforeAdvancingAfterRecordedDay() = runTest {
        repository.assignCurrentRoutineDayDate(LocalDate.of(2026, 5, 24))
        val viewModel = viewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.nextRoutineDayUi == null) {
                state = awaitItem()
            }
            val recordedIds = state.nextRoutineDayUi?.previewExercises.orEmpty().map { it.id }.toSet()

            viewModel.requestCompleteCurrentRoutineDay(
                skippedPlannedExerciseIds = emptySet(),
                justRecordedPlannedExerciseIds = recordedIds
            )
            advanceUntilIdle()

            var confirm = awaitItem()
            while (confirm.routineCompletionConfirm == null) {
                confirm = awaitItem()
            }
            assertThat(confirm.activeRoutineProgress?.dayIndex).isEqualTo(0)
            assertThat(confirm.routineCompletionConfirm?.reason).isEqualTo(RoutineCompletionConfirmReason.MANUAL)
            assertThat(confirm.routineCompletionConfirm?.unrecordedExercises).isEmpty()

            viewModel.confirmCompleteCurrentRoutineDay()
            advanceUntilIdle()

            var advanced = awaitItem()
            while (advanced.activeRoutineProgress?.dayIndex != 1) {
                advanced = awaitItem()
            }
            assertThat(advanced.latestRoutineDayCompletion?.dayNumber).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun confirmCompleteCurrentRoutineDay_ignoresDuplicateCycleCompletionConfirmations() = runTest {
        repository.progress.value = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 3,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-20T00:00:00Z")
        )
        repository.assignCurrentRoutineDayDate(LocalDate.of(2026, 5, 24))
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            assertThat(awaitItem().nextRoutineDayUi?.dayNumber).isEqualTo(4)

            viewModel.requestCompleteCurrentRoutineDay(
                skippedPlannedExerciseIds = emptySet(),
                justRecordedPlannedExerciseIds = emptySet()
            )
            advanceUntilIdle()

            var confirm = awaitItem()
            while (confirm.routineCompletionConfirm == null) {
                confirm = awaitItem()
            }

            viewModel.confirmCompleteCurrentRoutineDay()
            viewModel.confirmCompleteCurrentRoutineDay()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.activeRoutineProgress?.cycleNumber).isEqualTo(2)
            assertThat(viewModel.uiState.value.activeRoutineProgress?.dayIndex).isEqualTo(0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun requestCompleteCurrentRoutineDay_showsConfirmationForSkippedOrUnrecordedExercises() = runTest {
        val skipped = repository.plannedExercise(dayIndex = 0, exerciseIndex = 0)
        repository.assignCurrentRoutineDayDate(LocalDate.of(2026, 5, 24))
        val viewModel = viewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.nextRoutineDayUi == null) {
                state = awaitItem()
            }

            viewModel.requestCompleteCurrentRoutineDay(
                skippedPlannedExerciseIds = setOf(skipped.id),
                justRecordedPlannedExerciseIds = emptySet()
            )
            advanceUntilIdle()

            var updated = awaitItem()
            while (updated.routineCompletionConfirm == null) {
                updated = awaitItem()
            }
            val confirmation = updated.routineCompletionConfirm
            assertThat(confirmation?.unrecordedExercises?.map { it.id }).contains(skipped.id)
            assertThat(confirmation?.skippedExerciseIds).containsExactly(skipped.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectRoutineDayDate_continuesPendingCompleteDayRequest() = runTest {
        val selectedDate = LocalDate.of(2026, 5, 24)
        val viewModel = viewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.nextRoutineDayUi == null) {
                state = awaitItem()
            }

            viewModel.requestCompleteCurrentRoutineDay(
                skippedPlannedExerciseIds = emptySet(),
                justRecordedPlannedExerciseIds = emptySet()
            )
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.routineDayDatePicker?.reason)
                .isEqualTo(RoutineDayDatePickerReason.COMPLETE_DAY)

            viewModel.selectRoutineDayDate(
                date = selectedDate,
                onWorkoutStarted = {},
                onRecordSelected = {}
            )
            advanceUntilIdle()

            var updated = awaitItem()
            while (updated.routineCompletionConfirm == null) {
                updated = awaitItem()
            }
            assertThat(updated.routineDayDatePicker).isNull()
            assertThat(updated.routineCompletionConfirm?.unrecordedExercises)
                .hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun requestCompleteCurrentRoutineDay_excludesSessionRecordedExercisesFromConfirmation() = runTest {
        val recorded = repository.plannedExercise(dayIndex = 0, exerciseIndex = 0)
        val unrecorded = repository.plannedExercise(dayIndex = 0, exerciseIndex = 1)
        repository.assignCurrentRoutineDayDate(LocalDate.of(2026, 5, 24))
        val viewModel = viewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.nextRoutineDayUi == null) {
                state = awaitItem()
            }

            viewModel.requestCompleteCurrentRoutineDay(
                skippedPlannedExerciseIds = emptySet(),
                justRecordedPlannedExerciseIds = setOf(recorded.id)
            )
            advanceUntilIdle()

            var updated = awaitItem()
            while (updated.routineCompletionConfirm == null) {
                updated = awaitItem()
            }
            val confirmation = updated.routineCompletionConfirm
            assertThat(confirmation?.unrecordedExercises?.map { it.id }).containsExactly(unrecorded.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun confirmCancelLatestRoutineDay_restoresLatestDayAndDeletesDayLogs() = runTest {
        val firstDayLog = repository.completedLog(
            dayIndex = 0,
            performedAt = LocalDateTime.of(2026, 5, 18, 10, 0)
        )
        repository.setLogs(listOf(firstDayLog))
        repository.progress.value = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 1,
            lastCompletedDayIndex = 0,
            lastCompletedAt = fixedInstant,
            cycleNumber = 1,
            lastCompletedCycleNumber = 1,
            lastCompletedPreviousCycleStartedAt = Instant.parse("2026-05-18T00:00:00Z"),
            startedAt = Instant.parse("2026-05-18T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-18T00:00:00Z")
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.requestCancelLatestRoutineDay()
            assertThat(awaitItem().showCancelLatestRoutineDayDialog).isTrue()

            viewModel.confirmCancelLatestRoutineDay()
            advanceUntilIdle()

            var canceled = awaitItem()
            while (canceled.activeRoutineProgress?.dayIndex != 0) {
                canceled = awaitItem()
            }
            assertThat(canceled.activeRoutineProgress?.dayIndex).isEqualTo(0)
            assertThat(canceled.activeRoutineProgress?.lastCompletedDayIndex).isNull()
            assertThat(repository.currentLogs()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun routineRecommendationFlowOpensSettingsThenRecommendationsBeforeStartingRoutine() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)

            viewModel.showRoutineSettings()
            assertThat(awaitItem().showRoutineSettingsDialog).isTrue()

            viewModel.showRoutineRecommendations()
            val recommendations = awaitItem()
            assertThat(recommendations.showRoutineSettingsDialog).isFalse()
            assertThat(recommendations.showRoutineRecommendationsDialog).isTrue()
            assertThat(recommendations.routinePreviewTemplateId).isEqualTo("intermediate-body-part-4day")

            viewModel.startPreviewRoutine()
            advanceUntilIdle()

            val started = awaitItem()
            assertThat(started.showRoutineRecommendationsDialog).isFalse()
            assertThat(started.activeRoutineProgress?.templateId).isEqualTo("intermediate-body-part-4day")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun routineRecommendationFiltersNormalizeUnavailableTimeOptions() = runTest {
        repository.setTemplates(
            listOf(
                filterTemplate(
                    id = "beginner-body-part-5day-45",
                    cycleLength = 5,
                    sessionMinutes = 45,
                    structure = RoutineStructure.BODY_PART_SPLIT,
                    experience = TrainingExperience.BEGINNER
                ),
                filterTemplate(
                    id = "beginner-full-body-5day-45",
                    cycleLength = 5,
                    sessionMinutes = 45,
                    structure = RoutineStructure.FULL_BODY,
                    experience = TrainingExperience.BEGINNER
                ),
                filterTemplate(
                    id = "intermediate-body-part-2day-45",
                    cycleLength = 2,
                    sessionMinutes = 45,
                    structure = RoutineStructure.BODY_PART_SPLIT,
                    experience = TrainingExperience.INTERMEDIATE
                )
            )
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            var state = awaitItem()
            while (state.templates.size != 3) {
                state = awaitItem()
            }
            viewModel.updateRoutineExperience(TrainingExperience.BEGINNER)
            viewModel.updateRoutineCycleLength(5)
            viewModel.updateRoutineSessionMinutes(60)
            viewModel.updateRoutineFeeling(RoutineFeeling.FOCUSED_BODY_PART)
            advanceUntilIdle()

            state = awaitItem()
            while (state.routineRecommendationInput.experience != TrainingExperience.BEGINNER ||
                state.routineRecommendationInput.cycleLength != 5
            ) {
                state = awaitItem()
            }
            assertThat(state.routineRecommendationInput.experience).isEqualTo(TrainingExperience.BEGINNER)
            assertThat(state.routineRecommendationInput.cycleLength).isEqualTo(5)
            assertThat(state.routineRecommendationInput.sessionMinutes).isEqualTo(45)
            assertThat(state.routineRecommendationInput.feeling).isEqualTo(RoutineFeeling.FOCUSED_BODY_PART)
            assertThat(state.routineFilterAvailability.sessionMinutes).containsExactly(45)
            assertThat(state.routineFilterAvailability.sessionMinutes).doesNotContain(60)
            assertThat(state.recommendedTemplateId).isEqualTo("beginner-body-part-5day-45")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun routineRecommendationDefaultsFollowProfileExperience() = runTest {
        repository.setTemplates(
            listOf(
                filterTemplate(
                    id = "advanced-body-part-5day-60",
                    cycleLength = 5,
                    sessionMinutes = 60,
                    structure = RoutineStructure.BODY_PART_SPLIT,
                    experience = TrainingExperience.ADVANCED
                )
            )
        )
        sessionRepository.setExperience(TrainingExperience.ADVANCED)
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            var state = awaitItem()
            while (state.profileExperience != TrainingExperience.ADVANCED) {
                state = awaitItem()
            }

            assertThat(state.routineRecommendationInput.experience).isEqualTo(TrainingExperience.ADVANCED)
            assertThat(state.routineRecommendationInput.cycleLength).isEqualTo(5)
            assertThat(state.routineRecommendationInput.sessionMinutes).isEqualTo(60)
            assertThat(state.routineRecommendationInput.feeling).isEqualTo(RoutineFeeling.FOCUSED_BODY_PART)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun profileExperienceChange_keepsCurrentRoutineUntilUserChoosesRoutine() = runTest {
        val beginnerTemplate = PlanTemplate(
            id = "beginner-full-body-3day",
            name = "Beginner",
            level = PlanLevel.BEGINNER,
            cycleLength = 3,
            description = "Beginner",
            days = listOf(
                templateDay(0, "Beginner A", RoutineFocus.FULL_BODY, "back_pull"),
                templateDay(1, "Beginner B", RoutineFocus.FULL_BODY, "leg_press"),
                templateDay(2, "Beginner C", RoutineFocus.FULL_BODY, "shoulder_raise")
            ),
            structure = RoutineStructure.FULL_BODY,
            recommendedExperience = TrainingExperience.BEGINNER,
            sessionMinutes = 45,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        )
        val intermediateTemplate = PlanTemplate(
            id = "intermediate-body-part-4day-60",
            name = "Intermediate",
            level = PlanLevel.INTERMEDIATE,
            cycleLength = 4,
            description = "Intermediate",
            days = listOf(
                templateDay(0, "Intermediate Pull", RoutineFocus.BACK, "back_row"),
                templateDay(1, "Intermediate Push", RoutineFocus.CHEST, "chest_press"),
                templateDay(2, "Intermediate Legs", RoutineFocus.LOWER_BODY, "leg_press"),
                templateDay(3, "Intermediate Shoulders", RoutineFocus.SHOULDERS, "shoulder_raise")
            ),
            structure = RoutineStructure.BODY_PART_SPLIT,
            recommendedExperience = TrainingExperience.INTERMEDIATE,
            sessionMinutes = 60,
            focusSummary = listOf(
                RoutineFocus.BACK,
                RoutineFocus.CHEST,
                RoutineFocus.LOWER_BODY,
                RoutineFocus.SHOULDERS
            )
        )
        repository.setTemplates(listOf(beginnerTemplate, intermediateTemplate))
        repository.startRoutine(beginnerTemplate.id)
        repository.progress.value = RoutineProgress(
            templateId = beginnerTemplate.id,
            dayIndex = 1,
            lastCompletedDayIndex = 0,
            lastCompletedAt = fixedInstant,
            cycleNumber = 2,
            lastCompletedCycleNumber = 2,
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-24T00:00:00Z")
        )
        val originalCycleStartedAt = repository.progress.value.cycleStartedAt
        val viewModel = viewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.activeRoutineProgress?.templateId != beginnerTemplate.id) {
                state = awaitItem()
            }

            sessionRepository.setExperience(TrainingExperience.INTERMEDIATE)
            advanceUntilIdle()

            var updated = awaitItem()
            while (
                updated.profileExperience != TrainingExperience.INTERMEDIATE ||
                updated.routineRecommendationInput.experience != TrainingExperience.INTERMEDIATE
            ) {
                updated = awaitItem()
            }
            val progress = updated.activeRoutineProgress
            assertThat(progress?.templateId).isEqualTo(beginnerTemplate.id)
            assertThat(progress?.dayIndex).isEqualTo(1)
            assertThat(progress?.cycleNumber).isEqualTo(2)
            assertThat(progress?.cycleStartedAt).isEqualTo(originalCycleStartedAt)
            assertThat(updated.nextRoutineDayUi?.dayNumber).isEqualTo(2)
            assertThat(updated.nextRoutineDayUi?.startExercise?.exercise?.id?.value).isEqualTo("leg_press")
            assertThat(updated.routineRecommendationInput.experience).isEqualTo(TrainingExperience.INTERMEDIATE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun routineLibraryClosesWhenStartingTemplateOrOpeningRecommendationSettings() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)

            viewModel.showRoutineLibrary()
            var state = awaitItem()
            while (!state.showRoutineLibraryDialog) {
                state = awaitItem()
            }
            assertThat(state.showRoutineLibraryDialog).isTrue()

            viewModel.showRoutineSettings()
            var settings = awaitItem()
            while (!settings.showRoutineSettingsDialog) {
                settings = awaitItem()
            }
            assertThat(settings.showRoutineLibraryDialog).isFalse()
            assertThat(settings.showRoutineSettingsDialog).isTrue()

            viewModel.dismissRoutineSettings()
            viewModel.showRoutineLibrary()
            state = awaitItem()
            while (!state.showRoutineLibraryDialog) {
                state = awaitItem()
            }
            assertThat(state.showRoutineLibraryDialog).isTrue()

            viewModel.selectTemplate("intermediate-body-part-4day")
            advanceUntilIdle()

            var selected = awaitItem()
            while (selected.showRoutineLibraryDialog) {
                selected = awaitItem()
            }
            assertThat(selected.showRoutineLibraryDialog).isFalse()
            assertThat(selected.activeRoutineProgress?.templateId).isEqualTo("intermediate-body-part-4day")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectTemplate_resetsDayAndLatestCompletionButKeepsCurrentCycle() = runTest {
        val beginnerTemplate = PlanTemplate(
            id = "beginner-full-body-3day",
            name = "Beginner",
            level = PlanLevel.BEGINNER,
            cycleLength = 3,
            description = "Beginner",
            days = listOf(
                templateDay(0, "Beginner A", RoutineFocus.FULL_BODY, "back_pull"),
                templateDay(1, "Beginner B", RoutineFocus.FULL_BODY, "leg_press"),
                templateDay(2, "Beginner C", RoutineFocus.FULL_BODY, "shoulder_raise")
            ),
            structure = RoutineStructure.FULL_BODY,
            recommendedExperience = TrainingExperience.BEGINNER,
            sessionMinutes = 45,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        )
        val intermediateTemplate = PlanTemplate(
            id = "intermediate-body-part-4day-60",
            name = "Intermediate",
            level = PlanLevel.INTERMEDIATE,
            cycleLength = 4,
            description = "Intermediate",
            days = listOf(
                templateDay(0, "Intermediate Pull", RoutineFocus.BACK, "back_row"),
                templateDay(1, "Intermediate Push", RoutineFocus.CHEST, "chest_press"),
                templateDay(2, "Intermediate Legs", RoutineFocus.LOWER_BODY, "leg_press"),
                templateDay(3, "Intermediate Shoulders", RoutineFocus.SHOULDERS, "shoulder_raise")
            ),
            structure = RoutineStructure.BODY_PART_SPLIT,
            recommendedExperience = TrainingExperience.INTERMEDIATE,
            sessionMinutes = 60,
            focusSummary = listOf(
                RoutineFocus.BACK,
                RoutineFocus.CHEST,
                RoutineFocus.LOWER_BODY,
                RoutineFocus.SHOULDERS
            )
        )
        val startedAt = Instant.parse("2026-05-20T00:00:00Z")
        val cycleStartedAt = Instant.parse("2026-05-27T00:00:00Z")
        repository.setTemplates(listOf(beginnerTemplate, intermediateTemplate))
        repository.startRoutine(beginnerTemplate.id)
        repository.progress.value = RoutineProgress(
            templateId = beginnerTemplate.id,
            dayIndex = 2,
            lastCompletedDayIndex = 1,
            lastCompletedAt = fixedInstant,
            cycleNumber = 4,
            lastCompletedCycleNumber = 4,
            lastCompletedPreviousCycleStartedAt = Instant.parse("2026-05-20T00:00:00Z"),
            startedAt = startedAt,
            cycleStartedAt = cycleStartedAt
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)

            viewModel.showRoutineLibrary()
            viewModel.selectTemplate(intermediateTemplate.id)
            advanceUntilIdle()

            var confirm = awaitItem()
            while (confirm.routineSwitchConfirmTemplateId != intermediateTemplate.id) {
                confirm = awaitItem()
            }
            assertThat(confirm.showRoutineLibraryDialog).isTrue()
            assertThat(confirm.activeRoutineProgress?.templateId).isEqualTo(beginnerTemplate.id)

            viewModel.confirmRoutineSwitch()
            advanceUntilIdle()

            var selected = awaitItem()
            while (selected.activeRoutineProgress?.templateId != intermediateTemplate.id) {
                selected = awaitItem()
            }
            val progress = selected.activeRoutineProgress
            assertThat(progress?.dayIndex).isEqualTo(0)
            assertThat(progress?.cycleNumber).isEqualTo(4)
            assertThat(progress?.startedAt).isEqualTo(repository.switchStartedAt)
            assertThat(progress?.cycleStartedAt).isEqualTo(repository.switchStartedAt)
            assertThat(progress?.lastCompletedDayIndex).isNull()
            assertThat(progress?.lastCompletedAt).isNull()
            assertThat(progress?.routineDayDates).isEmpty()
            assertThat(selected.latestRoutineDayCompletion).isNull()
            assertThat(selected.nextRoutineDayUi?.dayNumber).isEqualTo(1)
            assertThat(selected.nextRoutineDayUi?.startExercise?.exercise?.id?.value).isEqualTo("back_row")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun showCreateCustomRoutine_startsWithEmptyDay() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            advanceUntilIdle()

            val builder = viewModel.uiState.value.customRoutineBuilder
            assertThat(builder.visible).isTrue()
            assertThat(builder.days).hasSize(1)
            assertThat(builder.days.first().title).isEmpty()
            assertThat(builder.days.first().focus).isNull()
            assertThat(builder.days.first().exercises).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateCustomRoutineDayFocus_setsAndClearsSelectedDayFocus() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.toggleCustomRoutineExerciseGroup(MuscleGroup.CHEST)
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.customRoutineBuilder.expandedExerciseGroups)
                .containsExactly(MuscleGroup.CHEST)

            viewModel.updateCustomRoutineDayFocus(RoutineFocus.CHEST)
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.customRoutineBuilder.days.first().focus)
                .isEqualTo(RoutineFocus.CHEST)
            assertThat(viewModel.uiState.value.customRoutineBuilder.expandedExerciseGroups).isEmpty()

            viewModel.updateCustomRoutineDayFocus(null)
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.customRoutineBuilder.days.first().focus).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun allowedCustomRoutineMuscleGroups_mapsFocusedSelections() {
        assertThat(allowedCustomRoutineMuscleGroups(null))
            .containsExactlyElementsIn(MuscleGroup.entries)
        assertThat(allowedCustomRoutineMuscleGroups(RoutineFocus.BACK))
            .containsExactly(MuscleGroup.BACK)
        assertThat(allowedCustomRoutineMuscleGroups(RoutineFocus.UPPER_BODY))
            .containsExactly(
                MuscleGroup.BACK,
                MuscleGroup.CHEST,
                MuscleGroup.SHOULDERS,
                MuscleGroup.ARMS,
                MuscleGroup.BICEPS,
                MuscleGroup.TRICEPS,
                MuscleGroup.FOREARMS
            )
        assertThat(allowedCustomRoutineMuscleGroups(RoutineFocus.PUSH))
            .containsExactly(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)
        assertThat(allowedCustomRoutineMuscleGroups(RoutineFocus.PULL))
            .containsExactly(MuscleGroup.BACK, MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
        assertThat(allowedCustomRoutineMuscleGroups(RoutineFocus.CARDIO_CONDITIONING))
            .containsExactly(MuscleGroup.CARDIO)
    }

    @Test
    fun addExerciseToCustomRoutine_ignoresExercisesOutsideSelectedFocus() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.updateCustomRoutineDayFocus(RoutineFocus.BACK)
            viewModel.addExerciseToCustomRoutine(ExerciseId("chest_press"))
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.customRoutineBuilder.days.first().exercises).isEmpty()

            viewModel.addExerciseToCustomRoutine(ExerciseId("back_pull"))
            advanceUntilIdle()
            assertThat(
                viewModel.uiState.value.customRoutineBuilder.days.first().exercises
                    .map { it.exercise.id.value }
            ).containsExactly("back_pull")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addExerciseToCustomRoutine_allowsExercisesWhenSecondaryMuscleGroupMatchesFocus() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.updateCustomRoutineDayFocus(RoutineFocus.BACK)
            viewModel.addExerciseToCustomRoutine(ExerciseId("squat_pattern"))
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.customRoutineBuilder.days.first().exercises
                    .map { it.exercise.id.value }
            ).containsExactly("squat_pattern")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateCustomRoutineDayFocus_keepsExercisesWhenSecondaryMuscleGroupMatchesFocus() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.addExerciseToCustomRoutine(ExerciseId("squat_pattern"))
            advanceUntilIdle()

            viewModel.updateCustomRoutineDayFocus(RoutineFocus.BACK)
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.customRoutineBuilder.days.first().exercises
                    .map { it.exercise.id.value }
            ).containsExactly("squat_pattern")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addExerciseToCustomRoutine_usesBaselineRepRecommendationAcrossProfileLevels() = runTest {
        sessionRepository.setExperience(TrainingExperience.ADVANCED)
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.addExerciseToCustomRoutine(ExerciseId("chest_press"))
            advanceUntilIdle()

            val exercise = viewModel.uiState.value.customRoutineBuilder.days.first().exercises.single()
            assertThat(exercise.sets).isEqualTo(3)
            assertThat(exercise.repRangeStart).isEqualTo(15)
            assertThat(exercise.repRangeEnd).isEqualTo(15)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateCustomRoutineDayFocus_removesOnlyExercisesOutsideNewFocus() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.addExerciseToCustomRoutine(ExerciseId("back_pull"))
            viewModel.addExerciseToCustomRoutine(ExerciseId("chest_press"))
            viewModel.addExerciseToCustomRoutine(ExerciseId("leg_press"))
            advanceUntilIdle()
            assertThat(
                viewModel.uiState.value.customRoutineBuilder.days.first().exercises
                    .map { it.exercise.id.value }
            ).containsExactly("back_pull", "chest_press", "leg_press").inOrder()

            viewModel.updateCustomRoutineDayFocus(RoutineFocus.UPPER_BODY)
            advanceUntilIdle()
            assertThat(
                viewModel.uiState.value.customRoutineBuilder.days.first().exercises
                    .map { it.exercise.id.value }
            ).containsExactly("back_pull", "chest_press").inOrder()

            viewModel.updateCustomRoutineDayFocus(RoutineFocus.CHEST)
            advanceUntilIdle()
            val focusedDay = viewModel.uiState.value.customRoutineBuilder.days.first()
            assertThat(focusedDay.focus).isEqualTo(RoutineFocus.CHEST)
            assertThat(focusedDay.exercises.map { it.exercise.id.value })
                .containsExactly("chest_press")

            viewModel.updateCustomRoutineDayFocus(null)
            advanceUntilIdle()
            val clearedDay = viewModel.uiState.value.customRoutineBuilder.days.first()
            assertThat(clearedDay.focus).isNull()
            assertThat(clearedDay.exercises.map { it.exercise.id.value })
                .containsExactly("chest_press")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun editCustomRoutine_loadsSavedRoutineIntoBuilderAndUpdatesSameTemplate() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.updateCustomRoutineName("My split")
            viewModel.updateCustomRoutineDayFocus(RoutineFocus.CHEST)
            viewModel.addExerciseToCustomRoutine(ExerciseId("chest_press"))
            viewModel.saveCustomRoutine(startAfterSave = false)
            advanceUntilIdle()

            viewModel.editCustomRoutine("custom-test")
            advanceUntilIdle()
            val editing = viewModel.uiState.value.customRoutineBuilder
            assertThat(editing.visible).isTrue()
            assertThat(editing.editingRoutineId).isEqualTo("custom-test")
            assertThat(editing.name).isEqualTo("My split")
            assertThat(editing.days.first().focus).isEqualTo(RoutineFocus.CHEST)

            viewModel.updateCustomRoutineName("My edited split")
            viewModel.saveCustomRoutine(startAfterSave = false)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.customRoutineBuilder.visible).isFalse()
            assertThat(viewModel.uiState.value.customTemplates.single { it.id == "custom-test" }.name)
                .isEqualTo("My edited split")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveCustomRoutine_keepsCurrentRoutineWhenStartAfterSaveIsFalse() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.updateCustomRoutineName("My split")
            viewModel.addExerciseToCustomRoutine(ExerciseId("back_pull"))
            viewModel.saveCustomRoutine(startAfterSave = false)
            advanceUntilIdle()

            val saved = viewModel.uiState.value
            assertThat(saved.customRoutineBuilder.visible).isFalse()
            assertThat(saved.activeRoutineProgress?.templateId).isEqualTo("intermediate-body-part-4day")
            assertThat(saved.customTemplates.map { it.id }).contains("custom-test")
            val customTemplate = saved.customTemplates.single { it.id == "custom-test" }
            assertThat(customTemplate.focusSummary).isEmpty()
            assertThat(customTemplate.days.first().title).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveCustomRoutineAndStart_changesActiveRoutineAndClosesBuilder() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.updateCustomRoutineName("My split")
            viewModel.addExerciseToCustomRoutine(ExerciseId("back_pull"))
            viewModel.saveCustomRoutine(startAfterSave = true)
            advanceUntilIdle()

            val started = viewModel.uiState.value
            assertThat(started.customRoutineBuilder.visible).isFalse()
            assertThat(started.activeRoutineProgress?.templateId).isEqualTo("custom-test")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveCustomRoutineAndStart_keepsBuilderOpenWhenRoutineSwitchFails() = runTest {
        repository.switchRoutineResult = Result.failure(IllegalStateException("switch failed"))
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.updateCustomRoutineName("My split")
            viewModel.addExerciseToCustomRoutine(ExerciseId("back_pull"))
            viewModel.saveCustomRoutine(startAfterSave = true)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.customRoutineBuilder.visible).isTrue()
            assertThat(state.customRoutineBuilder.error).isEqualTo(CustomRoutineFormError.SAVE_FAILED)
            assertThat(state.activeRoutineProgress?.templateId).isEqualTo("intermediate-body-part-4day")
            assertThat(state.customTemplates.map { it.id }).contains("custom-test")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveCustomRoutine_editingExistingRoutineDoesNotResetProgress() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.updateCustomRoutineName("My split")
            viewModel.addExerciseToCustomRoutine(ExerciseId("back_pull"))
            viewModel.saveCustomRoutine(startAfterSave = true)
            advanceUntilIdle()

            repository.progress.value = repository.progress.value.copy(dayIndex = 1)
            advanceUntilIdle()

            viewModel.editCustomRoutine("custom-test")
            advanceUntilIdle()

            viewModel.updateCustomRoutineName("My edited split")
            viewModel.saveCustomRoutine(startAfterSave = false)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.showCustomRoutineProgressConfirmDialog).isTrue()
            assertThat(viewModel.uiState.value.customRoutineBuilder.visible).isTrue()

            viewModel.keepCustomRoutineProgressAfterEdit()
            advanceUntilIdle()

            val saved = viewModel.uiState.value
            assertThat(saved.customRoutineBuilder.visible).isFalse()
            assertThat(saved.showCustomRoutineProgressConfirmDialog).isFalse()
            assertThat(saved.activeRoutineProgress?.templateId).isEqualTo("custom-test")
            assertThat(saved.activeRoutineProgress?.dayIndex).isEqualTo(1)
            assertThat(saved.customTemplates.single { it.id == "custom-test" }.name)
                .isEqualTo("My edited split")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveCustomRoutine_editingExistingRoutineCanResetCurrentCycle() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.updateCustomRoutineName("My split")
            viewModel.addExerciseToCustomRoutine(ExerciseId("back_pull"))
            viewModel.saveCustomRoutine(startAfterSave = true)
            advanceUntilIdle()

            val dayOneInstanceId = routineDayInstanceId("custom-test", cycleNumber = 1, dayNumber = 1)
            repository.progress.value = repository.progress.value.copy(
                dayIndex = 1,
                routineDayDates = mapOf(dayOneInstanceId to LocalDate.of(2026, 5, 24))
            )
            repository.setLogs(
                listOf(
                    repository.completedLog(
                        dayIndex = 0,
                        performedAt = LocalDateTime.of(2026, 5, 24, 12, 0),
                        routineDayInstanceId = dayOneInstanceId
                    )
                )
            )
            advanceUntilIdle()

            viewModel.editCustomRoutine("custom-test")
            advanceUntilIdle()
            viewModel.updateCustomRoutineName("My reset split")
            viewModel.saveCustomRoutine(startAfterSave = false)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.showCustomRoutineProgressConfirmDialog).isTrue()
            assertThat(repository.currentLogs()).hasSize(1)

            viewModel.resetCustomRoutineProgressAfterEdit()
            advanceUntilIdle()

            val saved = viewModel.uiState.value
            assertThat(saved.customRoutineBuilder.visible).isFalse()
            assertThat(saved.showCustomRoutineProgressConfirmDialog).isFalse()
            assertThat(saved.activeRoutineProgress?.templateId).isEqualTo("custom-test")
            assertThat(saved.activeRoutineProgress?.dayIndex).isEqualTo(0)
            assertThat(saved.activeRoutineProgress?.routineDayDates).isEmpty()
            assertThat(repository.currentLogs()).isEmpty()
            assertThat(saved.customTemplates.single { it.id == "custom-test" }.name)
                .isEqualTo("My reset split")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addExerciseToCustomRoutine_ignoresDuplicatesInSelectedDay() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.addExerciseToCustomRoutine(ExerciseId("back_pull"))
            viewModel.addExerciseToCustomRoutine(ExerciseId("chest_press"))
            viewModel.addExerciseToCustomRoutine(ExerciseId("chest_press"))
            advanceUntilIdle()

            val exerciseIds = viewModel.uiState.value.customRoutineBuilder.days.first().exercises
                .map { it.exercise.id.value }
            assertThat(exerciseIds).containsExactly("back_pull", "chest_press").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun moveCustomRoutineExercise_reordersSelectedDayExercises() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.addExerciseToCustomRoutine(ExerciseId("back_pull"))
            viewModel.addExerciseToCustomRoutine(ExerciseId("chest_press"))
            viewModel.addExerciseToCustomRoutine(ExerciseId("leg_press"))
            viewModel.moveCustomRoutineExerciseUp(2)
            advanceUntilIdle()

            var exerciseIds = viewModel.uiState.value.customRoutineBuilder.days.first().exercises
                .map { it.exercise.id.value }
            assertThat(exerciseIds).containsExactly("back_pull", "leg_press", "chest_press").inOrder()

            viewModel.moveCustomRoutineExerciseDown(1)
            advanceUntilIdle()

            exerciseIds = viewModel.uiState.value.customRoutineBuilder.days.first().exercises
                .map { it.exercise.id.value }
            assertThat(exerciseIds).containsExactly("back_pull", "chest_press", "leg_press").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removeCustomRoutineDay_keepsSelectedLogicalDayWhenRemovingPrecedingDay() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            viewModel.addCustomRoutineDay()
            viewModel.addCustomRoutineDay()
            viewModel.updateCustomRoutineDayFocus(RoutineFocus.BACK)
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.customRoutineBuilder.selectedDayIndex).isEqualTo(2)
            assertThat(viewModel.uiState.value.customRoutineBuilder.days[2].focus).isEqualTo(RoutineFocus.BACK)

            viewModel.removeCustomRoutineDay(0)
            advanceUntilIdle()
            val builder = viewModel.uiState.value.customRoutineBuilder
            assertThat(builder.selectedDayIndex).isEqualTo(1)
            assertThat(builder.days[builder.selectedDayIndex].focus).isEqualTo(RoutineFocus.BACK)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleCustomRoutineExerciseGroup_expandsCollapsesAndResetsOnDayChange() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.showCreateCustomRoutine()
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.customRoutineBuilder.expandedExerciseGroups).isEmpty()

            viewModel.toggleCustomRoutineExerciseGroup(MuscleGroup.CHEST)
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.customRoutineBuilder.expandedExerciseGroups)
                .containsExactly(MuscleGroup.CHEST)

            viewModel.toggleCustomRoutineExerciseGroup(MuscleGroup.CHEST)
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.customRoutineBuilder.expandedExerciseGroups).isEmpty()

            viewModel.toggleCustomRoutineExerciseGroup(MuscleGroup.BACK)
            viewModel.addCustomRoutineDay()
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.customRoutineBuilder.selectedDayIndex).isEqualTo(1)
            assertThat(viewModel.uiState.value.customRoutineBuilder.expandedExerciseGroups).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun viewModel(clock: Clock = fixedClock) = RoutineViewModel(
        observeExercises = ObserveExercisesUseCase(repository),
        observePlanTemplates = ObservePlanTemplatesUseCase(repository),
        observeCurrentRoutineCycle = ObserveCurrentRoutineCycleUseCase(
            routineProgressRepository = repository,
            cyclePlanRepository = repository,
            workoutLogRepository = repository,
            resolveCurrentRoutineCycle = ResolveCurrentRoutineCycleUseCase(),
            clock = clock
        ),
        observeTrainingExperience = ObserveTrainingExperienceUseCase(sessionRepository),
        observeLatestWorkoutLogs = ObserveLatestWorkoutLogsUseCase(repository),
        recommendExercisePrescription = RecommendExercisePrescriptionUseCase(),
        recommendRoutine = RecommendRoutineUseCase(),
        switchRoutineTemplate = SwitchRoutineTemplateUseCase(repository),
        setRoutineDayDate = SetRoutineDayDateUseCase(repository),
        completeRoutineDay = CompleteRoutineDayUseCase(repository, AdvanceRoutineDayUseCase()),
        cancelLatestRoutineDayCompletion = CancelLatestRoutineDayCompletionUseCase(repository),
        saveCustomRoutineUseCase = SaveCustomRoutineUseCase(repository, ValidateCustomRoutineUseCase()),
        clock = clock
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeSessionRepository : SessionRepository {
    private val trainingExperience = MutableStateFlow(TrainingExperience.BEGINNER)

    fun reset() {
        trainingExperience.value = TrainingExperience.BEGINNER
    }

    fun setExperience(experience: TrainingExperience) {
        trainingExperience.value = experience
    }

    override fun observeActiveSession(): Flow<com.smarttrainner.core.model.UserSession?> =
        MutableStateFlow(null)

    override fun observeTrainingExperience(): Flow<TrainingExperience> = trainingExperience

    override suspend fun startDefaultSession(
        nickname: String,
        profileSetup: com.smarttrainner.core.model.ProfileSetup
    ): Result<com.smarttrainner.core.model.UserSession> =
        Result.failure(IllegalStateException("Not used"))

    override suspend fun checkNicknameAvailability(
        nickname: String
    ): Result<com.smarttrainner.core.model.NicknameAvailability> =
        Result.failure(IllegalStateException("Not used"))

    override suspend fun signInWithGoogle(
        idToken: String,
        nickname: String?,
        profileSetup: com.smarttrainner.core.model.ProfileSetup?,
        forceDeviceLogin: Boolean
    ): Result<com.smarttrainner.core.model.UserSession> =
        Result.failure(IllegalStateException("Not used"))

    override suspend fun validateActiveSessionDevice(): Result<Unit> =
        Result.success(Unit)

    override suspend fun setTrainingExperience(experience: TrainingExperience): Result<Unit> {
        trainingExperience.value = experience
        return Result.success(Unit)
    }

    override suspend fun updateBodyProfile(
        gender: com.smarttrainner.core.model.ProfileGender?,
        heightCm: Int,
        weightKg: Double,
        nickname: String?
    ): Result<Unit> =
        Result.failure(IllegalStateException("Not used"))

    override suspend fun logout(): Result<Unit> =
        Result.failure(IllegalStateException("Not used"))
}

private class FakeTrainingRepository :
    ExerciseRepository,
    CyclePlanRepository,
    RoutinePlanCatalogRepository,
    RoutineProgressRepository,
    RoutinePlanCommandRepository,
    RoutineProgressCommandRepository,
    WorkoutLogRepository {
    private val cycleStart = LocalDate.of(2026, 5, 18)
    private val exercises = listOf(
        exercise("back_pull", MuscleGroup.BACK),
        exercise("back_row", MuscleGroup.BACK),
        exercise("chest_press", MuscleGroup.CHEST),
        exercise("leg_press", MuscleGroup.LOWER_BODY),
        exercise(
            "squat_pattern",
            MuscleGroup.LOWER_BODY,
            muscleGroups = listOf(MuscleGroup.LOWER_BODY, MuscleGroup.BACK, MuscleGroup.CORE)
        ),
        exercise("shoulder_raise", MuscleGroup.SHOULDERS)
    )
    private val template = PlanTemplate(
        id = "intermediate-body-part-4day",
        name = "부위 집중 4일 루틴",
        level = PlanLevel.INTERMEDIATE,
        cycleLength = 4,
        description = "test",
        days = listOf(
            templateDay(0, "등 집중", RoutineFocus.BACK, "back_pull", "back_row"),
            templateDay(1, "가슴 집중", RoutineFocus.CHEST, "chest_press"),
            templateDay(2, "하체 집중", RoutineFocus.LOWER_BODY, "leg_press"),
            templateDay(3, "어깨 집중", RoutineFocus.SHOULDERS, "shoulder_raise")
        ),
        structure = RoutineStructure.BODY_PART_SPLIT,
        recommendedExperience = TrainingExperience.INTERMEDIATE,
        sessionMinutes = 50,
        focusSummary = listOf(RoutineFocus.BACK, RoutineFocus.CHEST, RoutineFocus.LOWER_BODY, RoutineFocus.SHOULDERS)
    )
    private val templates = MutableStateFlow(listOf(template))
    private val selectedTemplateId = MutableStateFlow(template.id)

    private fun cyclePlan(
        template: PlanTemplate = this.template,
        cycleStartDate: LocalDate = cycleStart
    ) = CyclePlan(
        id = PlanId("plan"),
        templateId = template.id,
        name = template.name,
        cycleStartDate = cycleStartDate,
        days = template.days.map { day ->
            val date = cycleStartDate.plusDays(day.dayOffset.toLong())
            WorkoutDayPlan(
                date = date,
                title = day.title,
                focus = day.focus,
                exercises = day.exercises.map { templateExercise ->
                    val exercise = exercises.firstOrNull { it.id == templateExercise.exerciseId } ?: exercises.first()
                    PlannedExercise(
                        id = PlannedExerciseId("${date}_${exercise.id.value}"),
                        exercise = exercise,
                        sets = templateExercise.sets,
                        repRange = templateExercise.repRange,
                        durationMinutes = templateExercise.durationMinutes,
                        restSeconds = templateExercise.restSeconds,
                        note = templateExercise.note
                    )
                },
                dayNumber = day.dayNumber,
                primaryFocus = day.primaryFocus,
                secondaryFocuses = day.secondaryFocuses,
                minRecoveryHours = day.minRecoveryHours
            )
        }
    )

    val progress = MutableStateFlow(defaultProgress())
    private val logs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    private val latestLogs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    val requestedPlanCycleStartDates = mutableListOf<LocalDate>()
    val requestedPlanTemplateIds = mutableListOf<String>()
    var switchRoutineResult: Result<Unit> = Result.success(Unit)
    var switchStartedAt: Instant = Instant.parse("2026-05-24T12:00:00Z")

    fun setTemplates(nextTemplates: List<PlanTemplate>) {
        templates.value = nextTemplates
    }

    fun templatesForTest(): List<PlanTemplate> = templates.value

    fun reset() {
        templates.value = listOf(template)
        selectedTemplateId.value = template.id
        progress.value = defaultProgress()
        logs.value = emptyList()
        latestLogs.value = emptyList()
        requestedPlanCycleStartDates.clear()
        requestedPlanTemplateIds.clear()
        switchRoutineResult = Result.success(Unit)
        switchStartedAt = Instant.parse("2026-05-24T12:00:00Z")
    }

    fun setLogs(value: List<WorkoutLog>) {
        logs.value = value
        latestLogs.value = value
    }

    fun currentLogs(): List<WorkoutLog> = logs.value

    fun assignCurrentRoutineDayDate(date: LocalDate) {
        val current = progress.value
        assignRoutineDayDate(
            dayIndex = current.dayIndex,
            cycleNumber = current.cycleNumber,
            date = date
        )
    }

    fun assignRoutineDayDate(
        dayIndex: Int,
        cycleNumber: Int,
        date: LocalDate
    ) {
        val current = progress.value
        val instanceId = routineDayInstanceId(
            templateId = current.templateId,
            cycleNumber = cycleNumber,
            dayNumber = dayIndex + 1
        )
        progress.value = current.copy(
            routineDayDates = current.routineDayDates + (instanceId to date)
        )
    }

    fun setLatestLogs(value: List<WorkoutLog>) {
        latestLogs.value = value
    }

    fun plannedExercise(dayIndex: Int, exerciseIndex: Int = 0): PlannedExercise =
        cyclePlan(template).days[dayIndex].exercises[exerciseIndex]

    fun completedLog(
        dayIndex: Int,
        performedAt: LocalDateTime,
        setEntries: List<WorkoutSetLog> = emptyList(),
        routineDayInstanceId: String? = null
    ): WorkoutLog {
        val planned = plannedExercise(dayIndex)
        return WorkoutLog(
            id = WorkoutLogId(dayIndex.toLong() + 1),
            sessionId = UserSessionId("local-default"),
            plannedExerciseId = planned.id,
            exerciseId = planned.exercise.id,
            performedAt = performedAt,
            sets = planned.sets,
            reps = planned.repRange?.first,
            weightKg = null,
            durationMinutes = planned.durationMinutes,
            memo = "",
            completed = true,
            setEntries = setEntries,
            routineDayInstanceId = routineDayInstanceId
        )
    }

    override fun observeExercises(): Flow<List<Exercise>> = MutableStateFlow(exercises)

    override fun observePlanTemplates(): Flow<List<PlanTemplate>> = templates

    override fun observeCurrentCyclePlan(templateId: String, cycleStartDate: LocalDate): Flow<CyclePlan> {
        requestedPlanTemplateIds += templateId
        requestedPlanCycleStartDates += cycleStartDate
        return templates.map { templates ->
            val selectedTemplate = templates.firstOrNull { it.id == templateId } ?: template
            cyclePlan(selectedTemplate, cycleStartDate)
        }.distinctUntilChanged()
    }

    override fun observeRoutineProgress(): Flow<RoutineProgress> = progress

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = latestLogs

    override fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override suspend fun getExercise(id: ExerciseId): Exercise? = exercises.firstOrNull { it.id == id }

    override suspend fun selectPlanTemplate(templateId: String): Result<Unit> {
        selectedTemplateId.value = templateId
        return Result.success(Unit)
    }

    override suspend fun startRoutine(templateId: String): Result<Unit> {
        selectedTemplateId.value = templateId
        progress.value = defaultProgress(templateId)
        return Result.success(Unit)
    }

    override suspend fun switchRoutineTemplate(templateId: String): Result<Unit> {
        switchRoutineResult.getOrNull() ?: return switchRoutineResult
        selectedTemplateId.value = templateId
        val current = progress.value
        val currentCyclePrefix = routineDayInstancePrefix(
            templateId = current.templateId,
            cycleNumber = current.cycleNumber
        )
        logs.value = logs.value.filterNot { log ->
            log.routineDayInstanceId?.startsWith(currentCyclePrefix) == true
        }
        latestLogs.value = latestLogs.value.filterNot { log ->
            log.routineDayInstanceId?.startsWith(currentCyclePrefix) == true
        }
        progress.value = progress.value.copy(
            templateId = templateId,
            dayIndex = 0,
            startedAt = switchStartedAt,
            cycleStartedAt = switchStartedAt,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            lastCompletedCycleNumber = null,
            lastCompletedPreviousCycleStartedAt = null,
            routineDayDates = emptyMap()
        )
        return Result.success(Unit)
    }

    private fun defaultProgress(templateId: String = template.id): RoutineProgress {
        val cycleStartedAt = cycleStart.atStartOfDay().toInstant(ZoneOffset.UTC)
        return RoutineProgress(
            templateId = templateId,
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            startedAt = cycleStartedAt,
            cycleStartedAt = cycleStartedAt
        )
    }

    override suspend fun saveCustomRoutine(input: CustomRoutineInput): Result<PlanTemplate> {
        val customTemplate = PlanTemplate(
            id = input.id ?: "custom-test",
            name = input.name,
            level = PlanLevel.INTERMEDIATE,
            cycleLength = input.days.size,
            description = input.description,
            days = input.days.mapIndexed { dayIndex, day ->
                PlanTemplateDay(
                    dayOffset = dayIndex,
                    title = day.title,
                    focus = day.focus,
                    exercises = day.exercises.map { exercise ->
                        val repRangeStart = exercise.repRangeStart
                        val repRangeEnd = exercise.repRangeEnd
                        TemplateExercise(
                            exerciseId = exercise.exerciseId,
                            sets = exercise.sets,
                            repRange = if (repRangeStart != null && repRangeEnd != null) {
                                repRangeStart..repRangeEnd
                            } else {
                                null
                            },
                            durationMinutes = exercise.durationMinutes,
                            restSeconds = exercise.restSeconds,
                            note = exercise.note
                        )
                    },
                    dayNumber = dayIndex + 1,
                    primaryFocus = day.primaryFocus,
                    secondaryFocuses = day.secondaryFocuses,
                    minRecoveryHours = day.minRecoveryHours
                )
            },
            focusSummary = input.days.mapNotNull { it.primaryFocus }.distinct(),
            source = RoutineSource.CUSTOM
        )
        templates.value = templates.value.filterNot { it.id == customTemplate.id } + customTemplate
        return Result.success(customTemplate)
    }

    override suspend fun deleteCustomRoutine(templateId: String): Result<Unit> =
        Result.failure(IllegalStateException("Not used"))

    override suspend fun markRoutineDayCompleted(
        completedDayIndex: Int,
        nextDayIndex: Int,
        completedAt: Instant,
        newCycleStartedAt: Instant?
    ): Result<Unit> {
        val current = progress.value
        val startsNewCycle = newCycleStartedAt != null
        progress.value = current.copy(
            dayIndex = nextDayIndex,
            lastCompletedDayIndex = if (startsNewCycle) null else completedDayIndex,
            lastCompletedAt = if (startsNewCycle) null else completedAt,
            cycleNumber = if (startsNewCycle) current.cycleNumber + 1 else current.cycleNumber,
            lastCompletedCycleNumber = if (startsNewCycle) null else current.cycleNumber,
            lastCompletedPreviousCycleStartedAt = if (startsNewCycle) null else current.cycleStartedAt,
            cycleStartedAt = newCycleStartedAt ?: current.cycleStartedAt
        )
        return Result.success(Unit)
    }

    override suspend fun setRoutineDayDate(
        routineDayInstanceId: String,
        assignedDate: LocalDate,
        cycleStartedAt: Instant?
    ): Result<Unit> {
        val current = progress.value
        progress.value = current.copy(
            cycleStartedAt = cycleStartedAt ?: current.cycleStartedAt,
            routineDayDates = current.routineDayDates + (routineDayInstanceId to assignedDate)
        )
        logs.value = logs.value.map { log ->
            if (log.routineDayInstanceId == routineDayInstanceId) {
                log.copy(performedAt = assignedDate.atTime(12, 0))
            } else {
                log
            }
        }
        latestLogs.value = latestLogs.value.map { log ->
            if (log.routineDayInstanceId == routineDayInstanceId) {
                log.copy(performedAt = assignedDate.atTime(12, 0))
            } else {
                log
            }
        }
        return Result.success(Unit)
    }

    override suspend fun cancelLatestRoutineDayCompletion(
        restoredDayIndex: Int,
        restoredCycleNumber: Int,
        restoredCycleStartedAt: Instant?,
        remainingLatestCompletion: RoutineCompletionSnapshot?,
        routineDayInstanceId: String,
        plannedExerciseIds: Set<PlannedExerciseId>,
        additionalExerciseIdPrefix: String
    ): Result<Unit> {
        logs.value = logs.value.filterNot { log ->
            log.routineDayInstanceId == routineDayInstanceId ||
                (
                    log.routineDayInstanceId == null &&
                        (
                            log.plannedExerciseId in plannedExerciseIds ||
                                log.plannedExerciseId.value.startsWith(additionalExerciseIdPrefix)
                            )
                    )
        }
        progress.value = progress.value.copy(
            dayIndex = restoredDayIndex,
            cycleNumber = restoredCycleNumber,
            cycleStartedAt = restoredCycleStartedAt ?: progress.value.cycleStartedAt,
            lastCompletedDayIndex = remainingLatestCompletion?.dayIndex,
            lastCompletedAt = remainingLatestCompletion?.completedAt,
            lastCompletedCycleNumber = remainingLatestCompletion?.cycleNumber,
            lastCompletedPreviousCycleStartedAt = remainingLatestCompletion?.previousCycleStartedAt
        )
        return Result.success(Unit)
    }

}

private fun exercise(
    id: String,
    muscleGroup: MuscleGroup,
    muscleGroups: List<MuscleGroup> = listOf(muscleGroup)
) = Exercise(
    id = ExerciseId(id),
    name = id,
    muscleGroup = muscleGroup,
    muscleGroups = muscleGroups,
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

private fun plannedExercise(id: String) = PlannedExercise(
    id = PlannedExerciseId("planned_$id"),
    exercise = exercise(id, MuscleGroup.CHEST),
    sets = 3,
    repRange = 8..12,
    durationMinutes = null,
    restSeconds = 90,
    note = ""
)

private fun filterTemplate(
    id: String,
    cycleLength: Int,
    sessionMinutes: Int,
    structure: RoutineStructure,
    experience: TrainingExperience
) = PlanTemplate(
    id = id,
    name = id,
    level = when (experience) {
        TrainingExperience.BEGINNER -> PlanLevel.BEGINNER
        TrainingExperience.INTERMEDIATE -> PlanLevel.INTERMEDIATE
        TrainingExperience.ADVANCED -> PlanLevel.ADVANCED
    },
    cycleLength = cycleLength,
    description = id,
    days = List(cycleLength) { index ->
        PlanTemplateDay(
            dayOffset = index,
            title = "$id ${index + 1}",
            focus = "test",
            exercises = listOf(
                TemplateExercise(
                    exerciseId = ExerciseId("duration-${id}-${index + 1}"),
                    sets = 1,
                    repRange = null,
                    durationMinutes = sessionMinutes,
                    restSeconds = 0,
                    note = ""
                )
            ),
            primaryFocus = if (structure == RoutineStructure.FULL_BODY) {
                RoutineFocus.FULL_BODY
            } else {
                RoutineFocus.CHEST
            },
            secondaryFocuses = if (structure == RoutineStructure.BODY_PART_SPLIT) {
                listOf(RoutineFocus.PUSH)
            } else {
                emptyList()
            }
        )
    },
    structure = structure,
    recommendedExperience = experience,
    sessionMinutes = sessionMinutes,
    focusSummary = if (structure == RoutineStructure.FULL_BODY) {
        listOf(RoutineFocus.FULL_BODY)
    } else {
        listOf(
            RoutineFocus.BACK,
            RoutineFocus.CHEST,
            RoutineFocus.LOWER_BODY,
            RoutineFocus.SHOULDERS,
            RoutineFocus.ARMS,
            RoutineFocus.BICEPS,
            RoutineFocus.TRICEPS,
            RoutineFocus.PUSH,
            RoutineFocus.PULL
        )
    }
)

private fun planTemplate(
    id: String,
    name: String,
    source: RoutineSource,
    cycleLength: Int,
    days: List<PlanTemplateDay>
) = PlanTemplate(
    id = id,
    name = name,
    level = PlanLevel.INTERMEDIATE,
    cycleLength = cycleLength,
    description = name,
    days = days,
    structure = RoutineStructure.BODY_PART_SPLIT,
    recommendedExperience = TrainingExperience.INTERMEDIATE,
    sessionMinutes = 45,
    focusSummary = days.mapNotNull { it.primaryFocus },
    source = source
)

private fun templateDay(
    dayOffset: Int,
    title: String,
    focus: RoutineFocus,
    vararg exerciseIds: String
) = PlanTemplateDay(
    dayOffset = dayOffset,
    title = title,
    focus = title,
    exercises = exerciseIds.map { exerciseId ->
        TemplateExercise(
            exerciseId = ExerciseId(exerciseId),
            sets = 3,
            repRange = 8..12,
            durationMinutes = null,
            restSeconds = 90,
            note = ""
        )
    },
    dayNumber = dayOffset + 1,
    primaryFocus = focus,
    secondaryFocuses = listOf(RoutineFocus.ARMS),
    minRecoveryHours = 24
)
