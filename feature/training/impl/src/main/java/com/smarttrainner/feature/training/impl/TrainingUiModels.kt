package com.smarttrainner.feature.training.impl

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.PlannedExerciseId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutDayPlan
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.feature.exercise.api.ExerciseCatalogUiState
import com.smarttrainner.feature.routine.api.CustomRoutineBuilderState
import com.smarttrainner.feature.routine.api.NextRoutineDayUiModel
import com.smarttrainner.feature.routine.api.RoutineRecommendationFormState
import com.smarttrainner.feature.routine.api.RoutineUiState
import java.time.LocalDate

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

data class RecentWorkoutLogUiModel(
    val log: WorkoutLog,
    val exercise: Exercise?
)

data class TrainingUiState(
    val routine: RoutineUiState = RoutineUiState(),
    val exerciseCatalog: ExerciseCatalogUiState = ExerciseCatalogUiState(),
    val recentLogs: List<RecentWorkoutLogUiModel> = emptyList(),
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

    val templates: List<PlanTemplate>
        get() = routine.templates

    val selectedTemplateId: String
        get() = routine.selectedTemplateId

    val today: LocalDate
        get() = routine.today

    val plan: WeeklyPlan?
        get() = routine.plan

    val activeRoutineProgress: RoutineProgress?
        get() = routine.activeRoutineProgress

    val nextRoutineDay: WorkoutDayPlan?
        get() = routine.nextRoutineDay

    val nextRoutineDayUi: NextRoutineDayUiModel?
        get() = routine.nextRoutineDayUi

    val routineRecommendationInput: RoutineRecommendationFormState
        get() = routine.routineRecommendationInput

    val recommendedTemplateId: String?
        get() = routine.recommendedTemplateId

    val alternativeTemplateIds: List<String>
        get() = routine.alternativeTemplateIds

    val routinePreviewTemplateId: String?
        get() = routine.routinePreviewTemplateId

    val showRoutineLibraryDialog: Boolean
        get() = routine.showRoutineLibraryDialog

    val showRoutineSettingsDialog: Boolean
        get() = routine.showRoutineSettingsDialog

    val showRoutineRecommendationsDialog: Boolean
        get() = routine.showRoutineRecommendationsDialog

    val customRoutineBuilder: CustomRoutineBuilderState
        get() = routine.customRoutineBuilder

    val exercises: List<Exercise>
        get() = exerciseCatalog.exercises

    val logs: List<WorkoutLog>
        get() = routine.logs

    val latestWorkoutLogs: List<WorkoutLog>
        get() = exerciseCatalog.latestWorkoutLogs

    val completedPlannedExerciseIds: Set<PlannedExerciseId>
        get() = routine.completedPlannedExerciseIds

    val customTemplates: List<PlanTemplate>
        get() = routine.customTemplates

    val systemTemplates: List<PlanTemplate>
        get() = routine.systemTemplates
}
