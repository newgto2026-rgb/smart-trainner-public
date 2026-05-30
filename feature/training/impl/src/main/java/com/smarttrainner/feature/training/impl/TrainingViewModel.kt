package com.smarttrainner.feature.training.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.CompleteRoutineDayUseCase
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
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutLogInput
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TrainingViewModel @Inject constructor(
    observeExercises: ObserveExercisesUseCase,
    observePlanTemplates: ObservePlanTemplatesUseCase,
    observeCurrentWeeklyPlan: ObserveCurrentWeeklyPlanUseCase,
    observeRoutineProgress: ObserveRoutineProgressUseCase,
    observeWorkoutLogs: ObserveWorkoutLogsUseCase,
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase,
    observeWeeklySummary: ObserveWeeklySummaryUseCase,
    private val getLatestWorkoutLog: GetLatestWorkoutLogUseCase,
    private val recommendRoutine: RecommendRoutineUseCase,
    private val resolveRoutineCycleCompletion: ResolveRoutineCycleCompletionUseCase,
    private val startRoutine: StartRoutineUseCase,
    private val completeRoutineDay: CompleteRoutineDayUseCase,
    private val saveCustomRoutineUseCase: SaveCustomRoutineUseCase,
    private val saveWorkoutLog: SaveWorkoutLogUseCase,
    private val clock: Clock
) : ViewModel() {
    private val weekStart = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val selectedTab = MutableStateFlow(TrainingTab.HOME)
    private val selectedExerciseId = MutableStateFlow<ExerciseId?>(null)
    private val selectedPlannedExerciseId = MutableStateFlow<PlannedExerciseId?>(null)
    private val recordForm = MutableStateFlow(RecordFormState())
    private val recordingMode = MutableStateFlow(RecordingMode.SINGLE)
    private val recommendationForm = MutableStateFlow(RoutineRecommendationFormState())
    private val routinePreviewTemplateId = MutableStateFlow<String?>(null)
    private val showRoutineLibraryDialog = MutableStateFlow(false)
    private val showRoutineSettingsDialog = MutableStateFlow(false)
    private val showRoutineRecommendationsDialog = MutableStateFlow(false)
    private val customRoutineBuilder = MutableStateFlow(CustomRoutineBuilderState())
    private val formError = MutableStateFlow<RecordFormError?>(null)
    private val recordSaved = MutableStateFlow(false)
    private var recordPrefillToken = 0L

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
        recordForm,
        recommendationForm,
        customRoutineBuilder,
        formError,
        routineDialogState
    ) { form, recommendation, builder, error, routineDialog ->
        TrainingFormControlState(
            recordForm = form,
            recommendationForm = recommendation,
            customRoutineBuilder = builder,
            formError = error,
            routineDialogState = routineDialog
        )
    }

    private val controlState = combine(
        selectedTab,
        selectedExerciseId,
        selectedPlannedExerciseId,
        formControlState
    ) { tab, exerciseId, plannedExerciseId, formControl ->
        TrainingControlState(
            selectedTab = tab,
            selectedExerciseId = exerciseId,
            selectedPlannedExerciseId = plannedExerciseId,
            recordForm = formControl.recordForm,
            recommendationForm = formControl.recommendationForm,
            customRoutineBuilder = formControl.customRoutineBuilder,
            formError = formControl.formError,
            routineDialogState = formControl.routineDialogState
        )
    }

    private val saveState = combine(
        controlState,
        recordSaved
    ) { control, saved ->
        control.copy(recordSaved = saved)
    }

    private val routinePlanState = combine(
        observeCurrentWeeklyPlan(weekStart),
        observeRoutineProgress()
    ) { plan, progress ->
        RoutinePlanState(plan = plan, routineProgress = progress)
    }

    private val logState = combine(
        observeWorkoutLogs(weekStart),
        observeLatestWorkoutLogs()
    ) { weeklyLogs, latestLogs ->
        TrainingLogState(
            weeklyLogs = weeklyLogs,
            latestLogs = latestLogs
        )
    }

    private val dataState = combine(
        observePlanTemplates(),
        routinePlanState,
        logState,
        observeWeeklySummary(weekStart),
        observeExercises()
    ) { templates, routinePlan, logState, summary, exercises ->
        TrainingDataState(
            templates = templates,
            plan = routinePlan.plan,
            routineProgress = routinePlan.routineProgress,
            logs = logState.weeklyLogs,
            latestLogs = logState.latestLogs,
            summary = summary,
            exercises = exercises
        )
    }

    val uiState = combine(
        saveState,
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
        val selectedExercise = data.exercises.firstOrNull { it.id == control.selectedExerciseId }
        val recordingPlanned = data.plan.findPlannedExercise(control.selectedPlannedExerciseId)
        val selectedPlanned = recordingPlanned
            ?: nextDayUi?.startExercise
            ?: data.plan.firstIncomplete(completedIds)
        TrainingUiState(
            selectedTab = control.selectedTab,
            templates = data.templates,
            selectedTemplateId = data.plan.templateId,
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
            summary = data.summary,
            selectedExercise = selectedExercise,
            selectedPlannedExercise = selectedPlanned,
            recordingPlannedExercise = recordingPlanned,
            recordForm = control.recordForm,
            formError = control.formError,
            recordSaved = control.recordSaved
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingUiState()
    )

    fun selectTab(tab: TrainingTab) {
        selectedTab.value = tab
        selectedExerciseId.value = null
        formError.value = null
        recordSaved.value = false
    }

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
            formError.value = if (result.isSuccess) null else RecordFormError.COMPLETE_DAY_FAILED
            if (result.isSuccess) {
                selectedPlannedExerciseId.value = null
                recordSaved.value = false
            }
        }
    }

    fun selectExercise(exerciseId: ExerciseId) {
        selectedExerciseId.value = exerciseId
        selectedTab.value = TrainingTab.EXERCISES
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
        prefillRecordForm(exercise)
        formError.value = null
        recordSaved.value = false
    }

    fun startWorkout(exercise: PlannedExercise) {
        recordingMode.value = RecordingMode.ROUTINE
        selectedPlannedExerciseId.value = exercise.id
        selectedExerciseId.value = null
        prefillRecordForm(exercise)
        formError.value = null
        recordSaved.value = false
    }

    fun dismissRecordDialog() {
        recordPrefillToken += 1
        selectedPlannedExerciseId.value = null
        recordingMode.value = RecordingMode.SINGLE
        formError.value = null
        recordSaved.value = false
    }

    fun updateSetReps(index: Int, value: String) {
        updateSetEntry(index) { it.copy(reps = value.onlyNumber()) }
    }

    fun updateSetWeight(index: Int, value: String) {
        updateSetEntry(index) { it.copy(weightKg = value.onlyDecimal()) }
    }

    fun updateSetDuration(index: Int, value: String) {
        updateSetEntry(index) { it.copy(durationMinutes = value.onlyNumber()) }
    }

    fun updateSetRest(index: Int, value: String) {
        updateSetEntry(index) { it.copy(restSeconds = value.onlyNumber()) }
    }

    fun addSetEntry() {
        val planned = uiState.value.recordingPlannedExercise ?: return
        recordForm.update { form ->
            if (form.setEntries.size >= MAX_RECORD_SETS) return@update form
            val last = form.setEntries.lastOrNull()
            form.copy(
                setEntries = form.setEntries + (last ?: planned.defaultSetForm())
            )
        }
        formError.value = null
    }

    fun removeSetEntry(index: Int) {
        recordForm.update { form ->
            if (form.setEntries.size <= 1 || index !in form.setEntries.indices) {
                form
            } else {
                form.copy(setEntries = form.setEntries.filterIndexed { entryIndex, _ -> entryIndex != index })
            }
        }
        formError.value = null
    }

    fun updateMemo(value: String) = recordForm.update { it.copy(memo = value.take(120)) }

    fun saveRecord() {
        val state = uiState.value
        val planned = state.plan?.findPlannedExercise(selectedPlannedExerciseId.value)
        if (planned == null) {
            formError.value = RecordFormError.SELECT_EXERCISE
            return
        }
        val form = recordForm.value
        val validationError = validateSetEntries(planned, form.setEntries)
        if (validationError != null) {
            formError.value = validationError
            return
        }
        val setEntries = form.setEntries.toWorkoutSetLogs(planned)
        val firstReps = setEntries.firstOrNull { it.reps != null }?.reps
        val firstWeight = setEntries.firstOrNull { it.weightKg != null }?.weightKg
        val totalDuration = setEntries.sumOf { it.durationMinutes ?: 0 }.takeIf { it > 0 }
        val continueRoutine = recordingMode.value == RecordingMode.ROUTINE

        viewModelScope.launch {
            val result = saveWorkoutLog(
                WorkoutLogInput(
                    plannedExerciseId = planned.id,
                    exerciseId = planned.exercise.id,
                    performedAt = LocalDateTime.now(clock),
                    sets = setEntries.size,
                    reps = firstReps,
                    weightKg = firstWeight,
                    durationMinutes = totalDuration,
                    memo = form.memo,
                    completed = true,
                    setEntries = setEntries
                )
            )
            if (result.isSuccess) {
                formError.value = null
                val latestState = uiState.value
                val nextPlanned = if (continueRoutine) {
                    latestState.plan?.nextIncompleteInSameDay(
                        currentId = planned.id,
                        completedIds = latestState.completedPlannedExerciseIds + planned.id
                    )
                } else {
                    null
                }
                if (nextPlanned != null) {
                    selectedPlannedExerciseId.value = nextPlanned.id
                    prefillRecordForm(nextPlanned)
                    recordSaved.value = false
                } else {
                    recordPrefillToken += 1
                    selectedPlannedExerciseId.value = null
                    recordingMode.value = RecordingMode.SINGLE
                    recordForm.value = RecordFormState()
                    recordSaved.value = false
                }
            } else {
                formError.value = RecordFormError.SAVE_FAILED
                recordSaved.value = false
            }
        }
    }

    private fun prefillRecordForm(planned: PlannedExercise) {
        val currentState = uiState.value
        val previousLog = currentState.latestWorkoutLogs.latestRecordForExercise(planned.exercise.id)
            ?: currentState.logs.latestRecordForExercise(planned.exercise.id)
        val initialForm = RecordFormState(setEntries = planned.defaultSetForms(previousLog))
        val token = recordPrefillToken + 1
        recordPrefillToken = token
        recordForm.value = initialForm

        viewModelScope.launch {
            val latestLog = getLatestWorkoutLog(planned.exercise.id) ?: return@launch
            val latestForm = RecordFormState(setEntries = planned.defaultSetForms(latestLog))
            if (
                recordPrefillToken == token &&
                selectedPlannedExerciseId.value == planned.id &&
                recordForm.value == initialForm
            ) {
                recordForm.value = latestForm
            }
        }
    }

    private fun updateSetEntry(
        index: Int,
        update: (RecordSetFormState) -> RecordSetFormState
    ) {
        recordForm.update { form ->
            if (index !in form.setEntries.indices) {
                form
            } else {
                form.copy(
                    setEntries = form.setEntries.mapIndexed { entryIndex, entry ->
                        if (entryIndex == index) update(entry) else entry
                    }
                )
            }
        }
    }

    private data class TrainingDataState(
        val templates: List<PlanTemplate>,
        val plan: WeeklyPlan,
        val routineProgress: RoutineProgress,
        val logs: List<com.smarttrainner.core.model.WorkoutLog>,
        val latestLogs: List<com.smarttrainner.core.model.WorkoutLog>,
        val summary: com.smarttrainner.core.model.WeeklySummary,
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
        val recordForm: RecordFormState,
        val recommendationForm: RoutineRecommendationFormState,
        val customRoutineBuilder: CustomRoutineBuilderState,
        val formError: RecordFormError?,
        val routineDialogState: RoutineDialogState
    )

    private data class TrainingControlState(
        val selectedTab: TrainingTab,
        val selectedExerciseId: ExerciseId?,
        val selectedPlannedExerciseId: PlannedExerciseId?,
        val recordForm: RecordFormState,
        val recommendationForm: RoutineRecommendationFormState,
        val customRoutineBuilder: CustomRoutineBuilderState,
        val formError: RecordFormError?,
        val routineDialogState: RoutineDialogState,
        val recordSaved: Boolean = false
    )

    private data class RoutineDialogState(
        val previewTemplateId: String?,
        val showLibrary: Boolean,
        val showSettings: Boolean,
        val showRecommendations: Boolean
    )
}
