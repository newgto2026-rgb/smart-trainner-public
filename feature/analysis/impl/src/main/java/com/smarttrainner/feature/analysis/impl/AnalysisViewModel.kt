package com.smarttrainner.feature.analysis.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveAllWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveRoutineProgressUseCase
import com.smarttrainner.feature.analysis.domain.ObserveCycleSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModel @Inject constructor(
    observeAllWorkoutLogs: ObserveAllWorkoutLogsUseCase,
    observeCycleSummary: ObserveCycleSummaryUseCase,
    observeExercises: ObserveExercisesUseCase,
    observeRoutineProgress: ObserveRoutineProgressUseCase,
    clock: Clock
) : ViewModel() {
    internal val uiState = observeRoutineProgress()
        .flatMapLatest { progress ->
            combine(
                observeAllWorkoutLogs(),
                observeCycleSummary(progress, clock.zone),
                observeExercises()
            ) { logs, summary, exercises ->
                val exercisesById = exercises.associateBy { it.id }
                AnalysisUiState(
                    recentLogs = logs
                        .sortedByDescending { it.performedAt }
                        .map { log ->
                            RecentWorkoutLogUiModel(
                                log = log,
                                exercise = exercisesById[log.exerciseId]
                            )
                        },
                    summary = summary,
                    cycleNumber = progress.cycleNumber
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnalysisUiState()
        )
}
