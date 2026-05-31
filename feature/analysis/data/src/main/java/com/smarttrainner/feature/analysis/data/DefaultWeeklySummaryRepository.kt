package com.smarttrainner.feature.analysis.data

import com.smarttrainner.core.domain.WeeklyPlanRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.feature.analysis.domain.WeeklySummaryCalculator
import com.smarttrainner.feature.analysis.domain.WeeklySummaryRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class DefaultWeeklySummaryRepository @Inject constructor(
    private val weeklyPlanRepository: WeeklyPlanRepository,
    private val workoutLogRepository: WorkoutLogRepository,
    private val summaryCalculator: WeeklySummaryCalculator
) : WeeklySummaryRepository {
    override fun observeWeeklySummary(weekStartDate: LocalDate): Flow<WeeklySummary> =
        combine(
            weeklyPlanRepository.observeCurrentWeeklyPlan(weekStartDate),
            workoutLogRepository.observeWorkoutLogs(weekStartDate)
        ) { plan, logs ->
            summaryCalculator.calculate(weekStartDate, plan, logs)
        }
}
