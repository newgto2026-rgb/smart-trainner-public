package com.smarttrainner.app.training

import com.smarttrainner.core.model.RoutineRecommendationInput
import com.smarttrainner.feature.routine.api.RoutineRecommendationFormState

internal fun RoutineRecommendationFormState.toInput(): RoutineRecommendationInput =
    RoutineRecommendationInput(
        daysPerWeek = daysPerWeek,
        sessionMinutes = sessionMinutes,
        experience = experience,
        feeling = feeling
    )
