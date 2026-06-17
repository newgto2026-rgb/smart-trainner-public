package com.smarttrainner.feature.calendar.impl

import com.smarttrainner.core.model.ExerciseId
import java.time.LocalDate

internal sealed interface CalendarAction {
    data object OnPreviousMonthClick : CalendarAction
    data object OnNextMonthClick : CalendarAction
    data object OnToggleMonthExpansion : CalendarAction
    data class OnDateClick(val date: LocalDate) : CalendarAction
    data object OnAddWorkoutClick : CalendarAction
    data class OnEditWorkoutClick(val workout: CalendarSelectedWorkoutUiModel) : CalendarAction
    data class OnEditorExerciseSelected(val exerciseId: ExerciseId) : CalendarAction
    data class OnEditorSetRepsChanged(val index: Int, val value: String) : CalendarAction
    data class OnEditorSetWeightChanged(val index: Int, val value: String) : CalendarAction
    data class OnEditorSetDurationChanged(val index: Int, val value: String) : CalendarAction
    data class OnEditorSetRestChanged(val index: Int, val value: String) : CalendarAction
    data object OnEditorAddSet : CalendarAction
    data class OnEditorRemoveSet(val index: Int) : CalendarAction
    data class OnEditorMemoChanged(val value: String) : CalendarAction
    data object OnEditorSaveClick : CalendarAction
    data object OnEditorDismiss : CalendarAction
}
