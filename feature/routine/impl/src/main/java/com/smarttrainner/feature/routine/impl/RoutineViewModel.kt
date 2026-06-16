package com.smarttrainner.feature.routine.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveCurrentRoutineCycleUseCase
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObserveTrainingExperienceUseCase
import com.smarttrainner.core.domain.RecommendExercisePrescriptionUseCase
import com.smarttrainner.core.model.CurrentRoutineCycle
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.isRoutineAdditionalExerciseId
import com.smarttrainner.core.model.routineDayInstanceId
import com.smarttrainner.core.model.targetsAnyMuscleGroup
import com.smarttrainner.core.model.routineAdditionalExerciseIdPrefix
import com.smarttrainner.feature.routine.domain.CancelLatestRoutineDayCompletionUseCase
import com.smarttrainner.feature.routine.domain.CompleteRoutineDayUseCase
import com.smarttrainner.feature.routine.domain.ObservePlanTemplatesUseCase
import com.smarttrainner.feature.routine.domain.RecommendRoutineUseCase
import com.smarttrainner.feature.routine.domain.SaveCustomRoutineUseCase
import com.smarttrainner.feature.routine.domain.SetRoutineDayDateUseCase
import com.smarttrainner.feature.routine.domain.SwitchRoutineTemplateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class RoutineViewModel @Inject constructor(
    observeExercises: ObserveExercisesUseCase,
    observePlanTemplates: ObservePlanTemplatesUseCase,
    observeCurrentRoutineCycle: ObserveCurrentRoutineCycleUseCase,
    observeTrainingExperience: ObserveTrainingExperienceUseCase,
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase,
    private val recommendExercisePrescription: RecommendExercisePrescriptionUseCase,
    private val recommendRoutine: RecommendRoutineUseCase,
    private val switchRoutineTemplate: SwitchRoutineTemplateUseCase,
    private val completeRoutineDay: CompleteRoutineDayUseCase,
    private val cancelLatestRoutineDayCompletion: CancelLatestRoutineDayCompletionUseCase,
    private val setRoutineDayDate: SetRoutineDayDateUseCase,
    private val saveCustomRoutineUseCase: SaveCustomRoutineUseCase,
    private val clock: Clock
) : ViewModel() {
    private val recommendationForm = MutableStateFlow(
        RoutineRecommendationFormState.defaultFor(TrainingExperience.BEGINNER)
    )
    private val routinePreviewTemplateId = MutableStateFlow<String?>(null)
    private val showRoutineLibraryDialog = MutableStateFlow(false)
    private val showRoutineSettingsDialog = MutableStateFlow(false)
    private val showRoutineRecommendationsDialog = MutableStateFlow(false)
    private val routineSwitchConfirmTemplateId = MutableStateFlow<String?>(null)
    private val completionConfirm = MutableStateFlow<RoutineCompletionConfirmState?>(null)
    private val routineDayDatePicker = MutableStateFlow<RoutineDayDatePickerState?>(null)
    private val routineDayDateError = MutableStateFlow<RoutineDayDateError?>(null)
    private val routineExercisePicker = MutableStateFlow<RoutineExercisePickerState?>(null)
    private val showCancelLatestRoutineDayDialog = MutableStateFlow(false)
    private val showCustomRoutineProgressConfirmDialog = MutableStateFlow(false)
    private val customRoutineBuilder = MutableStateFlow(CustomRoutineBuilderState())
    private val completeDayFailed = MutableStateFlow(false)
    private var pendingRoutineDayDateAction: PendingRoutineDayDateAction? = null
    private var completeRoutineDayInFlight = false

    private val routineModalState = combine(
        completionConfirm,
        routineDayDatePicker,
        routineExercisePicker,
        showCancelLatestRoutineDayDialog,
        showCustomRoutineProgressConfirmDialog
    ) { completionConfirm, datePicker, exercisePicker, showCancelLatest, showCustomProgressConfirm ->
        RoutineModalState(
            completionConfirm = completionConfirm,
            datePicker = datePicker,
            exercisePicker = exercisePicker,
            showCancelLatest = showCancelLatest,
            showCustomProgressConfirm = showCustomProgressConfirm
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
            datePicker = modal.datePicker,
            exercisePicker = modal.exercisePicker,
            showCancelLatest = modal.showCancelLatest,
            showCustomProgressConfirm = modal.showCustomProgressConfirm
        )
    }

    private val formBaseState = combine(
        recommendationForm,
        customRoutineBuilder,
        completeDayFailed,
        routineDayDateError,
        routineDialogState
    ) { recommendation, builder, completeDayFailed, dateError, routineDialog ->
        RoutineFormControlState(
            recommendationForm = recommendation,
            customRoutineBuilder = builder,
            completeDayFailed = completeDayFailed,
            routineDayDateError = dateError,
            routineDialogState = routineDialog
        )
    }

    private val formControlState = combine(
        formBaseState,
        routineSwitchConfirmTemplateId
    ) { control, switchTemplateId ->
        control.copy(routineSwitchConfirmTemplateId = switchTemplateId)
    }

    private val dataState = combine(
        observePlanTemplates(),
        observeCurrentRoutineCycle(clock.zone),
        observeLatestWorkoutLogs(),
        observeExercises(),
        observeTrainingExperience()
    ) { templates, currentCycle, latestLogs, exercises, profileExperience ->
        RoutineDataState(
            templates = templates,
            currentCycle = currentCycle,
            latestLogs = latestLogs,
            exercises = exercises,
            profileExperience = profileExperience
        )
    }

    internal val uiState = combine(
        formControlState,
        dataState
    ) { control, data ->
        val currentCycle = data.currentCycle
        val progress = currentCycle.progress
        val plan = currentCycle.plan
        val activeTemplate = data.templates.firstOrNull { it.id == progress.templateId }
            ?: data.templates.firstOrNull { it.id == plan.templateId }
        val nextDayIndex = currentCycle.currentDayIndex
        val nextRoutineDay = currentCycle.currentDay
        val completedIds = currentCycle.currentDayCompletedPlannedExerciseIds
        val nextDayUi = nextRoutineDay?.toNextRoutineDayUiModel(
            template = activeTemplate,
            dayIndex = nextDayIndex,
            cycleNumber = progress.cycleNumber,
            routineDayDate = currentCycle.currentRoutineDayDate,
            previousRoutineDayDate = currentCycle.previousRoutineDayDate,
            completedIds = completedIds
        )
        val normalizedRecommendationForm = control.recommendationForm.normalizedFor(data.templates)
        val routineFilterAvailability = normalizedRecommendationForm.availableFilterOptions(data.templates)
        val exercisePrescriptions = data.exercises.associate { exercise ->
            exercise.id to recommendExercisePrescription(exercise, data.profileExperience)
        }
        val latestCompletion = currentCycle.latestCompletion?.let { completion ->
            LatestRoutineDayCompletionUiModel(
                day = completion.day,
                routineTemplate = activeTemplate,
                cycleNumber = completion.cycleNumber,
                dayNumber = completion.dayNumber,
                completedAtText = completion.completedAt?.toString()
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
            selectedTemplateId = plan.templateId,
            today = LocalDate.now(clock),
            plan = plan,
            activeRoutineProgress = progress,
            nextRoutineDay = nextDayUi?.day ?: nextRoutineDay,
            nextRoutineDayUi = nextDayUi,
            latestRoutineDayCompletion = latestCompletion,
            profileExperience = data.profileExperience,
            routineRecommendationInput = normalizedRecommendationForm,
            routineFilterAvailability = routineFilterAvailability,
            exercisePrescriptions = exercisePrescriptions,
            recommendedTemplateId = recommendation.primaryTemplateId,
            alternativeTemplateIds = recommendation.alternativeTemplateIds,
            routinePreviewTemplateId = previewTemplateId,
            routineSwitchConfirmTemplateId = control.routineSwitchConfirmTemplateId,
            showRoutineLibraryDialog = control.routineDialogState.showLibrary,
            showRoutineSettingsDialog = control.routineDialogState.showSettings,
            showRoutineRecommendationsDialog = control.routineDialogState.showRecommendations,
            routineCompletionConfirm = control.routineDialogState.completionConfirm,
            routineDayDatePicker = control.routineDialogState.datePicker,
            routineExercisePicker = control.routineDialogState.exercisePicker,
            showCancelLatestRoutineDayDialog = control.routineDialogState.showCancelLatest,
            showCustomRoutineProgressConfirmDialog = control.routineDialogState.showCustomProgressConfirm,
            customRoutineBuilder = control.customRoutineBuilder,
            exercises = data.exercises,
            logs = currentCycle.currentCycleLogs,
            latestWorkoutLogs = data.latestLogs,
            completedPlannedExerciseIds = completedIds,
            completeDayError = control.completeDayFailed,
            routineDayDateError = control.routineDayDateError
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
        if (templateId == uiState.value.activeRoutineProgress?.templateId) {
            showRoutineLibraryDialog.value = false
            return
        }
        routineSwitchConfirmTemplateId.value = templateId
    }

    fun confirmRoutineSwitch() {
        val templateId = routineSwitchConfirmTemplateId.value ?: return
        viewModelScope.launch {
            val result = switchRoutineTemplate(templateId)
            if (result.isSuccess) {
                routineSwitchConfirmTemplateId.value = null
                showRoutineLibraryDialog.value = false
                showRoutineRecommendationsDialog.value = false
                showRoutineSettingsDialog.value = false
                routinePreviewTemplateId.value = null
            }
        }
    }

    fun dismissRoutineSwitchConfirmation() {
        routineSwitchConfirmTemplateId.value = null
    }

    fun showRoutineLibrary() {
        routinePreviewTemplateId.value = null
        routineSwitchConfirmTemplateId.value = null
        showRoutineSettingsDialog.value = false
        showRoutineRecommendationsDialog.value = false
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
        showCustomRoutineProgressConfirmDialog.value = false
        showRoutineLibraryDialog.value = true
    }

    fun dismissRoutineLibrary() {
        routineSwitchConfirmTemplateId.value = null
        showRoutineLibraryDialog.value = false
    }

    fun showRoutineSettings() {
        routinePreviewTemplateId.value = null
        routineSwitchConfirmTemplateId.value = null
        showRoutineLibraryDialog.value = false
        showRoutineRecommendationsDialog.value = false
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
        showCustomRoutineProgressConfirmDialog.value = false
        showRoutineSettingsDialog.value = true
    }

    fun dismissRoutineSettings() {
        showRoutineSettingsDialog.value = false
    }

    fun showRoutineRecommendations() {
        routinePreviewTemplateId.value = null
        routineSwitchConfirmTemplateId.value = null
        showRoutineLibraryDialog.value = false
        showRoutineSettingsDialog.value = false
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
        showRoutineRecommendationsDialog.value = true
    }

    fun dismissRoutineRecommendations() {
        routineSwitchConfirmTemplateId.value = null
        showRoutineRecommendationsDialog.value = false
        routinePreviewTemplateId.value = null
    }

    fun selectRoutinePreview(templateId: String) {
        routinePreviewTemplateId.value = templateId
    }

    fun startPreviewRoutine() {
        val state = uiState.value
        val templateId = state.routinePreviewTemplateId ?: state.recommendedTemplateId ?: return
        if (templateId == state.activeRoutineProgress?.templateId) {
            showRoutineLibraryDialog.value = false
            showRoutineRecommendationsDialog.value = false
            showRoutineSettingsDialog.value = false
            routinePreviewTemplateId.value = null
            return
        }
        routineSwitchConfirmTemplateId.value = templateId
    }

    fun showCreateCustomRoutine() {
        showRoutineLibraryDialog.value = false
        routineSwitchConfirmTemplateId.value = null
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
        showCustomRoutineProgressConfirmDialog.value = false
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
        routineSwitchConfirmTemplateId.value = null
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
        showCustomRoutineProgressConfirmDialog.value = false
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
        routineSwitchConfirmTemplateId.value = null
        completionConfirm.value = null
        routineExercisePicker.value = null
        showCancelLatestRoutineDayDialog.value = false
        showCustomRoutineProgressConfirmDialog.value = false
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
        showCustomRoutineProgressConfirmDialog.value = false
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
        if (shouldConfirmCustomRoutineProgressPolicy(state, builder, startAfterSave)) {
            showCustomRoutineProgressConfirmDialog.value = true
            return
        }
        commitCustomRoutine(
            startAfterSave = startAfterSave,
            resetCurrentCycle = false
        )
    }

    fun keepCustomRoutineProgressAfterEdit() {
        showCustomRoutineProgressConfirmDialog.value = false
        commitCustomRoutine(
            startAfterSave = false,
            resetCurrentCycle = false
        )
    }

    fun resetCustomRoutineProgressAfterEdit() {
        showCustomRoutineProgressConfirmDialog.value = false
        commitCustomRoutine(
            startAfterSave = false,
            resetCurrentCycle = true
        )
    }

    fun dismissCustomRoutineProgressConfirmation() {
        showCustomRoutineProgressConfirmDialog.value = false
    }

    private fun commitCustomRoutine(
        startAfterSave: Boolean,
        resetCurrentCycle: Boolean
    ) {
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
                val switchResult = if (startAfterSave || resetCurrentCycle) {
                    switchRoutineTemplate(template.id)
                } else {
                    Result.success(Unit)
                }
                if (switchResult.isSuccess) {
                    customRoutineBuilder.value = CustomRoutineBuilderState()
                } else {
                    customRoutineBuilder.update { it.copy(error = CustomRoutineFormError.SAVE_FAILED) }
                }
            }.onFailure {
                customRoutineBuilder.update { it.copy(error = CustomRoutineFormError.SAVE_FAILED) }
            }
        }
    }

    private fun shouldConfirmCustomRoutineProgressPolicy(
        state: RoutineUiState,
        builder: CustomRoutineBuilderState,
        startAfterSave: Boolean
    ): Boolean {
        val editingId = builder.editingRoutineId ?: return false
        val progress = state.activeRoutineProgress ?: return false
        return !startAfterSave &&
            editingId == progress.templateId &&
            state.hasCurrentCycleProgress()
    }

    fun updateRoutineCycleLength(cycleLength: Int) {
        updateRecommendationForm { it.copy(cycleLength = cycleLength) }
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
        if (completeRoutineDayInFlight) return
        val state = uiState.value
        val routineDay = state.nextRoutineDayUi ?: return
        if (routineDay.routineDayDate == null) {
            openRoutineDayDatePicker(
                reason = RoutineDayDatePickerReason.COMPLETE_DAY,
                pendingAction = PendingRoutineDayDateAction.CompleteDay(
                    skippedPlannedExerciseIds = skippedPlannedExerciseIds,
                    justRecordedPlannedExerciseIds = justRecordedPlannedExerciseIds
                )
            )
            return
        }
        continueCompleteCurrentRoutineDay(
            state = state,
            routineDay = routineDay,
            skippedPlannedExerciseIds = skippedPlannedExerciseIds,
            justRecordedPlannedExerciseIds = justRecordedPlannedExerciseIds
        )
    }

    private fun continueCompleteCurrentRoutineDay(
        state: RoutineUiState,
        routineDay: NextRoutineDayUiModel,
        skippedPlannedExerciseIds: Set<PlannedExerciseId>,
        justRecordedPlannedExerciseIds: Set<PlannedExerciseId>,
        completedAtOverride: Instant? = null
    ) {
        val effectiveCompletedIds = state.completedPlannedExerciseIds + justRecordedPlannedExerciseIds
        val unrecordedExercises = routineDay.previewExercises.filter { exercise ->
            exercise.id !in effectiveCompletedIds || exercise.id in skippedPlannedExerciseIds
        }
        completionConfirm.value = RoutineCompletionConfirmState(
            reason = routineDay.completionConfirmReason(
                skippedPlannedExerciseIds = skippedPlannedExerciseIds,
                unrecordedExercises = unrecordedExercises
            ),
            skippedExerciseIds = skippedPlannedExerciseIds,
            unrecordedExercises = unrecordedExercises
        )
    }

    fun confirmCompleteCurrentRoutineDay(onCompleted: () -> Unit = {}) {
        completionConfirm.value = null
        completeCurrentRoutineDay(onCompleted)
    }

    fun dismissCompleteRoutineDayConfirmation() {
        completionConfirm.value = null
    }

    fun requestStartWorkout(
        plannedExercise: PlannedExercise? = null,
        onStarted: (PlannedExercise) -> Unit
    ) {
        val state = uiState.value
        val routineDay = state.nextRoutineDayUi ?: return
        val startExercise = plannedExercise?.currentRoutineDayExercise(state)
            ?: routineDay.startExercise
            ?: return
        if (routineDay.routineDayDate == null) {
            openRoutineDayDatePicker(
                reason = RoutineDayDatePickerReason.START_WORKOUT,
                pendingAction = PendingRoutineDayDateAction.StartWorkout(startExercise)
            )
            return
        }
        onStarted(startExercise)
    }

    fun requestRecordSelected(
        plannedExercise: PlannedExercise,
        onSelected: (PlannedExercise) -> Unit
    ) {
        val state = uiState.value
        val recordableExercise = plannedExercise.currentRoutineDayExercise(state) ?: return
        if (recordableExercise.id in state.completedPlannedExerciseIds) return
        if (recordableExercise.routineDayDate == null) {
            openRoutineDayDatePicker(
                reason = RoutineDayDatePickerReason.START_WORKOUT,
                pendingAction = PendingRoutineDayDateAction.RecordExercise(recordableExercise)
            )
            return
        }
        onSelected(recordableExercise)
    }

    fun requestEditRoutineDayDate() {
        openRoutineDayDatePicker(
            reason = RoutineDayDatePickerReason.EDIT,
            pendingAction = null
        )
    }

    fun dismissRoutineDayDatePicker() {
        pendingRoutineDayDateAction = null
        routineDayDatePicker.value = null
    }

    fun selectRoutineDayDate(
        date: LocalDate,
        onWorkoutStarted: (PlannedExercise) -> Unit,
        onRecordSelected: (PlannedExercise) -> Unit
    ) {
        val state = uiState.value
        val routineDay = state.nextRoutineDayUi ?: return
        val routineDayInstanceId = routineDay.routineDayInstanceId ?: return
        val validationError = validateRoutineDayDate(date, routineDay, state.today)
        if (validationError != null) {
            routineDayDateError.value = validationError
            return
        }
        viewModelScope.launch {
            val result = setRoutineDayDate(
                routineDayInstanceId = routineDayInstanceId,
                assignedDate = date,
                cycleStartedAt = if (routineDay.dayNumber == 1) {
                    date.atStartOfDay(clock.zone).toInstant()
                } else {
                    null
                }
            )
            if (result.isFailure) {
                routineDayDateError.value = RoutineDayDateError.SAVE_FAILED
                return@launch
            }
            val updatedState = withTimeoutOrNull(2_000) {
                uiState.first { updated ->
                    updated.nextRoutineDayUi?.routineDayInstanceId == routineDayInstanceId &&
                        updated.nextRoutineDayUi?.routineDayDate == date
                }
            } ?: uiState.value
            val pendingAction = pendingRoutineDayDateAction
            pendingRoutineDayDateAction = null
            routineDayDatePicker.value = null
            routineDayDateError.value = null
            when (pendingAction) {
                is PendingRoutineDayDateAction.StartWorkout -> {
                    onWorkoutStarted(
                        pendingAction.plannedExercise.withRoutineDayDate(
                            date = date,
                            routineDayInstanceId = routineDayInstanceId
                        ).currentRoutineDayExercise(updatedState)
                            ?: pendingAction.plannedExercise.withRoutineDayDate(
                                date = date,
                                routineDayInstanceId = routineDayInstanceId
                            )
                    )
                }
                is PendingRoutineDayDateAction.RecordExercise -> {
                    onRecordSelected(
                        pendingAction.plannedExercise.withRoutineDayDate(
                            date = date,
                            routineDayInstanceId = routineDayInstanceId
                        ).currentRoutineDayExercise(updatedState)
                            ?: pendingAction.plannedExercise.withRoutineDayDate(
                                date = date,
                                routineDayInstanceId = routineDayInstanceId
                            )
                    )
                }
                is PendingRoutineDayDateAction.CompleteDay -> {
                    val updatedRoutineDay = updatedState.nextRoutineDayUi
                        ?.takeIf { updatedRoutineDay ->
                            updatedRoutineDay.routineDayInstanceId == routineDayInstanceId &&
                                updatedRoutineDay.routineDayDate == date
                        }
                        ?: routineDay.copy(routineDayDate = date)
                    continueCompleteCurrentRoutineDay(
                        state = updatedState,
                        routineDay = updatedRoutineDay,
                        skippedPlannedExerciseIds = pendingAction.skippedPlannedExerciseIds,
                        justRecordedPlannedExerciseIds = pendingAction.justRecordedPlannedExerciseIds,
                        completedAtOverride = routineDayCompletedAt(date)
                    )
                }
                null -> Unit
            }
        }
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
                    note = "",
                    routineDayInstanceId = routineDay.previewExercises
                        .firstOrNull()
                        ?.routineDayInstanceId,
                    routineDayDate = routineDay.routineDayDate
                )
            }
        }
        routineExercisePicker.value = null
        return RoutineExercisePickResult(
            mode = picker.mode,
            plannedExercise = selected
        )
    }

    fun completeCurrentRoutineDay(
        onCompleted: () -> Unit = {},
        completedAtOverride: Instant? = null
    ) {
        if (completeRoutineDayInFlight) return
        val state = uiState.value
        val template = activeTemplate(state)
            ?: return
        val dayIndex = state.activeRoutineProgress?.dayIndex
            ?.coerceIn(0, (template.cycleLength - 1).coerceAtLeast(0))
            ?: 0
        val completedAt = completedAtOverride ?: state.nextRoutineDayUi
            ?.routineDayDate
            ?.let(::routineDayCompletedAt)
            ?: clock.instant()
        completeRoutineDayInFlight = true
        viewModelScope.launch {
            try {
                val result = completeRoutineDay(
                    template = template,
                    completedDayIndex = dayIndex,
                    completedAt = completedAt
                )
                completeDayFailed.value = result.isFailure
                if (result.isSuccess) {
                    onCompleted()
                }
            } finally {
                completeRoutineDayInFlight = false
            }
        }
    }

    private fun routineDayCompletedAt(date: LocalDate): Instant =
        date.atTime(LocalTime.NOON).atZone(clock.zone).toInstant()

    private fun NextRoutineDayUiModel.completesCurrentCycle(): Boolean =
        dayNumber == routineTemplate?.cycleLength

    private fun NextRoutineDayUiModel.completionConfirmReason(
        skippedPlannedExerciseIds: Set<PlannedExerciseId>,
        unrecordedExercises: List<PlannedExercise>
    ): RoutineCompletionConfirmReason =
        when {
            completesCurrentCycle() -> RoutineCompletionConfirmReason.CYCLE_COMPLETE
            skippedPlannedExerciseIds.isEmpty() ->
                RoutineCompletionConfirmReason.MANUAL
            else ->
                RoutineCompletionConfirmReason.SESSION_ENDED_WITH_SKIPS
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

    private fun RoutineUiState.hasCurrentCycleProgress(): Boolean {
        val progress = activeRoutineProgress ?: return false
        val hasAssignedCurrentCycleDate = plan?.days.orEmpty().any { day ->
            progress.routineDayDates.containsKey(
                routineDayInstanceId(
                    templateId = progress.templateId,
                    cycleNumber = progress.cycleNumber,
                    dayNumber = day.dayNumber
                )
            )
        }
        return progress.dayIndex > 0 ||
            progress.lastCompletedDayIndex != null ||
            hasAssignedCurrentCycleDate ||
            logs.isNotEmpty()
    }

    private fun openRoutineDayDatePicker(
        reason: RoutineDayDatePickerReason,
        pendingAction: PendingRoutineDayDateAction?
    ) {
        val state = uiState.value
        val routineDay = state.nextRoutineDayUi ?: return
        val minimumDate = routineDay.previousRoutineDayDate
        val maximumDate = state.today
        val earliestSelectableDate = minimumDate?.plusDays(1) ?: LocalDate.MIN
        if (earliestSelectableDate.isAfter(maximumDate)) {
            pendingRoutineDayDateAction = null
            routineDayDatePicker.value = null
            routineDayDateError.value = RoutineDayDateError.BEFORE_PREVIOUS_DAY
            return
        }
        pendingRoutineDayDateAction = pendingAction
        routineDayDateError.value = null
        routineDayDatePicker.value = RoutineDayDatePickerState(
            reason = reason,
            initialDate = routineDay.routineDayDate
                ?: maximumDate.coerceAtLeast(earliestSelectableDate),
            minDateExclusive = minimumDate,
            maxDateInclusive = maximumDate
        )
    }

    private fun validateRoutineDayDate(
        date: LocalDate,
        routineDay: NextRoutineDayUiModel,
        today: LocalDate
    ): RoutineDayDateError? = when {
        date.isAfter(today) -> RoutineDayDateError.FUTURE
        routineDay.previousRoutineDayDate != null &&
            !date.isAfter(routineDay.previousRoutineDayDate) -> RoutineDayDateError.BEFORE_PREVIOUS_DAY
        else -> null
    }

    private fun PlannedExercise.withRoutineDayDate(
        date: LocalDate,
        routineDayInstanceId: String
    ): PlannedExercise = copy(
        routineDayInstanceId = this.routineDayInstanceId ?: routineDayInstanceId,
        routineDayDate = date
    )

    private fun PlannedExercise.currentRoutineDayExercise(state: RoutineUiState): PlannedExercise? {
        val currentDay = state.nextRoutineDayUi ?: return null
        currentDay.previewExercises.firstOrNull { it.id == id }?.let { current ->
            return if (current.exercise.id == exercise.id) {
                current
            } else {
                copy(
                    routineDayInstanceId = routineDayInstanceId
                        ?: current.routineDayInstanceId
                        ?: currentDay.routineDayInstanceId,
                    routineDayDate = routineDayDate
                        ?: current.routineDayDate
                        ?: currentDay.routineDayDate
                )
            }
        }
        if (routineDayInstanceId != currentDay.routineDayInstanceId) return null
        if (!id.isRoutineAdditionalExerciseId() && id.value.endsWith("_${exercise.id.value}")) {
            currentDay.previewExercises.firstOrNull { it.exercise.id == exercise.id }?.let { return it }
        }
        return copy(routineDayDate = routineDayDate ?: currentDay.routineDayDate)
    }

    private data class RoutineDataState(
        val templates: List<PlanTemplate>,
        val currentCycle: CurrentRoutineCycle,
        val latestLogs: List<WorkoutLog>,
        val exercises: List<com.smarttrainner.core.model.Exercise>,
        val profileExperience: TrainingExperience
    )

    private data class RoutineFormControlState(
        val recommendationForm: RoutineRecommendationFormState,
        val customRoutineBuilder: CustomRoutineBuilderState,
        val completeDayFailed: Boolean,
        val routineDayDateError: RoutineDayDateError?,
        val routineDialogState: RoutineDialogState,
        val routineSwitchConfirmTemplateId: String? = null
    )

    private data class RoutineModalState(
        val completionConfirm: RoutineCompletionConfirmState?,
        val datePicker: RoutineDayDatePickerState?,
        val exercisePicker: RoutineExercisePickerState?,
        val showCancelLatest: Boolean,
        val showCustomProgressConfirm: Boolean
    )

    private data class RoutineDialogState(
        val previewTemplateId: String?,
        val showLibrary: Boolean,
        val showSettings: Boolean,
        val showRecommendations: Boolean,
        val completionConfirm: RoutineCompletionConfirmState?,
        val datePicker: RoutineDayDatePickerState?,
        val exercisePicker: RoutineExercisePickerState?,
        val showCancelLatest: Boolean,
        val showCustomProgressConfirm: Boolean
    )

    private sealed interface PendingRoutineDayDateAction {
        data class StartWorkout(val plannedExercise: PlannedExercise) : PendingRoutineDayDateAction
        data class RecordExercise(val plannedExercise: PlannedExercise) : PendingRoutineDayDateAction
        data class CompleteDay(
            val skippedPlannedExerciseIds: Set<PlannedExerciseId>,
            val justRecordedPlannedExerciseIds: Set<PlannedExerciseId>
        ) : PendingRoutineDayDateAction
    }
}
