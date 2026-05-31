package com.smarttrainner.feature.routine.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.ExerciseRepository
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObserveWorkoutLogsUseCase
import com.smarttrainner.core.domain.WeeklyPlanRepository
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
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TemplateExercise
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSessionId
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogId
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.feature.routine.domain.AdvanceRoutineDayUseCase
import com.smarttrainner.feature.routine.domain.CompleteRoutineDayUseCase
import com.smarttrainner.feature.routine.domain.ObserveCurrentWeeklyPlanUseCase
import com.smarttrainner.feature.routine.domain.ObservePlanTemplatesUseCase
import com.smarttrainner.feature.routine.domain.ObserveRoutineProgressUseCase
import com.smarttrainner.feature.routine.domain.RecommendRoutineUseCase
import com.smarttrainner.feature.routine.domain.RoutinePlanCatalogRepository
import com.smarttrainner.feature.routine.domain.RoutinePlanCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressCommandRepository
import com.smarttrainner.feature.routine.domain.RoutineProgressRepository
import com.smarttrainner.feature.routine.domain.ResolveRoutineCycleCompletionUseCase
import com.smarttrainner.feature.routine.domain.SaveCustomRoutineUseCase
import com.smarttrainner.feature.routine.domain.StartRoutineUseCase
import com.smarttrainner.feature.routine.domain.ValidateCustomRoutineUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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

    @Before
    fun setUp() {
        repository.reset()
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
    fun uiState_refreshesWeeklySourcesWhenCollectionRestartsAfterWeekChange() = runTest {
        val clock = MutableClock(fixedInstant, fixedClock.zone)
        val viewModel = viewModel(clock)

        viewModel.uiState.test {
            skipItems(1)
            assertThat(awaitItem().plan?.weekStartDate).isEqualTo(LocalDate.of(2026, 5, 18))
            cancelAndIgnoreRemainingEvents()
        }

        clock.setInstant(Instant.parse("2026-05-25T12:00:00Z"))
        advanceTimeBy(5_001)
        runCurrent()

        viewModel.uiState.test {
            assertThat(awaitItem().plan?.weekStartDate).isEqualTo(LocalDate.of(2026, 5, 18))
            assertThat(awaitItem().plan?.weekStartDate).isEqualTo(LocalDate.of(2026, 5, 25))
            assertThat(repository.requestedPlanWeekStartDates)
                .containsAtLeast(LocalDate.of(2026, 5, 18), LocalDate.of(2026, 5, 25))
            assertThat(repository.requestedLogWeekStartDates)
                .containsAtLeast(LocalDate.of(2026, 5, 18), LocalDate.of(2026, 5, 25))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_refreshesWeeklySourcesWhenWeekBoundaryPassesWhileSubscribed() = runTest {
        val clock = MutableClock(Instant.parse("2026-05-24T23:59:59Z"), fixedClock.zone)
        val viewModel = viewModel(clock)
        backgroundScope.launch {
            viewModel.refreshWeekStartOnWeekBoundary()
        }

        viewModel.uiState.test {
            skipItems(1)
            assertThat(awaitItem().plan?.weekStartDate).isEqualTo(LocalDate.of(2026, 5, 18))

            clock.setInstant(Instant.parse("2026-05-25T00:00:00Z"))
            advanceTimeBy(1_000)
            runCurrent()

            assertThat(awaitItem().plan?.weekStartDate).isEqualTo(LocalDate.of(2026, 5, 25))
            assertThat(repository.requestedPlanWeekStartDates)
                .containsAtLeast(LocalDate.of(2026, 5, 18), LocalDate.of(2026, 5, 25))
            assertThat(repository.requestedLogWeekStartDates)
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
            completedIds = emptySet()
        )

        assertThat(uiModel.previewExercises.map { it.id }).containsExactlyElementsIn(exercises.map { it.id }).inOrder()
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
            daysPerWeek = 0,
            description = "",
            days = emptyList(),
            sessionMinutes = 45,
            source = RoutineSource.SYSTEM
        )

        val uiModel = day.toNextRoutineDayUiModel(
            template = emptyTemplate,
            dayIndex = 0,
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
            assertThat(state.nextRoutineDayUi?.startExercise?.id).isEqualTo(repository.plannedExercise(0).id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun completeCurrentRoutineDay_advancesProgressAndWrapsAfterLastDay() = runTest {
        repository.progress.value = RoutineProgress(
            templateId = "intermediate-body-part-4day",
            dayIndex = 3,
            lastCompletedDayIndex = null,
            lastCompletedAt = null,
            startedAt = Instant.parse("2026-05-20T00:00:00Z"),
            cycleStartedAt = Instant.parse("2026-05-20T00:00:00Z")
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            assertThat(awaitItem().nextRoutineDayUi?.dayNumber).isEqualTo(4)

            viewModel.completeCurrentRoutineDay()
            advanceUntilIdle()

            val advanced = awaitItem()
            assertThat(advanced.activeRoutineProgress?.dayIndex).isEqualTo(0)
            assertThat(advanced.activeRoutineProgress?.lastCompletedAt).isEqualTo(fixedInstant)
            assertThat(advanced.activeRoutineProgress?.cycleStartedAt).isEqualTo(fixedInstant)
            assertThat(advanced.nextRoutineDayUi?.dayNumber).isEqualTo(1)
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
    fun saveCustomRoutine_makesNewRoutineCurrentAndClosesBuilder() = runTest {
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
            assertThat(saved.activeRoutineProgress?.templateId).isEqualTo("custom-test")
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
        observeCurrentWeeklyPlan = ObserveCurrentWeeklyPlanUseCase(repository),
        observeRoutineProgress = ObserveRoutineProgressUseCase(repository),
        observeWorkoutLogs = ObserveWorkoutLogsUseCase(repository),
        observeLatestWorkoutLogs = ObserveLatestWorkoutLogsUseCase(repository),
        recommendRoutine = RecommendRoutineUseCase(),
        resolveRoutineCycleCompletion = ResolveRoutineCycleCompletionUseCase(),
        startRoutine = StartRoutineUseCase(repository),
        completeRoutineDay = CompleteRoutineDayUseCase(repository, AdvanceRoutineDayUseCase()),
        saveCustomRoutineUseCase = SaveCustomRoutineUseCase(repository, ValidateCustomRoutineUseCase()),
        clock = clock
    )
}

private class MutableClock(
    private var currentInstant: Instant,
    private val currentZone: ZoneId
) : Clock() {
    fun setInstant(instant: Instant) {
        currentInstant = instant
    }

    override fun getZone(): ZoneId = currentZone

    override fun withZone(zone: ZoneId): Clock = MutableClock(currentInstant, zone)

    override fun instant(): Instant = currentInstant
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

private class FakeTrainingRepository :
    ExerciseRepository,
    WeeklyPlanRepository,
    RoutinePlanCatalogRepository,
    RoutineProgressRepository,
    RoutinePlanCommandRepository,
    RoutineProgressCommandRepository,
    WorkoutLogRepository {
    private val weekStart = LocalDate.of(2026, 5, 18)
    private val exercises = listOf(
        exercise("back_pull", MuscleGroup.BACK),
        exercise("back_row", MuscleGroup.BACK),
        exercise("chest_press", MuscleGroup.CHEST),
        exercise("leg_press", MuscleGroup.LOWER_BODY),
        exercise("shoulder_raise", MuscleGroup.SHOULDERS)
    )
    private val template = PlanTemplate(
        id = "intermediate-body-part-4day",
        name = "부위 집중 4일 루틴",
        level = PlanLevel.INTERMEDIATE,
        daysPerWeek = 4,
        description = "test",
        days = listOf(
            templateDay(0, "등 집중", RoutineFocus.BACK, "back_pull", "back_row"),
            templateDay(1, "가슴 집중", RoutineFocus.CHEST, "chest_press"),
            templateDay(2, "하체 집중", RoutineFocus.LOWER_BODY, "leg_press"),
            templateDay(3, "어깨 집중", RoutineFocus.SHOULDERS, "shoulder_raise")
        ),
        structure = RoutineStructure.BODY_PART_SPLIT,
        recommendedExperience = TrainingExperience.INTERMEDIATE,
        cycleLength = 4,
        sessionMinutes = 50,
        focusSummary = listOf(RoutineFocus.BACK, RoutineFocus.CHEST, RoutineFocus.LOWER_BODY, RoutineFocus.SHOULDERS)
    )
    private val templates = MutableStateFlow(listOf(template))
    private val plan = WeeklyPlan(
        id = PlanId("plan"),
        templateId = template.id,
        name = template.name,
        weekStartDate = weekStart,
        days = template.days.map { day ->
            val date = weekStart.plusDays(day.dayOffset.toLong())
            WorkoutDayPlan(
                date = date,
                title = day.title,
                focus = day.focus,
                exercises = day.exercises.map { templateExercise ->
                    val exercise = exercises.first { it.id == templateExercise.exerciseId }
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

    val progress = MutableStateFlow(
        RoutineProgress(
            templateId = template.id,
            dayIndex = 0,
            lastCompletedDayIndex = null,
            lastCompletedAt = null
        )
    )
    private val logs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    private val latestLogs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    val requestedPlanWeekStartDates = mutableListOf<LocalDate>()
    val requestedLogWeekStartDates = mutableListOf<LocalDate>()

    fun reset() {
        progress.value = RoutineProgress(template.id, 0, null, null)
        logs.value = emptyList()
        latestLogs.value = emptyList()
        requestedPlanWeekStartDates.clear()
        requestedLogWeekStartDates.clear()
    }

    fun setLogs(value: List<WorkoutLog>) {
        logs.value = value
        latestLogs.value = value
    }

    fun setLatestLogs(value: List<WorkoutLog>) {
        latestLogs.value = value
    }

    fun plannedExercise(dayIndex: Int, exerciseIndex: Int = 0): PlannedExercise =
        plan.days[dayIndex].exercises[exerciseIndex]

    fun completedLog(
        dayIndex: Int,
        performedAt: LocalDateTime,
        setEntries: List<WorkoutSetLog> = emptyList()
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
            setEntries = setEntries
        )
    }

    override fun observeExercises(): Flow<List<Exercise>> = MutableStateFlow(exercises)

    override fun observePlanTemplates(): Flow<List<PlanTemplate>> = templates

    override fun observeCurrentWeeklyPlan(weekStartDate: LocalDate): Flow<WeeklyPlan> {
        requestedPlanWeekStartDates += weekStartDate
        return MutableStateFlow(plan.copy(weekStartDate = weekStartDate))
    }

    override fun observeRoutineProgress(): Flow<RoutineProgress> = progress

    override fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>> {
        requestedLogWeekStartDates += weekStartDate
        return logs
    }

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = latestLogs

    override suspend fun getExercise(id: ExerciseId): Exercise? = exercises.firstOrNull { it.id == id }

    override suspend fun selectPlanTemplate(templateId: String): Result<Unit> = Result.success(Unit)

    override suspend fun startRoutine(templateId: String): Result<Unit> {
        progress.value = RoutineProgress(templateId, 0, null, null)
        return Result.success(Unit)
    }

    override suspend fun saveCustomRoutine(input: CustomRoutineInput): Result<PlanTemplate> {
        val customTemplate = PlanTemplate(
            id = input.id ?: "custom-test",
            name = input.name,
            level = PlanLevel.INTERMEDIATE,
            daysPerWeek = input.days.size,
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
            cycleLength = input.days.size,
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
        progress.value = RoutineProgress(
            templateId = current.templateId,
            dayIndex = nextDayIndex,
            lastCompletedDayIndex = completedDayIndex,
            lastCompletedAt = completedAt,
            startedAt = current.startedAt,
            cycleStartedAt = newCycleStartedAt ?: current.cycleStartedAt
        )
        return Result.success(Unit)
    }

}

private fun exercise(id: String, muscleGroup: MuscleGroup) = Exercise(
    id = ExerciseId(id),
    name = id,
    muscleGroup = muscleGroup,
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
