package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.domain.ExercisePrescription
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import java.time.LocalDate

internal enum class CustomRoutineFormError {
    NAME,
    DAYS,
    EMPTY_DAY,
    EXERCISE,
    SAVE_FAILED
}

internal data class RoutineRecommendationFormState(
    val daysPerWeek: Int = 4,
    val sessionMinutes: Int = 60,
    val experience: TrainingExperience = TrainingExperience.INTERMEDIATE,
    val feeling: RoutineFeeling = RoutineFeeling.APP_RECOMMENDED
) {
    companion object {
        fun defaultFor(experience: TrainingExperience): RoutineRecommendationFormState = when (experience) {
            TrainingExperience.BEGINNER -> RoutineRecommendationFormState(
                daysPerWeek = 3,
                sessionMinutes = 45,
                experience = experience,
                feeling = RoutineFeeling.APP_RECOMMENDED
            )
            TrainingExperience.INTERMEDIATE -> RoutineRecommendationFormState(
                daysPerWeek = 4,
                sessionMinutes = 60,
                experience = experience,
                feeling = RoutineFeeling.APP_RECOMMENDED
            )
            TrainingExperience.ADVANCED -> RoutineRecommendationFormState(
                daysPerWeek = 5,
                sessionMinutes = 60,
                experience = experience,
                feeling = RoutineFeeling.FOCUSED_BODY_PART
            )
        }
    }
}

internal data class CustomRoutineBuilderState(
    val visible: Boolean = false,
    val editingRoutineId: String? = null,
    val name: String = "",
    val selectedDayIndex: Int = 0,
    val days: List<CustomRoutineDayFormState> = emptyList(),
    val expandedExerciseGroups: Set<MuscleGroup> = emptySet(),
    val error: CustomRoutineFormError? = null,
    val savedTemplateId: String? = null
)

internal data class CustomRoutineDayFormState(
    val title: String,
    val focus: RoutineFocus?,
    val exercises: List<CustomRoutineExerciseFormState>
)

internal data class CustomRoutineExerciseFormState(
    val exercise: Exercise,
    val sets: Int,
    val repRangeStart: Int?,
    val repRangeEnd: Int?,
    val durationMinutes: Int?,
    val restSeconds: Int,
    val note: String = ""
)

internal data class NextRoutineDayUiModel(
    val day: WorkoutDayPlan,
    val routineTemplate: PlanTemplate?,
    val primaryFocus: RoutineFocus?,
    val secondaryFocuses: List<RoutineFocus>,
    val cycleNumber: Int,
    val dayNumber: Int,
    val focus: String,
    val sessionMinutes: Int,
    val previewExercises: List<PlannedExercise>,
    val startExercise: PlannedExercise?,
    val nextPrimaryFocus: RoutineFocus?,
    val completedExerciseCount: Int,
    val totalExerciseCount: Int,
    val minRecoveryHours: Int
)

internal data class LatestRoutineDayCompletionUiModel(
    val day: WorkoutDayPlan,
    val routineTemplate: PlanTemplate?,
    val cycleNumber: Int,
    val dayNumber: Int,
    val completedAtText: String?
)

internal enum class RoutineCompletionConfirmReason {
    MANUAL,
    SESSION_ENDED_WITH_SKIPS
}

internal data class RoutineCompletionConfirmState(
    val reason: RoutineCompletionConfirmReason,
    val skippedExerciseIds: Set<PlannedExerciseId>,
    val unrecordedExercises: List<PlannedExercise>
)

internal enum class RoutineExercisePickerMode {
    SUBSTITUTE,
    ADD
}

internal data class RoutineExercisePickerState(
    val mode: RoutineExercisePickerMode,
    val anchorExercise: PlannedExercise?
)

internal data class RoutineExercisePickResult(
    val mode: RoutineExercisePickerMode,
    val plannedExercise: PlannedExercise
)

internal data class RoutineUiState(
    val templates: List<PlanTemplate> = emptyList(),
    val selectedTemplateId: String = "",
    val today: LocalDate = LocalDate.ofEpochDay(0),
    val plan: WeeklyPlan? = null,
    val activeRoutineProgress: RoutineProgress? = null,
    val nextRoutineDay: WorkoutDayPlan? = null,
    val nextRoutineDayUi: NextRoutineDayUiModel? = null,
    val latestRoutineDayCompletion: LatestRoutineDayCompletionUiModel? = null,
    val profileExperience: TrainingExperience = TrainingExperience.BEGINNER,
    val routineRecommendationInput: RoutineRecommendationFormState =
        RoutineRecommendationFormState.defaultFor(TrainingExperience.BEGINNER),
    val routineFilterAvailability: RoutineRecommendationFilterAvailability = RoutineRecommendationFilterAvailability(),
    val exercisePrescriptions: Map<ExerciseId, ExercisePrescription> = emptyMap(),
    val recommendedTemplateId: String? = null,
    val alternativeTemplateIds: List<String> = emptyList(),
    val routinePreviewTemplateId: String? = null,
    val showRoutineLibraryDialog: Boolean = false,
    val showRoutineSettingsDialog: Boolean = false,
    val showRoutineRecommendationsDialog: Boolean = false,
    val routineCompletionConfirm: RoutineCompletionConfirmState? = null,
    val routineExercisePicker: RoutineExercisePickerState? = null,
    val showCancelLatestRoutineDayDialog: Boolean = false,
    val customRoutineBuilder: CustomRoutineBuilderState = CustomRoutineBuilderState(),
    val exercises: List<Exercise> = emptyList(),
    val logs: List<WorkoutLog> = emptyList(),
    val latestWorkoutLogs: List<WorkoutLog> = emptyList(),
    val completedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet(),
    val completeDayError: Boolean = false
) {
    val customTemplates: List<PlanTemplate>
        get() = templates.filter { it.source == RoutineSource.CUSTOM }

    val systemTemplates: List<PlanTemplate>
        get() = templates.filter { it.source == RoutineSource.SYSTEM }
}

internal data class RoutineActions(
    val onTemplateSelected: (String) -> Unit = {},
    val onDaysPerWeekChanged: (Int) -> Unit = {},
    val onSessionMinutesChanged: (Int) -> Unit = {},
    val onExperienceChanged: (TrainingExperience) -> Unit = {},
    val onFeelingChanged: (RoutineFeeling) -> Unit = {},
    val onShowLibrary: () -> Unit = {},
    val onLibraryDismiss: () -> Unit = {},
    val onShowSettings: () -> Unit = {},
    val onSettingsDismiss: () -> Unit = {},
    val onShowRecommendations: () -> Unit = {},
    val onRecommendationsDismiss: () -> Unit = {},
    val onPreviewSelected: (String) -> Unit = {},
    val onStartPreviewRoutine: () -> Unit = {},
    val onCreateCustomRoutine: () -> Unit = {},
    val onCopyTemplateToCustom: (String) -> Unit = {},
    val onEditCustomRoutine: (String) -> Unit = {},
    val onCustomRoutineNameChanged: (String) -> Unit = {},
    val onCustomRoutineDaySelected: (Int) -> Unit = {},
    val onCustomRoutineDayFocusChanged: (RoutineFocus?) -> Unit = {},
    val onCustomRoutineDayAdded: () -> Unit = {},
    val onCustomRoutineDayRemoved: (Int) -> Unit = {},
    val onCustomRoutineExerciseGroupToggled: (MuscleGroup) -> Unit = {},
    val onCustomRoutineExerciseAdded: (ExerciseId) -> Unit = {},
    val onCustomRoutineExerciseRemoved: (Int) -> Unit = {},
    val onCustomRoutineExerciseMovedUp: (Int) -> Unit = {},
    val onCustomRoutineExerciseMovedDown: (Int) -> Unit = {},
    val onCustomRoutineSaved: (Boolean) -> Unit = {},
    val onCustomRoutineBuilderDismiss: () -> Unit = {},
    val onRequestCompleteRoutineDay: (Set<PlannedExerciseId>, Set<PlannedExerciseId>) -> Unit = { _, _ -> },
    val onConfirmCompleteRoutineDay: () -> Unit = {},
    val onDismissCompleteRoutineDay: () -> Unit = {},
    val onRequestCancelLatestRoutineDay: () -> Unit = {},
    val onConfirmCancelLatestRoutineDay: () -> Unit = {},
    val onDismissCancelLatestRoutineDay: () -> Unit = {},
    val onRequestSubstituteExercise: (PlannedExercise) -> Unit = {},
    val onRequestAdditionalExercise: (PlannedExercise?) -> Unit = {},
    val onRoutineExercisePicked: (ExerciseId) -> Unit = {},
    val onDismissRoutineExercisePicker: () -> Unit = {},
    val onWorkoutStarted: (PlannedExercise) -> Unit = {},
    val onSubstituteExerciseSelected: (PlannedExercise) -> Unit = {},
    val onAdditionalExerciseSelected: (PlannedExercise) -> Unit = {},
    val onCompleteRoutineDay: () -> Unit = {},
    val onExerciseMethodSelected: (ExerciseId) -> Unit = {},
    val onRecordSelected: (PlannedExercise) -> Unit = {}
)
