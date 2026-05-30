package com.smarttrainner.feature.routine.domain

import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.RoutineProgress
import kotlinx.coroutines.flow.Flow

interface RoutinePlanCatalogRepository {
    fun observePlanTemplates(): Flow<List<PlanTemplate>>
}

interface RoutineProgressRepository {
    fun observeRoutineProgress(): Flow<RoutineProgress>
}
