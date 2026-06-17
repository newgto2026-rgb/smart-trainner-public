package com.smarttrainner.feature.exercise.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ArchiveCustomExerciseUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.ExerciseSource
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.feature.exercise.domain.GetExerciseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase,
    private val getExercise: GetExerciseUseCase,
    private val archiveCustomExercise: ArchiveCustomExerciseUseCase
) : ViewModel() {
    private val selectedExerciseId = MutableStateFlow<ExerciseId?>(null)
    private val showRecordAction = MutableStateFlow(false)
    private val showDeleteConfirmation = MutableStateFlow(false)
    private val deleting = MutableStateFlow(false)
    private val deleteError = MutableStateFlow(false)

    private val latestWorkoutLogs = observeLatestWorkoutLogs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedExercise = selectedExerciseId.mapLatest { id ->
        id?.let { getExercise(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val deleteState = combine(
        showDeleteConfirmation,
        deleting,
        deleteError
    ) { confirmDelete, deleting, deleteError ->
        ExerciseDeleteState(
            showConfirmation = confirmDelete,
            deleting = deleting,
            error = deleteError
        )
    }

    internal val uiState = combine(
        selectedExerciseId,
        selectedExercise,
        showRecordAction,
        latestWorkoutLogs,
        deleteState
    ) { exerciseId, exercise, showRecordAction, latestLogs, deleteState ->
        val currentExercise = exercise?.takeIf { it.id == exerciseId }
        val canDelete = currentExercise?.source == ExerciseSource.USER_CREATED
        ExerciseDetailUiState(
            exercise = currentExercise,
            latestWorkoutLog = currentExercise?.let { latestLogs.latestForExercise(it.id) },
            showRecordAction = showRecordAction && currentExercise != null,
            showDeleteAction = canDelete,
            showDeleteConfirmation = canDelete && deleteState.showConfirmation,
            deleting = deleteState.deleting,
            deleteError = deleteState.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExerciseDetailUiState()
    )

    fun updateSelection(
        exerciseId: ExerciseId?,
        shouldShowRecordAction: Boolean
    ) {
        if (exerciseId == null) {
            selectedExerciseId.value = null
            showRecordAction.value = false
            resetDeleteState()
            return
        }
        showRecordAction.value = shouldShowRecordAction
        if (selectedExerciseId.value == exerciseId) return
        resetDeleteState()
        selectedExerciseId.value = exerciseId
    }

    fun requestDeleteCustomExercise() {
        if (selectedExercise.value?.source != ExerciseSource.USER_CREATED || deleting.value) return
        deleteError.value = false
        showDeleteConfirmation.value = true
    }

    fun dismissDeleteCustomExercise() {
        if (deleting.value) return
        showDeleteConfirmation.value = false
        deleteError.value = false
    }

    fun confirmDeleteCustomExercise(onDeleted: () -> Unit) {
        val exercise = selectedExercise.value?.takeIf { it.source == ExerciseSource.USER_CREATED } ?: return
        if (deleting.value) return
        deleting.value = true
        deleteError.value = false
        viewModelScope.launch {
            archiveCustomExercise(exercise.id).fold(
                onSuccess = {
                    resetDeleteState()
                    selectedExerciseId.value = null
                    showRecordAction.value = false
                    onDeleted()
                },
                onFailure = {
                    deleting.update { false }
                    deleteError.value = true
                }
            )
        }
    }

    private fun resetDeleteState() {
        showDeleteConfirmation.value = false
        deleting.value = false
        deleteError.value = false
    }
}

private data class ExerciseDeleteState(
    val showConfirmation: Boolean,
    val deleting: Boolean,
    val error: Boolean
)

private fun List<WorkoutLog>.latestForExercise(exerciseId: ExerciseId): WorkoutLog? =
    filter { it.exerciseId == exerciseId }
        .maxByOrNull { it.performedAt }
