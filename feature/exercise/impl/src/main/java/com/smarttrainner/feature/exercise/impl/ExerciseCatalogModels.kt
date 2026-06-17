package com.smarttrainner.feature.exercise.impl

import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.WorkoutLog

internal data class ExerciseCatalogUiState(
    val exercises: List<Exercise> = emptyList(),
    val latestWorkoutLogs: List<WorkoutLog> = emptyList(),
    val searchQuery: String = "",
    val selectedExerciseId: ExerciseId? = null,
    val customExerciseForm: CustomExerciseFormUiState = CustomExerciseFormUiState()
)

internal data class CustomExerciseFormUiState(
    val visible: Boolean = false,
    val name: String = "",
    val muscleGroup: MuscleGroup = MuscleGroup.LOWER_BODY,
    val equipment: EquipmentType = EquipmentType.BODYWEIGHT,
    val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    val imageUri: String = "",
    val summary: String = "",
    val instructions: List<String> = listOf(""),
    val safetyCues: List<String> = listOf(""),
    val defaultSets: String = "3",
    val repRangeStart: String = "8",
    val repRangeEnd: String = "12",
    val defaultDurationMinutes: String = "",
    val restSeconds: String = "90",
    val saving: Boolean = false,
    val error: CustomExerciseFormError? = null
)

internal enum class CustomExerciseFormError {
    NAME,
    INSTRUCTIONS,
    SAFETY,
    SETS,
    TARGET,
    REPS,
    DURATION,
    REST,
    SAVE
}
