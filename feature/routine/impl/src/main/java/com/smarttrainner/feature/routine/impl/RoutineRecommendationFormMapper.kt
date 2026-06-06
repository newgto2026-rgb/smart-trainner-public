package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.model.RoutineRecommendationInput

internal fun RoutineRecommendationFormState.toInput(): RoutineRecommendationInput =
    RoutineRecommendationInput(
        cycleLength = cycleLength,
        sessionMinutes = sessionMinutes,
        experience = experience,
        feeling = feeling
    )
