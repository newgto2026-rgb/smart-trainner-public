package com.smarttrainner.feature.analysis.domain

import com.smarttrainner.core.model.RoutineProgress
import java.time.ZoneId
import javax.inject.Inject

class ObserveCycleSummaryUseCase @Inject constructor(
    private val repository: CycleSummaryRepository
) {
    operator fun invoke(
        progress: RoutineProgress,
        zone: ZoneId
    ) = repository.observeCycleSummary(
        progress = progress,
        zone = zone
    )
}
