package com.smarttrainner.feature.analysis.domain

import com.smarttrainner.core.model.CurrentRoutineCycle
import java.time.ZoneId
import javax.inject.Inject

class ObserveCycleSummaryUseCase @Inject constructor(
    private val repository: CycleSummaryRepository
) {
    operator fun invoke(
        currentCycle: CurrentRoutineCycle,
        zone: ZoneId
    ) = repository.observeCycleSummary(
        currentCycle = currentCycle,
        zone = zone
    )
}
