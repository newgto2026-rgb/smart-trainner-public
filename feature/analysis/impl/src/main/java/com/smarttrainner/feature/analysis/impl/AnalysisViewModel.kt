package com.smarttrainner.feature.analysis.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.ObserveWeeklySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModel @Inject constructor(
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase,
    observeWeeklySummary: ObserveWeeklySummaryUseCase,
    observeExercises: ObserveExercisesUseCase,
    clock: Clock
) : ViewModel() {
    internal val uiState = flow {
        emit(LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    }.flatMapLatest { weekStart ->
        combine(
            observeLatestWorkoutLogs(),
            observeWeeklySummary(weekStart),
            observeExercises()
        ) { latestLogs, summary, exercises ->
            val exercisesById = exercises.associateBy { it.id }
            AnalysisUiState(
                recentLogs = latestLogs
                    .sortedByDescending { it.performedAt }
                    .take(RECENT_WORKOUT_LOG_LIMIT)
                    .map { log ->
                        RecentWorkoutLogUiModel(
                            log = log,
                            exercise = exercisesById[log.exerciseId]
                        )
                    },
                summary = summary
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalysisUiState()
    )
}

private const val RECENT_WORKOUT_LOG_LIMIT = 3
