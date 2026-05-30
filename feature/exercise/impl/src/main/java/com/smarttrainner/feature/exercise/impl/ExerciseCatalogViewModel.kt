package com.smarttrainner.feature.exercise.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.feature.exercise.api.ExerciseCatalogUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExerciseCatalogViewModel @Inject constructor(
    observeExercises: ObserveExercisesUseCase,
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase
) : ViewModel() {
    private val selectedExerciseId = MutableStateFlow<ExerciseId?>(null)

    val uiState = combine(
        observeExercises(),
        observeLatestWorkoutLogs(),
        selectedExerciseId
    ) { exercises, latestLogs, selectedExerciseId ->
        ExerciseCatalogUiState(
            exercises = exercises,
            latestWorkoutLogs = latestLogs,
            selectedExerciseId = selectedExerciseId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExerciseCatalogUiState()
    )

    fun updateSelection(exerciseId: ExerciseId?) {
        selectedExerciseId.value = exerciseId
    }
}
