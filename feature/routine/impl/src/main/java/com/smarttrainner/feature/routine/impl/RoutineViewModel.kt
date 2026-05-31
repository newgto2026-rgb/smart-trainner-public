package com.smarttrainner.feature.routine.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObserveWorkoutLogsUseCase
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.feature.routine.domain.CompleteRoutineDayUseCase
import com.smarttrainner.feature.routine.domain.ObserveCurrentWeeklyPlanUseCase
import com.smarttrainner.feature.routine.domain.ObservePlanTemplatesUseCase
import com.smarttrainner.feature.routine.domain.ObserveRoutineProgressUseCase
import com.smarttrainner.feature.routine.domain.RecommendRoutineUseCase
import com.smarttrainner.feature.routine.domain.ResolveRoutineCycleCompletionUseCase
import com.smarttrainner.feature.routine.domain.SaveCustomRoutineUseCase
import com.smarttrainner.feature.routine.domain.StartRoutineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class RoutineViewModel @Inject constructor(
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
    private val weekStart = MutableStateFlow(currentWeekStart())
    private val weekStartFlow = weekStart
        .onStart { refreshWeekStartIfNeeded() }
        .distinctUntilChanged()

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
        RoutineFormControlState(
            recommendationForm = recommendation,
            customRoutineBuilder = builder,
            completeDayFailed = completeDayFailed,
            routineDialogState = routineDialog
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
            RoutineLogState(
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
        RoutineDataState(
            templates = templates,
            plan = routinePlan.plan,
            routineProgress = routinePlan.routineProgress,
            logs = logState.weeklyLogs,
            latestLogs = logState.latestLogs,
            exercises = exercises
        )
    }

    internal val uiState = combine(
        formControlState,
        dataState
    ) { control, data ->
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
            input = control.recommendationForm.toInput(),
            templates = data.templates
        )
        val previewTemplateId = control.routineDialogState.previewTemplateId
            ?: recommendation.primaryTemplateId
        RoutineUiState(
            templates = data.templates,
            selectedTemplateId = data.plan.templateId,
            today = LocalDate.now(clock),
            plan = data.plan,
            activeRoutineProgress = data.routineProgress,
            nextRoutineDay = nextRoutineDay,
            nextRoutineDayUi = nextDayUi,
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
            completedPlannedExerciseIds = completedIds,
            completeDayError = control.completeDayFailed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoutineUiState()
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
            if (index <= 0 || index >= day.exercises.size) {
                day
            } else {
                day.copy(exercises = day.exercises.move(index, index - 1))
            }
        }
    }

    fun moveCustomRoutineExerciseDown(index: Int) {
        customRoutineBuilder.updateSelectedDay { day ->
            if (index < 0 || index >= day.exercises.lastIndex) {
                day
            } else {
                day.copy(exercises = day.exercises.move(index, index + 1))
            }
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
        val shouldStartAfterSave = startAfterSave || builder.editingRoutineId == null
        viewModelScope.launch {
            val result = saveCustomRoutineUseCase(
                input = input,
                availableExerciseIds = state.exercises.map { it.id }.toSet()
            )
            result.onSuccess { template ->
                if (shouldStartAfterSave) {
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

    fun updateRoutineExperience(experience: TrainingExperience) {
        recommendationForm.update { it.copy(experience = experience) }
    }

    fun updateRoutineFeeling(feeling: RoutineFeeling) {
        recommendationForm.update { it.copy(feeling = feeling) }
    }

    fun completeCurrentRoutineDay(onCompleted: () -> Unit = {}) {
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
                onCompleted()
            }
        }
    }

    fun refreshWeekStartIfNeeded() {
        val currentWeekStart = currentWeekStart()
        if (weekStart.value != currentWeekStart) {
            weekStart.value = currentWeekStart
        }
    }

    suspend fun refreshWeekStartOnWeekBoundary() {
        while (currentCoroutineContext().isActive) {
            refreshWeekStartIfNeeded()
            delay(durationUntilNextWeek(weekStart.value))
        }
    }

    private fun currentWeekStart(): LocalDate =
        LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private fun durationUntilNextWeek(currentWeekStart: LocalDate): Long {
        val nextWeekStart = currentWeekStart
            .plusWeeks(1)
            .atTime(LocalTime.MIDNIGHT)
            .atZone(clock.zone)
            .toInstant()
        return Duration.between(clock.instant(), nextWeekStart).toMillis().coerceAtLeast(1L)
    }

    private data class RoutineDataState(
        val templates: List<PlanTemplate>,
        val plan: WeeklyPlan,
        val routineProgress: RoutineProgress,
        val logs: List<WorkoutLog>,
        val latestLogs: List<WorkoutLog>,
        val exercises: List<com.smarttrainner.core.model.Exercise>
    )

    private data class RoutineLogState(
        val weeklyLogs: List<WorkoutLog>,
        val latestLogs: List<WorkoutLog>
    )

    private data class RoutinePlanState(
        val plan: WeeklyPlan,
        val routineProgress: RoutineProgress
    )

    private data class RoutineFormControlState(
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
