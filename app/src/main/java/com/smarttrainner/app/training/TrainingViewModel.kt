package com.smarttrainner.app.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.CompleteRoutineDayUseCase
import com.smarttrainner.core.domain.ObserveCurrentWeeklyPlanUseCase
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObservePlanTemplatesUseCase
import com.smarttrainner.core.domain.ObserveRoutineProgressUseCase
import com.smarttrainner.core.domain.ObserveWorkoutLogsUseCase
import com.smarttrainner.core.domain.RecommendRoutineUseCase
import com.smarttrainner.core.domain.ResolveRoutineCycleCompletionUseCase
import com.smarttrainner.core.domain.SaveCustomRoutineUseCase
import com.smarttrainner.core.domain.StartRoutineUseCase
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineRecommendation
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.feature.exercise.api.ExerciseCatalogUiState
import com.smarttrainner.feature.routine.api.CustomRoutineBuilderState
import com.smarttrainner.feature.routine.api.CustomRoutineDayFormState
import com.smarttrainner.feature.routine.api.CustomRoutineExerciseFormState
import com.smarttrainner.feature.routine.api.CustomRoutineFormError
import com.smarttrainner.feature.routine.api.NextRoutineDayUiModel
import com.smarttrainner.feature.routine.api.RoutineRecommendationFormState
import com.smarttrainner.feature.routine.api.RoutineUiState
import com.smarttrainner.feature.routine.api.allowedCustomRoutineMuscleGroups
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TrainingViewModel @Inject constructor(
    observeExercises: ObserveExercisesUseCase,
    observePlanTemplates: ObservePlanTemplatesUseCase,
    observeCurrentWeeklyPlan: ObserveCurrentWeeklyPlanUseCase,
    observeRoutineProgress: ObserveRoutineProgressUseCase,
    observeWorkoutLogs: ObserveWorkoutLogsUseCase,
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase,
    private val recommendRoutine: RecommendRoutineUseCase,
    private val resolveRoutineCycleCompletion: ResolveRoutineCycleCompletionUseCase,
    private val startRoutine: StartRoutineUseCase,
    private val completeRoutineDay: CompleteRoutineDayUseCase,
    private val saveCustomRoutineUseCase: SaveCustomRoutineUseCase,
    private val clock: Clock
) : ViewModel() {
    private val weekStartFlow = flow {
        emit(currentWeekStart())
    }.distinctUntilChanged()

    private val selectedExerciseId = MutableStateFlow<ExerciseId?>(null)
    private val selectedPlannedExerciseId = MutableStateFlow<PlannedExerciseId?>(null)
    private val recordingMode = MutableStateFlow(RecordingMode.SINGLE)
    private val recommendationForm = MutableStateFlow(RoutineRecommendationFormState())
    private val routinePreviewTemplateId = MutableStateFlow<String?>(null)
    private val showRoutineLibraryDialog = MutableStateFlow(false)
    private val showRoutineSettingsDialog = MutableStateFlow(false)
    private val showRoutineRecommendationsDialog = MutableStateFlow(false)
    private val customRoutineBuilder = MutableStateFlow(CustomRoutineBuilderState())
    private val completeDayFailed = MutableStateFlow(false)

    private val routineDialogState = combine(
        routinePreviewTemplateId,
        showRoutineLibraryDialog,
        showRoutineSettingsDialog,
        showRoutineRecommendationsDialog
    ) { previewTemplateId, showLibrary, showSettings, showRecommendations ->
        RoutineDialogState(
            previewTemplateId = previewTemplateId,
            showLibrary = showLibrary,
            showSettings = showSettings,
            showRecommendations = showRecommendations
        )
    }

    private val formControlState = combine(
        recommendationForm,
        customRoutineBuilder,
        completeDayFailed,
        routineDialogState
    ) { recommendation, builder, completeDayFailed, routineDialog ->
        TrainingFormControlState(
            recommendationForm = recommendation,
            customRoutineBuilder = builder,
            completeDayFailed = completeDayFailed,
            routineDialogState = routineDialog
        )
    }

    private val controlState = combine(
        selectedExerciseId,
        selectedPlannedExerciseId,
        formControlState
    ) { exerciseId, plannedExerciseId, formControl ->
        TrainingControlState(
            selectedExerciseId = exerciseId,
            selectedPlannedExerciseId = plannedExerciseId,
            recommendationForm = formControl.recommendationForm,
            customRoutineBuilder = formControl.customRoutineBuilder,
            completeDayFailed = formControl.completeDayFailed,
            routineDialogState = formControl.routineDialogState
        )
    }

    private val routinePlanState = weekStartFlow.flatMapLatest { weekStart ->
        combine(
            observeCurrentWeeklyPlan(weekStart),
            observeRoutineProgress()
        ) { plan, progress ->
            RoutinePlanState(plan = plan, routineProgress = progress)
        }
    }

    private val logState = weekStartFlow.flatMapLatest { weekStart ->
        combine(
            observeWorkoutLogs(weekStart),
            observeLatestWorkoutLogs()
        ) { weeklyLogs, latestLogs ->
            TrainingLogState(
                weeklyLogs = weeklyLogs,
                latestLogs = latestLogs
            )
        }
    }

    private val dataState = combine(
        observePlanTemplates(),
        routinePlanState,
        logState,
        observeExercises()
    ) { templates, routinePlan, logState, exercises ->
        TrainingDataState(
            templates = templates,
            plan = routinePlan.plan,
            routineProgress = routinePlan.routineProgress,
            logs = logState.weeklyLogs,
            latestLogs = logState.latestLogs,
            exercises = exercises
        )
    }

    private val presentationState = combine(
        dataState,
        recommendationForm
    ) { data, recommendationForm ->
        val completedIds = resolveRoutineCycleCompletion(data.logs, data.routineProgress, clock.zone)
        val activeTemplate = data.templates.firstOrNull { it.id == data.routineProgress.templateId }
            ?: data.templates.firstOrNull { it.id == data.plan.templateId }
        val nextDayIndex = data.routineProgress.dayIndex.coerceIn(
            0,
            (data.plan.days.size - 1).coerceAtLeast(0)
        )
        val nextRoutineDay = data.plan.days.getOrNull(nextDayIndex) ?: data.plan.days.firstOrNull()
        val nextDayUi = nextRoutineDay?.toNextRoutineDayUiModel(
            template = activeTemplate,
            dayIndex = nextDayIndex,
            completedIds = completedIds
        )
        val recommendation = recommendRoutine(
            input = recommendationForm.toInput(),
            templates = data.templates
        )
        TrainingPresentationState(
            data = data,
            completedIds = completedIds,
            nextRoutineDay = nextRoutineDay,
            nextRoutineDayUi = nextDayUi,
            recommendation = recommendation
        )
    }

    val uiState = combine(
        controlState,
        presentationState
    ) { control, presentation ->
        val data = presentation.data
        val recommendation = presentation.recommendation
        val previewTemplateId = control.routineDialogState.previewTemplateId
            ?: recommendation.primaryTemplateId
        val recordingPlanned = data.plan.findPlannedExercise(control.selectedPlannedExerciseId)
        val selectedPlanned = recordingPlanned
            ?: presentation.nextRoutineDayUi?.startExercise
            ?: data.plan.firstIncomplete(presentation.completedIds)
        TrainingUiState(
            routine = RoutineUiState(
                templates = data.templates,
                selectedTemplateId = data.plan.templateId,
                today = LocalDate.now(clock),
                plan = data.plan,
                activeRoutineProgress = data.routineProgress,
                nextRoutineDay = presentation.nextRoutineDay,
                nextRoutineDayUi = presentation.nextRoutineDayUi,
                routineRecommendationInput = control.recommendationForm,
                recommendedTemplateId = recommendation.primaryTemplateId,
                alternativeTemplateIds = recommendation.alternativeTemplateIds,
                routinePreviewTemplateId = previewTemplateId,
                showRoutineLibraryDialog = control.routineDialogState.showLibrary,
                showRoutineSettingsDialog = control.routineDialogState.showSettings,
                showRoutineRecommendationsDialog = control.routineDialogState.showRecommendations,
                customRoutineBuilder = control.customRoutineBuilder,
                exercises = data.exercises,
                logs = data.logs,
                latestWorkoutLogs = data.latestLogs,
                completedPlannedExerciseIds = presentation.completedIds,
                completeDayError = control.completeDayFailed
            ),
            exerciseCatalog = ExerciseCatalogUiState(
                exercises = data.exercises,
                latestWorkoutLogs = data.latestLogs,
                selectedExerciseId = control.selectedExerciseId
            ),
            recordingPlannedExercise = recordingPlanned,
            selectedExerciseId = control.selectedExerciseId,
            selectedPlannedExercise = selectedPlanned
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingUiState()
    )

    fun selectTemplate(templateId: String) {
        viewModelScope.launch {
            val result = startRoutine(templateId)
            if (result.isSuccess) {
                showRoutineLibraryDialog.value = false
            }
        }
    }

    fun showRoutineLibrary() {
        routinePreviewTemplateId.value = null
        showRoutineSettingsDialog.value = false
        showRoutineRecommendationsDialog.value = false
        showRoutineLibraryDialog.value = true
    }

    fun dismissRoutineLibrary() {
        showRoutineLibraryDialog.value = false
    }

    fun showRoutineSettings() {
        routinePreviewTemplateId.value = null
        showRoutineLibraryDialog.value = false
        showRoutineRecommendationsDialog.value = false
        showRoutineSettingsDialog.value = true
    }

    fun dismissRoutineSettings() {
        showRoutineSettingsDialog.value = false
    }

    fun showRoutineRecommendations() {
        routinePreviewTemplateId.value = null
        showRoutineLibraryDialog.value = false
        showRoutineSettingsDialog.value = false
        showRoutineRecommendationsDialog.value = true
    }

    fun dismissRoutineRecommendations() {
        showRoutineRecommendationsDialog.value = false
        routinePreviewTemplateId.value = null
    }

    fun selectRoutinePreview(templateId: String) {
        routinePreviewTemplateId.value = templateId
    }

    fun startPreviewRoutine() {
        val state = uiState.value
        val templateId = state.routinePreviewTemplateId ?: state.recommendedTemplateId ?: return
        viewModelScope.launch {
            startRoutine(templateId)
            showRoutineLibraryDialog.value = false
            showRoutineRecommendationsDialog.value = false
            showRoutineSettingsDialog.value = false
            routinePreviewTemplateId.value = null
        }
    }

    fun showCreateCustomRoutine() {
        showRoutineLibraryDialog.value = false
        customRoutineBuilder.value = CustomRoutineBuilderState(
            visible = true,
            name = "",
            days = listOf(defaultBuilderDay())
        )
    }

    fun copyTemplateToCustom(templateId: String) {
        val state = uiState.value
        val template = state.templates.firstOrNull { it.id == templateId } ?: return
        val exercisesById = state.exercises.associateBy { it.id }
        showRoutineLibraryDialog.value = false
        customRoutineBuilder.value = CustomRoutineBuilderState(
            visible = true,
            name = "${template.name} Copy",
            days = template.days.map { day ->
                CustomRoutineDayFormState(
                    title = day.title,
                    focus = day.primaryFocus,
                    exercises = day.exercises.mapNotNull { templateExercise ->
                        exercisesById[templateExercise.exerciseId]?.let { exercise ->
                            CustomRoutineExerciseFormState(
                                exercise = exercise,
                                sets = templateExercise.sets,
                                repRangeStart = templateExercise.repRange?.first,
                                repRangeEnd = templateExercise.repRange?.last,
                                durationMinutes = templateExercise.durationMinutes,
                                restSeconds = templateExercise.restSeconds,
                                note = templateExercise.note
                            )
                        }
                    }
                )
            }.ifEmpty { listOf(defaultBuilderDay()) }
        )
    }

    fun editCustomRoutine(templateId: String) {
        val state = uiState.value
        val template = state.customTemplates.firstOrNull { it.id == templateId } ?: return
        val exercisesById = state.exercises.associateBy { it.id }
        showRoutineLibraryDialog.value = false
        customRoutineBuilder.value = CustomRoutineBuilderState(
            visible = true,
            editingRoutineId = template.id,
            name = template.name,
            days = template.days.map { day ->
                CustomRoutineDayFormState(
                    title = day.title,
                    focus = day.primaryFocus,
                    exercises = day.exercises.mapNotNull { templateExercise ->
                        exercisesById[templateExercise.exerciseId]?.let { exercise ->
                            CustomRoutineExerciseFormState(
                                exercise = exercise,
                                sets = templateExercise.sets,
                                repRangeStart = templateExercise.repRange?.first,
                                repRangeEnd = templateExercise.repRange?.last,
                                durationMinutes = templateExercise.durationMinutes,
                                restSeconds = templateExercise.restSeconds,
                                note = templateExercise.note
                            )
                        }
                    }
                )
            }.ifEmpty { listOf(defaultBuilderDay()) }
        )
    }

    fun dismissCustomRoutineBuilder() {
        customRoutineBuilder.value = CustomRoutineBuilderState()
    }

    fun updateCustomRoutineName(value: String) {
        customRoutineBuilder.update { it.copy(name = value.take(60), error = null, savedTemplateId = null) }
    }

    fun selectCustomRoutineDay(index: Int) {
        customRoutineBuilder.update { builder ->
            if (index !in builder.days.indices) {
                builder
            } else {
                builder.copy(
                    selectedDayIndex = index,
                    expandedExerciseGroups = emptySet()
                )
            }
        }
    }

    fun addCustomRoutineDay() {
        customRoutineBuilder.update { builder ->
            if (builder.days.size >= MAX_CUSTOM_ROUTINE_DAYS) {
                builder
            } else {
                builder.copy(
                    selectedDayIndex = builder.days.size,
                    days = builder.days + defaultBuilderDay(),
                    expandedExerciseGroups = emptySet(),
                    error = null
                )
            }
        }
    }

    fun removeCustomRoutineDay(index: Int) {
        customRoutineBuilder.update { builder ->
            if (builder.days.size <= 1 || index !in builder.days.indices) {
                builder
            } else {
                val nextDays = builder.days.filterIndexed { dayIndex, _ -> dayIndex != index }
                val nextSelectedDayIndex = if (builder.selectedDayIndex > index) {
                    builder.selectedDayIndex - 1
                } else {
                    builder.selectedDayIndex.coerceAtMost(nextDays.lastIndex)
                }
                builder.copy(
                    days = nextDays,
                    selectedDayIndex = nextSelectedDayIndex,
                    expandedExerciseGroups = emptySet(),
                    error = null
                )
            }
        }
    }

    fun updateCustomRoutineDayFocus(focus: RoutineFocus?) {
        customRoutineBuilder.update { builder ->
            val selectedIndex = builder.selectedDayIndex
            if (selectedIndex !in builder.days.indices) {
                builder
            } else {
                val nextFocus = focus?.takeUnless { it == RoutineFocus.FULL_BODY }
                val allowedGroups = allowedCustomRoutineMuscleGroups(nextFocus)
                builder.copy(
                    days = builder.days.mapIndexed { index, day ->
                        if (index == selectedIndex) {
                            day.copy(
                                focus = nextFocus,
                                exercises = day.exercises.filter { form ->
                                    form.exercise.muscleGroup in allowedGroups
                                }
                            )
                        } else {
                            day
                        }
                    },
                    expandedExerciseGroups = emptySet(),
                    error = null,
                    savedTemplateId = null
                )
            }
        }
    }

    fun toggleCustomRoutineExerciseGroup(group: MuscleGroup) {
        customRoutineBuilder.update { builder ->
            val nextGroups = if (group in builder.expandedExerciseGroups) {
                builder.expandedExerciseGroups - group
            } else {
                builder.expandedExerciseGroups + group
            }
            builder.copy(expandedExerciseGroups = nextGroups)
        }
    }

    fun addExerciseToCustomRoutine(exerciseId: ExerciseId) {
        val exercise = uiState.value.exercises.firstOrNull { it.id == exerciseId } ?: return
        val builder = customRoutineBuilder.value
        val selectedDay = builder.days.getOrNull(builder.selectedDayIndex)
        if (exercise.muscleGroup !in allowedCustomRoutineMuscleGroups(selectedDay?.focus)) return
        customRoutineBuilder.updateSelectedDay { day ->
            if (day.exercises.any { it.exercise.id == exerciseId }) {
                day
            } else {
                day.copy(exercises = day.exercises + exercise.toCustomRoutineExerciseForm())
            }
        }
    }

    fun removeExerciseFromCustomRoutine(index: Int) {
        customRoutineBuilder.updateSelectedDay { day ->
            if (index !in day.exercises.indices) {
                day
            } else {
                day.copy(exercises = day.exercises.filterIndexed { exerciseIndex, _ -> exerciseIndex != index })
            }
        }
    }

    fun moveCustomRoutineExerciseUp(index: Int) {
        customRoutineBuilder.updateSelectedDay { day ->
            day.copy(exercises = day.exercises.move(index, index - 1))
        }
    }

    fun moveCustomRoutineExerciseDown(index: Int) {
        customRoutineBuilder.updateSelectedDay { day ->
            day.copy(exercises = day.exercises.move(index, index + 1))
        }
    }

    fun saveCustomRoutine(startAfterSave: Boolean) {
        val state = uiState.value
        val builder = customRoutineBuilder.value
        val input = builder.toInput()
        val validationError = builder.toFormError()
        if (input == null || validationError != null) {
            customRoutineBuilder.update { it.copy(error = validationError ?: CustomRoutineFormError.EMPTY_DAY) }
            return
        }
        viewModelScope.launch {
            val result = saveCustomRoutineUseCase(
                input = input,
                availableExerciseIds = state.exercises.map { it.id }.toSet()
            )
            result.onSuccess { template ->
                if (startAfterSave) {
                    startRoutine(template.id)
                }
                customRoutineBuilder.value = CustomRoutineBuilderState()
            }.onFailure {
                customRoutineBuilder.update { it.copy(error = CustomRoutineFormError.SAVE_FAILED) }
            }
        }
    }

    fun updateRoutineDaysPerWeek(daysPerWeek: Int) {
        recommendationForm.update { it.copy(daysPerWeek = daysPerWeek) }
    }

    fun updateRoutineSessionMinutes(sessionMinutes: Int) {
        recommendationForm.update { it.copy(sessionMinutes = sessionMinutes) }
    }

    fun updateRoutineExperience(experience: com.smarttrainner.core.model.TrainingExperience) {
        recommendationForm.update { it.copy(experience = experience) }
    }

    fun updateRoutineFeeling(feeling: com.smarttrainner.core.model.RoutineFeeling) {
        recommendationForm.update { it.copy(feeling = feeling) }
    }

    fun completeCurrentRoutineDay() {
        val state = uiState.value
        val template = state.templates.firstOrNull { it.id == state.activeRoutineProgress?.templateId }
            ?: state.templates.firstOrNull { it.id == state.selectedTemplateId }
            ?: return
        val dayIndex = state.activeRoutineProgress?.dayIndex
            ?.coerceIn(0, (template.cycleLength - 1).coerceAtLeast(0))
            ?: 0
        viewModelScope.launch {
            val result = completeRoutineDay(
                template = template,
                completedDayIndex = dayIndex,
                completedAt = clock.instant()
            )
            completeDayFailed.value = result.isFailure
            if (result.isSuccess) {
                selectedPlannedExerciseId.value = null
            }
        }
    }

    fun selectExercise(exerciseId: ExerciseId) {
        selectedExerciseId.value = exerciseId
    }

    fun showExerciseMethod(exerciseId: ExerciseId) {
        selectedExerciseId.value = exerciseId
    }

    fun dismissExerciseDetail() {
        selectedExerciseId.value = null
    }

    fun selectPlannedExercise(exercise: PlannedExercise) {
        recordingMode.value = RecordingMode.SINGLE
        selectedPlannedExerciseId.value = exercise.id
        selectedExerciseId.value = null
    }

    fun startWorkout(exercise: PlannedExercise) {
        recordingMode.value = RecordingMode.ROUTINE
        selectedPlannedExerciseId.value = exercise.id
        selectedExerciseId.value = null
    }

    fun dismissRecordDialog() {
        selectedPlannedExerciseId.value = null
        recordingMode.value = RecordingMode.SINGLE
    }

    fun handleRecordSaved(planned: PlannedExercise) {
        val continueRoutine = recordingMode.value == RecordingMode.ROUTINE
        val nextPlanned = if (continueRoutine) {
            uiState.value.plan?.nextIncompleteInSameDay(
                currentId = planned.id,
                completedIds = uiState.value.completedPlannedExerciseIds + planned.id
            )
        } else {
            null
        }
        if (nextPlanned != null) {
            selectedPlannedExerciseId.value = nextPlanned.id
        } else {
            selectedPlannedExerciseId.value = null
            recordingMode.value = RecordingMode.SINGLE
        }
    }

    private fun currentWeekStart(): LocalDate =
        LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private data class TrainingPresentationState(
        val data: TrainingDataState,
        val completedIds: Set<PlannedExerciseId>,
        val nextRoutineDay: WorkoutDayPlan?,
        val nextRoutineDayUi: NextRoutineDayUiModel?,
        val recommendation: RoutineRecommendation
    )

    private data class TrainingDataState(
        val templates: List<PlanTemplate>,
        val plan: WeeklyPlan,
        val routineProgress: RoutineProgress,
        val logs: List<com.smarttrainner.core.model.WorkoutLog>,
        val latestLogs: List<com.smarttrainner.core.model.WorkoutLog>,
        val exercises: List<com.smarttrainner.core.model.Exercise>
    )

    private data class TrainingLogState(
        val weeklyLogs: List<com.smarttrainner.core.model.WorkoutLog>,
        val latestLogs: List<com.smarttrainner.core.model.WorkoutLog>
    )

    private data class RoutinePlanState(
        val plan: WeeklyPlan,
        val routineProgress: RoutineProgress
    )

    private data class TrainingFormControlState(
        val recommendationForm: RoutineRecommendationFormState,
        val customRoutineBuilder: CustomRoutineBuilderState,
        val completeDayFailed: Boolean,
        val routineDialogState: RoutineDialogState
    )

    private data class TrainingControlState(
        val selectedExerciseId: ExerciseId?,
        val selectedPlannedExerciseId: PlannedExerciseId?,
        val recommendationForm: RoutineRecommendationFormState,
        val customRoutineBuilder: CustomRoutineBuilderState,
        val completeDayFailed: Boolean,
        val routineDialogState: RoutineDialogState
    )

    private data class RoutineDialogState(
        val previewTemplateId: String?,
        val showLibrary: Boolean,
        val showSettings: Boolean,
        val showRecommendations: Boolean
    )
}
