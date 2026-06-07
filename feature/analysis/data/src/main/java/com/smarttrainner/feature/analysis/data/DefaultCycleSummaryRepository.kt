package com.smarttrainner.feature.analysis.data

import com.smarttrainner.core.model.CurrentRoutineCycle
import com.smarttrainner.core.model.CycleSummary
import com.smarttrainner.feature.analysis.domain.CycleSummaryCalculator
import com.smarttrainner.feature.analysis.domain.CycleSummaryRepository
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class DefaultCycleSummaryRepository @Inject constructor(
    private val summaryCalculator: CycleSummaryCalculator
) : CycleSummaryRepository {
    override fun observeCycleSummary(
        currentCycle: CurrentRoutineCycle,
        zone: ZoneId
    ): Flow<CycleSummary> = flowOf(summaryCalculator.calculate(currentCycle, zone))
}
