package com.smarttrainner.feature.training.impl

import com.smarttrainner.core.model.CustomRoutineDayInput
import com.smarttrainner.core.model.CustomRoutineExerciseInput
import com.smarttrainner.core.model.CustomRoutineInput
import com.smarttrainner.core.model.RoutineRecommendationInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal const val MAX_CUSTOM_ROUTINE_DAYS = 7

internal fun RoutineRecommendationFormState.toInput(): RoutineRecommendationInput =
    RoutineRecommendationInput(
        daysPerWeek = daysPerWeek,
        sessionMinutes = sessionMinutes,
        experience = experience,
        feeling = feeling
    )

internal fun defaultBuilderDay(): CustomRoutineDayFormState = CustomRoutineDayFormState(
    title = "",
    focus = null,
    exercises = emptyList()
)

internal fun com.smarttrainner.core.model.Exercise.toCustomRoutineExerciseForm(): CustomRoutineExerciseFormState =
    CustomRoutineExerciseFormState(
        exercise = this,
        sets = defaultSets,
        repRangeStart = defaultRepRange?.first,
        repRangeEnd = defaultRepRange?.last,
        durationMinutes = defaultDurationMinutes,
        restSeconds = restSeconds
    )

internal fun MutableStateFlow<CustomRoutineBuilderState>.updateSelectedDay(
    updateDay: (CustomRoutineDayFormState) -> CustomRoutineDayFormState
) {
    update { builder ->
        val selectedIndex = builder.selectedDayIndex
        if (selectedIndex !in builder.days.indices) {
            builder
        } else {
            builder.copy(
                days = builder.days.mapIndexed { index, day ->
                    if (index == selectedIndex) updateDay(day) else day
                },
                error = null,
                savedTemplateId = null
            )
        }
    }
}

internal fun <T> List<T>.move(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex !in indices || toIndex !in indices) return this
    val mutable = toMutableList()
    val item = mutable.removeAt(fromIndex)
    mutable.add(toIndex, item)
    return mutable
}

internal fun CustomRoutineBuilderState.toFormError(): CustomRoutineFormError? = when {
    name.trim().isEmpty() -> CustomRoutineFormError.NAME
    days.isEmpty() || days.size > MAX_CUSTOM_ROUTINE_DAYS -> CustomRoutineFormError.DAYS
    days.any { it.exercises.isEmpty() } -> CustomRoutineFormError.EMPTY_DAY
    else -> null
}

internal fun CustomRoutineBuilderState.toInput(): CustomRoutineInput? {
    if (!visible) return null
    return CustomRoutineInput(
        id = editingRoutineId,
        name = name,
        description = "",
        days = days.map { day ->
            CustomRoutineDayInput(
                title = day.title,
                focus = day.focus?.name.orEmpty(),
                primaryFocus = day.focus,
                exercises = day.exercises.map { exercise ->
                    CustomRoutineExerciseInput(
                        exerciseId = exercise.exercise.id,
                        sets = exercise.sets,
                        repRangeStart = exercise.repRangeStart,
                        repRangeEnd = exercise.repRangeEnd,
                        durationMinutes = exercise.durationMinutes,
                        restSeconds = exercise.restSeconds,
                        note = exercise.note
                    )
                }
            )
        }
    )
}
