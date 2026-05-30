package com.smarttrainner.core.data

import com.smarttrainner.core.domain.RoutinePlanRepository
import com.smarttrainner.core.domain.WeeklySummaryCalculator
import com.smarttrainner.core.domain.WeeklySummaryRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.WeeklySummary
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class DefaultWeeklySummaryRepository @Inject constructor(
    private val routinePlanRepository: RoutinePlanRepository,
    private val workoutLogRepository: WorkoutLogRepository,
    private val summaryCalculator: WeeklySummaryCalculator
) : WeeklySummaryRepository {
    override fun observeWeeklySummary(weekStartDate: LocalDate): Flow<WeeklySummary> =
        combine(
            routinePlanRepository.observeCurrentWeeklyPlan(weekStartDate),
            workoutLogRepository.observeWorkoutLogs(weekStartDate)
        ) { plan, logs ->
            summaryCalculator.calculate(weekStartDate, plan, logs)
        }
}
