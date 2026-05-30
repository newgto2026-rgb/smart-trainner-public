package com.smarttrainner.feature.training.impl

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
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog

enum class TrainingTab {
    HOME,
    PLAN,
    EXERCISES,
    ANALYSIS
}

enum class RecordFormError {
    SELECT_EXERCISE,
    SETS,
    REPS,
    WEIGHT,
    DURATION,
    REST,
    SAVE_FAILED,
    COMPLETE_DAY_FAILED
}

enum class CustomRoutineFormError {
    NAME,
    DAYS,
    EMPTY_DAY,
    EXERCISE,
    SAVE_FAILED
}

data class RecordFormState(
    val setEntries: List<RecordSetFormState> = emptyList(),
    val memo: String = ""
)

data class RecordSetFormState(
    val reps: String = "",
    val weightKg: String = "",
    val durationMinutes: String = "",
    val restSeconds: String = ""
)

data class RoutineRecommendationFormState(
    val daysPerWeek: Int = 4,
    val sessionMinutes: Int = 60,
    val experience: TrainingExperience = TrainingExperience.INTERMEDIATE,
    val feeling: RoutineFeeling = RoutineFeeling.FOCUSED_BODY_PART
)

data class CustomRoutineBuilderState(
    val visible: Boolean = false,
    val editingRoutineId: String? = null,
    val name: String = "",
    val selectedDayIndex: Int = 0,
    val days: List<CustomRoutineDayFormState> = emptyList(),
    val expandedExerciseGroups: Set<MuscleGroup> = emptySet(),
    val error: CustomRoutineFormError? = null,
    val savedTemplateId: String? = null
)

data class CustomRoutineDayFormState(
    val title: String,
    val focus: RoutineFocus?,
    val exercises: List<CustomRoutineExerciseFormState>
)

data class CustomRoutineExerciseFormState(
    val exercise: Exercise,
    val sets: Int,
    val repRangeStart: Int?,
    val repRangeEnd: Int?,
    val durationMinutes: Int?,
    val restSeconds: Int,
    val note: String = ""
)

data class NextRoutineDayUiModel(
    val day: WorkoutDayPlan,
    val routineTemplate: PlanTemplate?,
    val primaryFocus: RoutineFocus?,
    val secondaryFocuses: List<RoutineFocus>,
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

data class TrainingUiState(
    val selectedTab: TrainingTab = TrainingTab.HOME,
    val templates: List<PlanTemplate> = emptyList(),
    val selectedTemplateId: String = "",
    val plan: WeeklyPlan? = null,
    val activeRoutineProgress: RoutineProgress? = null,
    val nextRoutineDay: WorkoutDayPlan? = null,
    val nextRoutineDayUi: NextRoutineDayUiModel? = null,
    val routineRecommendationInput: RoutineRecommendationFormState = RoutineRecommendationFormState(),
    val recommendedTemplateId: String? = null,
    val alternativeTemplateIds: List<String> = emptyList(),
    val routinePreviewTemplateId: String? = null,
    val showRoutineLibraryDialog: Boolean = false,
    val showRoutineSettingsDialog: Boolean = false,
    val showRoutineRecommendationsDialog: Boolean = false,
    val customRoutineBuilder: CustomRoutineBuilderState = CustomRoutineBuilderState(),
    val exercises: List<Exercise> = emptyList(),
    val logs: List<WorkoutLog> = emptyList(),
    val latestWorkoutLogs: List<WorkoutLog> = emptyList(),
    val completedPlannedExerciseIds: Set<PlannedExerciseId> = emptySet(),
    val summary: WeeklySummary? = null,
    val selectedExercise: Exercise? = null,
    val selectedPlannedExercise: PlannedExercise? = null,
    val recordingPlannedExercise: PlannedExercise? = null,
    val recordForm: RecordFormState = RecordFormState(),
    val formError: RecordFormError? = null,
    val recordSaved: Boolean = false
) {
    val selectedExerciseId: ExerciseId?
        get() = selectedExercise?.id

    val customTemplates: List<PlanTemplate>
        get() = templates.filter { it.source == RoutineSource.CUSTOM }

    val systemTemplates: List<PlanTemplate>
        get() = templates.filter { it.source == RoutineSource.SYSTEM }
}

internal fun allowedCustomRoutineMuscleGroups(focus: RoutineFocus?): Set<MuscleGroup> = when (focus) {
    null,
    RoutineFocus.FULL_BODY -> MuscleGroup.entries.toSet()
    RoutineFocus.UPPER_BODY -> setOf(
        MuscleGroup.BACK,
        MuscleGroup.CHEST,
        MuscleGroup.SHOULDERS,
        MuscleGroup.ARMS,
        MuscleGroup.BICEPS,
        MuscleGroup.TRICEPS,
        MuscleGroup.FOREARMS
    )
    RoutineFocus.PUSH -> setOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)
    RoutineFocus.PULL -> setOf(MuscleGroup.BACK, MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
    RoutineFocus.CHEST -> setOf(MuscleGroup.CHEST)
    RoutineFocus.BACK -> setOf(MuscleGroup.BACK)
    RoutineFocus.LOWER_BODY -> setOf(MuscleGroup.LOWER_BODY)
    RoutineFocus.SHOULDERS -> setOf(MuscleGroup.SHOULDERS)
    RoutineFocus.ARMS -> setOf(
        MuscleGroup.ARMS,
        MuscleGroup.BICEPS,
        MuscleGroup.TRICEPS,
        MuscleGroup.FOREARMS
    )
    RoutineFocus.BICEPS -> setOf(MuscleGroup.BICEPS)
    RoutineFocus.TRICEPS -> setOf(MuscleGroup.TRICEPS)
    RoutineFocus.FOREARMS -> setOf(MuscleGroup.FOREARMS)
    RoutineFocus.CARDIO_CONDITIONING -> setOf(MuscleGroup.CARDIO)
    RoutineFocus.CORE -> setOf(MuscleGroup.CORE)
}
