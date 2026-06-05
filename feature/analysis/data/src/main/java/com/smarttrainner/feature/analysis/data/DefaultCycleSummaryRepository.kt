package com.smarttrainner.feature.analysis.data

import com.smarttrainner.core.domain.CyclePlanRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.CycleSummary
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.feature.analysis.domain.CycleSummaryCalculator
import com.smarttrainner.feature.analysis.domain.CycleSummaryRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class DefaultCycleSummaryRepository @Inject constructor(
    private val cyclePlanRepository: CyclePlanRepository,
    private val workoutLogRepository: WorkoutLogRepository,
    private val summaryCalculator: CycleSummaryCalculator
) : CycleSummaryRepository {
    override fun observeCycleSummary(
        progress: RoutineProgress,
        zone: ZoneId
    ): Flow<CycleSummary> {
        val cycleStartDate = (progress.cycleStartedAt ?: progress.startedAt)
            ?.atZone(zone)
            ?.toLocalDate()
            ?: LocalDate.now(zone)
        return combine(
            cyclePlanRepository.observeCurrentCyclePlan(cycleStartDate),
            workoutLogRepository.observeAllWorkoutLogs()
        ) { plan, logs ->
            summaryCalculator.calculate(
                plan = plan,
                logs = logs,
                progress = progress,
                zone = zone
            )
        }
    }
}
