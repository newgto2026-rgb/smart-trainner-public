package com.smarttrainner.feature.routine.domain

import com.smarttrainner.core.model.PlanTemplate
import kotlinx.coroutines.flow.Flow

interface RoutinePlanCatalogRepository {
    fun observePlanTemplates(): Flow<List<PlanTemplate>>
}
