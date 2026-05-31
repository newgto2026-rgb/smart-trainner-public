package com.smarttrainner.feature.exercise.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExerciseCatalogViewModel @Inject constructor(
    observeExercises: ObserveExercisesUseCase,
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase
) : ViewModel() {
    internal val uiState = combine(
        observeExercises(),
        observeLatestWorkoutLogs()
    ) { exercises, latestLogs ->
        ExerciseCatalogUiState(
            exercises = exercises,
            latestWorkoutLogs = latestLogs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExerciseCatalogUiState()
    )
}
