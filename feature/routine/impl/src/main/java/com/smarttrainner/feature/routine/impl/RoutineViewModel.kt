package com.smarttrainner.feature.routine.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObserveTrainingExperienceUseCase
import com.smarttrainner.core.domain.ObserveWorkoutLogsUseCase
import com.smarttrainner.core.domain.RecommendExercisePrescriptionUseCase
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.targetsAnyMuscleGroup
import com.smarttrainner.feature.routine.domain.CancelLatestRoutineDayCompletionUseCase
import com.smarttrainner.feature.routine.domain.CompleteRoutineDayUseCase
import com.smarttrainner.feature.routine.domain.ObserveCurrentWeeklyPlanUseCase
import com.smarttrainner.feature.routine.domain.ObservePlanTemplatesUseCase
import com.smarttrainner.feature.routine.domain.ObserveRoutineProgressUseCase
import com.smarttrainner.feature.routine.domain.RecommendRoutineUseCase
import com.smarttrainner.feature.routine.domain.ResolveRoutineCycleCompletionUseCase
import com.smarttrainner.feature.routine.domain.SaveCustomRoutineUseCase
import com.smarttrainner.feature.routine.domain.SwitchRoutineTemplateUseCase
import com.smarttrainner.feature.routine.domain.routineAdditionalExerciseIdPrefix
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
    observeTrainingExperience: ObserveTrainingExperienceUseCase,
    observeWorkoutLogs: ObserveWorkoutLogsUseCase,
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase,
    private val recommendExercisePrescription: RecommendExercisePrescriptionUseCase,
    private val recommendRoutine: RecommendRoutineUseCase,
    private val resolveRoutineCycleCompletion: ResolveRoutineCycleCompletionUseCase,
    private val switchRoutineTemplate: SwitchRoutineTemplateUseCase,
    private val completeRoutineDay: CompleteRoutineDayUseCase,
    private val cancelLatestRoutineDayCompletion: CancelLatestRoutineDayCompletionUseCase,
    private val saveCustomRoutineUseCase: SaveCustomRoutineUseCase,
    private val clock: Clock
) : ViewModel() {
    private val weekStart = MutableStateFlow(currentWeekStart())
    private val weekStartFlow = weekStart
        .onStart { refreshWeekStartIfNeeded() }
        .distinctUntilChanged()

    private val recommendationForm = MutableStateFlow(
        RoutineRecommendationFormState.defaultFor(TrainingExperience.BEGINNER)
    )
    private val routinePreviewTemplateId = MutableStateFlow<String?>(null)
    private val showRoutineLibraryDialog = MutableStateFlow(false)
    private val showRoutineSettingsDialog = MutableStateFlow(false)
    private val showRoutineRecommendationsDialog = MutableStateFlow(false)
    private val completionConfirm = MutableStateFlow<RoutineCompletionConfirmState?>(null)
    private val routineExercisePicker = MutableStateFlow<RoutineExercisePickerState?>(null)
    private val showCancelLatestRoutineDayDialog = MutableStateFlow(false)
    private val customRoutineBuilder = MutableStateFlow(CustomRoutineBuilderState())
    private val completeDayFailed = MutableStateFlow(false)

    private val routineModalState = combine(
        completionConfirm,
        routineExercisePicker,
        showCancelLatestRoutineDayDialog
    ) { completionConfirm, exercisePicker, showCancelLatest ->
        RoutineModalState(
            completionConfirm = completionConfirm,
            exercisePicker = exercisePicker,
            showCancelLatest = showCancelLatest
        )
    }

    private val routineDialogState = combine(
        routinePreviewTemplateId,
        showRoutineLibraryDialog,
        showRoutineSettingsDialog,
        showRoutineRecommendationsDialog,
        routineModalState
    ) { previewTemplateId, showLibrary, showSettings, showRecommendations, modal ->
        RoutineDialogState(
            previewTemplateId = previewTemplateId,
            showLibrary = showLibrary,
            showSettings = showSettings,
            showRecommendations = showRecommendations,
            completionConfirm = modal.completionConfirm,
            exercisePicker = modal.exercisePicker,
            showCancelLatest = modal.showCancelLatest
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
        observeExercises(),
        observeTrainingExperience()
    ) { templates, routinePlan, logState, exercises, profileExperience ->
        RoutineDataState(
            templates = templates,
            plan = routinePlan.plan,
            routineProgress = routinePlan.routineProgress,
            logs = logState.weeklyLogs,
            latestLogs = logState.latestLogs,
            exercises = exercises,
            profileExperience = profileExperience
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
            cycleNumber = data.routineProgress.cycleNumber,
            completedIds = completedIds
        )
        val normalizedRecommendationForm = control.recommendationForm.normalizedFor(data.templates)
        val routineFilterAvailability = normalizedRecommendationForm.availableFilterOptions(data.templates)
        val exercisePrescriptions = data.exercises.associate { exercise ->
            exercise.id to recommendExercisePrescription(exercise, data.profileExperience)
        }
        val latestCompletion = data.routineProgress.lastCompletedDayIndex
            ?.let { data.plan.days.getOrNull(it) }
            ?.let { completedDay ->
                LatestRoutineDayCompletionUiModel(
                    day = completedDay,
                    routineTemplate = activeTemplate,
                    cycleNumber = data.routineProgress.lastCompletedCycleNumber
                        ?: data.routineProgress.cycleNumber,
                    dayNumber = completedDay.dayNumber,
                    completedAtText = data.routineProgress.lastCompletedAt?.toString()
                )
            }
        val recommendation = recommendRoutine(
            input = normalizedRecommendationForm.toInput(),
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
            latestRoutineDayCompletion = latestCompletion,
            profileExperience = data.profileExperience,
            routineRecommendationInput = normalizedRecommendationForm,
            routineFilterAvailability = routineFilterAvailability,
            exercisePrescriptions = exercisePrescriptions,
            recommendedTemplateId = recommendation.primaryTemplateId,
            alternativeTemplateIds = recommendation.alternativeTemplateIds,
            routinePreviewTemplateId = previewTemplateId,
            showRoutineLibraryDialog = control.routineDialogState.showLibrary,
            showRoutineSettingsDialog = control.routineDialogState.showSettings,
            showRoutineRecommendationsDialog = control.routineDialogState.showRecommendations,
            routineCompletionConfirm = control.routineDialogState.completionConfirm,
            routineExercisePicker = control.routineDialogState.exercisePicker,
            showCancelLatestRoutineDayDialog = control.routineDialogState.showCancelLatest,
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

    init {
        viewModelScope.launch {
            observeTrainingExperience()
                .distinctUntilChanged()
                .collect { experience ->
                    recommendationForm.value = RoutineRecommendationFormState.defaultFor(experience)
                }
        }
    }

    fun selectTemplate(templateId: String) {
        viewModelScope.launch {
            val result = switchRoutineTemplate(templateId)
            if (result.isSuccess) {
                showRoutineLibraryDialog.value = false
            }
        }
    }

    fun showRoutineLibrary() {
        routinePreviewTemplateId.value = null
        showRoutineSettingsDialog.value = false
        showRoutineRecommendationsDialog.value = false
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
        showRoutineLibraryDialog.value = true
    }

    fun dismissRoutineLibrary() {
        showRoutineLibraryDialog.value = false
    }

    fun showRoutineSettings() {
        routinePreviewTemplateId.value = null
        showRoutineLibraryDialog.value = false
        showRoutineRecommendationsDialog.value = false
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
        showRoutineSettingsDialog.value = true
    }

    fun dismissRoutineSettings() {
        showRoutineSettingsDialog.value = false
    }

    fun showRoutineRecommendations() {
        routinePreviewTemplateId.value = null
        showRoutineLibraryDialog.value = false
        showRoutineSettingsDialog.value = false
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
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
            switchRoutineTemplate(templateId)
            showRoutineLibraryDialog.value = false
            showRoutineRecommendationsDialog.value = false
            showRoutineSettingsDialog.value = false
            routinePreviewTemplateId.value = null
        }
    }

    fun showCreateCustomRoutine() {
        showRoutineLibraryDialog.value = false
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
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
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
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
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
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
                                    form.exercise.targetsAnyMuscleGroup(allowedGroups)
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
        val state = uiState.value
        val exercise = state.exercises.firstOrNull { it.id == exerciseId } ?: return
        val builder = customRoutineBuilder.value
        val selectedDay = builder.days.getOrNull(builder.selectedDayIndex)
        if (!exercise.targetsAnyMuscleGroup(allowedCustomRoutineMuscleGroups(selectedDay?.focus))) return
        val prescription = state.exercisePrescriptions[exerciseId]
            ?: recommendExercisePrescription(exercise, state.profileExperience)
        customRoutineBuilder.updateSelectedDay { day ->
            if (day.exercises.any { it.exercise.id == exerciseId }) {
                day
            } else {
                day.copy(exercises = day.exercises + exercise.toCustomRoutineExerciseForm(prescription))
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
        viewModelScope.launch {
            val result = saveCustomRoutineUseCase(
                input = input,
                availableExerciseIds = state.exercises.map { it.id }.toSet()
            )
            result.onSuccess { template ->
                if (startAfterSave) {
                    switchRoutineTemplate(template.id)
                }
                customRoutineBuilder.value = CustomRoutineBuilderState()
            }.onFailure {
                customRoutineBuilder.update { it.copy(error = CustomRoutineFormError.SAVE_FAILED) }
            }
        }
    }

    fun updateRoutineDaysPerWeek(daysPerWeek: Int) {
        updateRecommendationForm { it.copy(daysPerWeek = daysPerWeek) }
    }

    fun updateRoutineSessionMinutes(sessionMinutes: Int) {
        updateRecommendationForm { it.copy(sessionMinutes = sessionMinutes) }
    }

    fun updateRoutineExperience(experience: TrainingExperience) {
        updateRecommendationForm { it.copy(experience = experience) }
    }

    fun updateRoutineFeeling(feeling: RoutineFeeling) {
        updateRecommendationForm { it.copy(feeling = feeling) }
    }

    fun requestCompleteCurrentRoutineDay(
        skippedPlannedExerciseIds: Set<PlannedExerciseId>,
        justRecordedPlannedExerciseIds: Set<PlannedExerciseId>
    ) {
        val state = uiState.value
        val routineDay = state.nextRoutineDayUi ?: return
        val effectiveCompletedIds = state.completedPlannedExerciseIds + justRecordedPlannedExerciseIds
        val unrecordedExercises = routineDay.previewExercises.filter { exercise ->
            exercise.id !in effectiveCompletedIds || exercise.id in skippedPlannedExerciseIds
        }
        if (unrecordedExercises.isEmpty()) {
            completeCurrentRoutineDay()
        } else {
            completionConfirm.value = RoutineCompletionConfirmState(
                reason = if (skippedPlannedExerciseIds.isEmpty()) {
                    RoutineCompletionConfirmReason.MANUAL
                } else {
                    RoutineCompletionConfirmReason.SESSION_ENDED_WITH_SKIPS
                },
                skippedExerciseIds = skippedPlannedExerciseIds,
                unrecordedExercises = unrecordedExercises
            )
        }
    }

    fun confirmCompleteCurrentRoutineDay(onCompleted: () -> Unit = {}) {
        completionConfirm.value = null
        completeCurrentRoutineDay(onCompleted)
    }

    fun dismissCompleteRoutineDayConfirmation() {
        completionConfirm.value = null
    }

    fun requestCancelLatestRoutineDay() {
        if (uiState.value.latestRoutineDayCompletion != null) {
            showCancelLatestRoutineDayDialog.value = true
        }
    }

    fun dismissCancelLatestRoutineDay() {
        showCancelLatestRoutineDayDialog.value = false
    }

    fun confirmCancelLatestRoutineDay(onCanceled: () -> Unit = {}) {
        val state = uiState.value
        val progress = state.activeRoutineProgress ?: return
        val template = activeTemplate(state) ?: return
        val latestCompletion = state.latestRoutineDayCompletion ?: return
        viewModelScope.launch {
            val result = cancelLatestRoutineDayCompletion(
                template = template,
                progress = progress,
                completedDay = latestCompletion.day
            )
            completeDayFailed.value = result.isFailure
            if (result.isSuccess) {
                showCancelLatestRoutineDayDialog.value = false
                onCanceled()
            }
        }
    }

    fun requestSubstituteExercise(plannedExercise: PlannedExercise) {
        routineExercisePicker.value = RoutineExercisePickerState(
            mode = RoutineExercisePickerMode.SUBSTITUTE,
            anchorExercise = plannedExercise
        )
    }

    fun requestAdditionalExercise(anchorExercise: PlannedExercise?) {
        routineExercisePicker.value = RoutineExercisePickerState(
            mode = RoutineExercisePickerMode.ADD,
            anchorExercise = anchorExercise
        )
    }

    fun dismissRoutineExercisePicker() {
        routineExercisePicker.value = null
    }

    internal fun selectRoutineSessionExercise(exerciseId: ExerciseId): RoutineExercisePickResult? {
        val state = uiState.value
        val picker = state.routineExercisePicker ?: return null
        val exercise = state.exercises.firstOrNull { it.id == exerciseId } ?: return null
        val anchor = picker.anchorExercise
        val selected = when (picker.mode) {
            RoutineExercisePickerMode.SUBSTITUTE -> {
                anchor?.copy(exercise = exercise) ?: return null
            }
            RoutineExercisePickerMode.ADD -> {
                val routineDay = state.nextRoutineDayUi ?: return null
                val prescription = state.exercisePrescriptions[exercise.id]
                    ?: recommendExercisePrescription(exercise, state.profileExperience)
                PlannedExercise(
                    id = PlannedExerciseId(
                        routineAdditionalExerciseIdPrefix(
                            templateId = state.activeRoutineProgress?.templateId
                                ?: state.selectedTemplateId,
                            cycleNumber = state.activeRoutineProgress?.cycleNumber ?: routineDay.cycleNumber,
                            dayNumber = routineDay.dayNumber
                        ) + "${clock.instant().toEpochMilli()}|${exercise.id.value}"
                    ),
                    exercise = exercise,
                    sets = prescription.sets,
                    repRange = prescription.repRange,
                    durationMinutes = prescription.durationMinutes,
                    restSeconds = prescription.restSeconds,
                    note = ""
                )
            }
        }
        routineExercisePicker.value = null
        return RoutineExercisePickResult(
            mode = picker.mode,
            plannedExercise = selected
        )
    }

    fun completeCurrentRoutineDay(onCompleted: () -> Unit = {}) {
        val state = uiState.value
        val template = activeTemplate(state)
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

    private fun updateRecommendationForm(
        transform: (RoutineRecommendationFormState) -> RoutineRecommendationFormState
    ) {
        val templates = uiState.value.templates
        recommendationForm.update { transform(it).normalizedFor(templates) }
    }

    private fun activeTemplate(state: RoutineUiState): PlanTemplate? =
        state.templates.firstOrNull { it.id == state.activeRoutineProgress?.templateId }
            ?: state.templates.firstOrNull { it.id == state.selectedTemplateId }

    private data class RoutineDataState(
        val templates: List<PlanTemplate>,
        val plan: WeeklyPlan,
        val routineProgress: RoutineProgress,
        val logs: List<WorkoutLog>,
        val latestLogs: List<WorkoutLog>,
        val exercises: List<com.smarttrainner.core.model.Exercise>,
        val profileExperience: TrainingExperience
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

    private data class RoutineModalState(
        val completionConfirm: RoutineCompletionConfirmState?,
        val exercisePicker: RoutineExercisePickerState?,
        val showCancelLatest: Boolean
    )

    private data class RoutineDialogState(
        val previewTemplateId: String?,
        val showLibrary: Boolean,
        val showSettings: Boolean,
        val showRecommendations: Boolean,
        val completionConfirm: RoutineCompletionConfirmState?,
        val exercisePicker: RoutineExercisePickerState?,
        val showCancelLatest: Boolean
    )
}
