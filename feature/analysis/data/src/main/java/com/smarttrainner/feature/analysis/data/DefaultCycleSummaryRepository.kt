package com.smarttrainner.feature.analysis.data

import com.smarttrainner.core.domain.SessionRepository
import com.smarttrainner.core.model.CurrentRoutineCycle
import com.smarttrainner.core.model.CycleSummary
import com.smarttrainner.feature.analysis.domain.CycleSummaryCalculator
import com.smarttrainner.feature.analysis.domain.CycleSummaryRepository
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DefaultCycleSummaryRepository @Inject constructor(
    private val summaryCalculator: CycleSummaryCalculator,
    private val sessionRepository: SessionRepository
) : CycleSummaryRepository {
    override fun observeCycleSummary(
        currentCycle: CurrentRoutineCycle,
        zone: ZoneId
    ): Flow<CycleSummary> =
        sessionRepository.observeActiveSession().map { session ->
            summaryCalculator.calculate(
                currentCycle = currentCycle,
                zone = zone,
                bodyWeightKg = session?.profile?.latestBodyMeasurement?.weightKg
            )
        }
}
