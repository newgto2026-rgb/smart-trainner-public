package com.smarttrainner.feature.analysis.data

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.domain.CyclePlanRepository
import com.smarttrainner.core.domain.WorkoutLogRepository
import com.smarttrainner.core.model.CyclePlan
import com.smarttrainner.core.model.PlanId
import com.smarttrainner.core.model.RoutineProgress
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.feature.analysis.domain.CycleSummaryCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultCycleSummaryRepositoryTest {
    private val cyclePlanRepository = RecordingCyclePlanRepository()
    private val workoutLogRepository = EmptyWorkoutLogRepository()
    private val repository = DefaultCycleSummaryRepository(
        cyclePlanRepository = cyclePlanRepository,
        workoutLogRepository = workoutLogRepository,
        summaryCalculator = CycleSummaryCalculator()
    )

    @Test
    fun observeCycleSummary_usesRoutineStartWhenCycleStartIsMissing() = runTest {
        val summary = repository.observeCycleSummary(
            progress = progress(
                startedAt = Instant.parse("2026-05-20T00:00:00Z"),
                cycleStartedAt = null
            ),
            zone = ZoneOffset.UTC
        ).first()

        assertThat(cyclePlanRepository.requestedCycleStartDates)
            .containsExactly(LocalDate.of(2026, 5, 20))
        assertThat(summary.cycleStartDate).isEqualTo(LocalDate.of(2026, 5, 20))
    }

    @Test
    fun observeCycleSummary_prefersCycleStartOverRoutineStart() = runTest {
        val summary = repository.observeCycleSummary(
            progress = progress(
                startedAt = Instant.parse("2026-05-01T00:00:00Z"),
                cycleStartedAt = Instant.parse("2026-05-25T00:00:00Z")
            ),
            zone = ZoneOffset.UTC
        ).first()

        assertThat(cyclePlanRepository.requestedCycleStartDates)
            .containsExactly(LocalDate.of(2026, 5, 25))
        assertThat(summary.cycleStartDate).isEqualTo(LocalDate.of(2026, 5, 25))
    }

    private fun progress(
        startedAt: Instant?,
        cycleStartedAt: Instant?
    ): RoutineProgress = RoutineProgress(
        templateId = "balanced",
        dayIndex = 0,
        lastCompletedDayIndex = null,
        lastCompletedAt = null,
        cycleNumber = 1,
        startedAt = startedAt,
        cycleStartedAt = cycleStartedAt
    )
}

private class RecordingCyclePlanRepository : CyclePlanRepository {
    val requestedCycleStartDates = mutableListOf<LocalDate>()

    override fun observeCurrentCyclePlan(cycleStartDate: LocalDate): Flow<CyclePlan> {
        requestedCycleStartDates += cycleStartDate
        return flowOf(
            CyclePlan(
                id = PlanId("plan"),
                templateId = "balanced",
                name = "균형 루틴",
                cycleStartDate = cycleStartDate,
                days = emptyList()
            )
        )
    }
}

private class EmptyWorkoutLogRepository : WorkoutLogRepository {
    override fun observeLatestWorkoutLogs(): Flow<List<WorkoutLog>> = flowOf(emptyList())

    override fun observeAllWorkoutLogs(): Flow<List<WorkoutLog>> = flowOf(emptyList())
}
