package com.smarttrainner.feature.training.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.CompleteRoutineDayUseCase
import com.smarttrainner.core.domain.AdvanceRoutineDayUseCase
import com.smarttrainner.core.domain.GetLatestWorkoutLogUseCase
import com.smarttrainner.core.domain.ObserveCurrentWeeklyPlanUseCase
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObservePlanTemplatesUseCase
import com.smarttrainner.core.domain.ObserveRoutineProgressUseCase
import com.smarttrainner.core.domain.ObserveWeeklySummaryUseCase
import com.smarttrainner.core.domain.ObserveWorkoutLogsUseCase
import com.smarttrainner.core.domain.RecommendRoutineUseCase
import com.smarttrainner.core.domain.ResolveRoutineCycleCompletionUseCase
import com.smarttrainner.core.domain.SaveCustomRoutineUseCase
import com.smarttrainner.core.domain.SaveWorkoutLogUseCase
import com.smarttrainner.core.domain.StartRoutineUseCase
import com.smarttrainner.core.domain.TrainingRepository
import com.smarttrainner.core.domain.ValidateCustomRoutineUseCase
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
import com.smarttrainner.core.model.WorkoutLogInput
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingViewModelRoutineTest {
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
            assertThat(state.selectedPlannedExercise?.exercise?.id?.value).isEqualTo("chest_press")
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
    fun showExerciseMethod_keepsPlanTabSelected() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)

            viewModel.selectTab(TrainingTab.PLAN)
            viewModel.selectPlannedExercise(repository.plannedExercise(1))
            viewModel.showExerciseMethod(repository.plannedExercise(1).exercise.id)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.selectedTab).isEqualTo(TrainingTab.PLAN)
            assertThat(state.selectedExercise?.id?.value).isEqualTo("chest_press")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun startWorkout_saveRecordAdvancesToNextExerciseInSameDay() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.startWorkout(repository.plannedExercise(dayIndex = 0, exerciseIndex = 0))
            viewModel.saveRecord()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.recordingPlannedExercise?.id)
                .isEqualTo(repository.plannedExercise(dayIndex = 0, exerciseIndex = 1).id)
            assertThat(state.recordSaved).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectPlannedExercise_saveRecordClosesSingleRecordDialog() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.selectPlannedExercise(repository.plannedExercise(dayIndex = 1))
            viewModel.saveRecord()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.recordingPlannedExercise).isNull()
            assertThat(state.recordSaved).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun startWorkout_prefillsSetCountRepsAndWeightFromLatestExerciseLog() = runTest {
        repository.setLogs(
            listOf(
                repository.completedLog(
                    dayIndex = 0,
                    performedAt = LocalDateTime.of(2026, 5, 19, 7, 0),
                    setEntries = listOf(
                        WorkoutSetLog(order = 1, reps = 7, weightKg = 42.5, durationMinutes = null),
                        WorkoutSetLog(order = 2, reps = 8, weightKg = 45.0, durationMinutes = null, restSeconds = 120),
                        WorkoutSetLog(order = 3, reps = 6, weightKg = 47.5, durationMinutes = null, restSeconds = 150),
                        WorkoutSetLog(order = 4, reps = 5, weightKg = 50.0, durationMinutes = null, restSeconds = 180)
                    )
                )
            )
        )
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            awaitItem()

            viewModel.startWorkout(repository.plannedExercise(dayIndex = 0, exerciseIndex = 0))
            advanceUntilIdle()

            val setEntries = viewModel.uiState.value.recordForm.setEntries
            assertThat(setEntries.map { it.reps }).containsExactly("7", "8", "6", "5").inOrder()
            assertThat(setEntries.map { it.weightKg }).containsExactly("42.5", "45", "47.5", "50").inOrder()
            assertThat(setEntries.map { it.restSeconds }).containsExactly("90", "120", "150", "180").inOrder()
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
    fun saveCustomRoutine_keepsCurrentRoutineAndClosesBuilder() = runTest {
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

    private fun viewModel() = TrainingViewModel(
        observeExercises = ObserveExercisesUseCase(repository),
        observePlanTemplates = ObservePlanTemplatesUseCase(repository),
        observeCurrentWeeklyPlan = ObserveCurrentWeeklyPlanUseCase(repository),
        observeRoutineProgress = ObserveRoutineProgressUseCase(repository),
        observeWorkoutLogs = ObserveWorkoutLogsUseCase(repository),
        observeLatestWorkoutLogs = ObserveLatestWorkoutLogsUseCase(repository),
        observeWeeklySummary = ObserveWeeklySummaryUseCase(repository),
        getLatestWorkoutLog = GetLatestWorkoutLogUseCase(repository),
        recommendRoutine = RecommendRoutineUseCase(),
        resolveRoutineCycleCompletion = ResolveRoutineCycleCompletionUseCase(),
        startRoutine = StartRoutineUseCase(repository),
        completeRoutineDay = CompleteRoutineDayUseCase(repository, AdvanceRoutineDayUseCase()),
        saveCustomRoutineUseCase = SaveCustomRoutineUseCase(repository, ValidateCustomRoutineUseCase()),
        saveWorkoutLog = SaveWorkoutLogUseCase(repository),
        clock = fixedClock
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

private class FakeTrainingRepository : TrainingRepository {
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

    fun reset() {
        progress.value = RoutineProgress(template.id, 0, null, null)
        logs.value = emptyList()
    }

    fun setLogs(value: List<WorkoutLog>) {
        logs.value = value
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

    override fun observeCustomRoutines(): Flow<List<PlanTemplate>> = MutableStateFlow(
        templates.value.filter { it.source == RoutineSource.CUSTOM }
    )

    override fun observeCurrentWeeklyPlan(weekStartDate: LocalDate): Flow<WeeklyPlan> = MutableStateFlow(plan)

    override fun observeRoutineProgress(): Flow<RoutineProgress> = progress

    override fun observeWorkoutLogs(weekStartDate: LocalDate): Flow<List<WorkoutLog>> = logs

    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = logs

    override fun observeWeeklySummary(weekStartDate: LocalDate): Flow<WeeklySummary> = MutableStateFlow(
        WeeklySummary(
            weekStartDate = weekStartDate,
            plannedExerciseCount = 4,
            completedExerciseCount = 0,
            totalSets = 0,
            totalVolumeKg = 0.0,
            totalMinutes = 0,
            streakDays = 0,
            muscleBalance = emptyMap(),
            insight = ""
        )
    )

    override suspend fun getExercise(id: ExerciseId): Exercise? = exercises.firstOrNull { it.id == id }

    override suspend fun getLatestWorkoutLog(exerciseId: ExerciseId): WorkoutLog? =
        logs.value
            .filter { it.exerciseId == exerciseId }
            .maxByOrNull { it.performedAt }

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

    override suspend fun saveWorkoutLog(input: WorkoutLogInput): Result<Unit> {
        logs.value = logs.value + WorkoutLog(
            id = WorkoutLogId(logs.value.size.toLong() + 1),
            sessionId = UserSessionId("local-default"),
            plannedExerciseId = input.plannedExerciseId,
            exerciseId = input.exerciseId,
            performedAt = input.performedAt,
            sets = input.sets,
            reps = input.reps,
            weightKg = input.weightKg,
            durationMinutes = input.durationMinutes,
            memo = input.memo,
            completed = input.completed,
            setEntries = input.setEntries
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
