package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.model.RoutineRecommendationInput

internal fun RoutineRecommendationFormState.toInput(): RoutineRecommendationInput =
    RoutineRecommendationInput(
        daysPerWeek = daysPerWeek,
        sessionMinutes = sessionMinutes,
        experience = experience,
        feeling = feeling
    )
