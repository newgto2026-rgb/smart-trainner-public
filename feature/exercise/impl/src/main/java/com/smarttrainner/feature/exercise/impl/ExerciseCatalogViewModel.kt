package com.smarttrainner.feature.exercise.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.domain.SaveCustomExerciseUseCase
import com.smarttrainner.core.domain.CustomExerciseValidationError
import com.smarttrainner.core.model.CustomExerciseInput
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExerciseCatalogViewModel : ViewModel {
    private val searchQuery = MutableStateFlow("")
    private val customExerciseForm = MutableStateFlow(CustomExerciseFormUiState())
    private val latestExerciseIds = MutableStateFlow<Set<ExerciseId>>(emptySet())
    private val saveCustomExercise: SaveCustomExerciseUseCase

    internal val uiState: StateFlow<ExerciseCatalogUiState>

    @Inject
    constructor(
        observeExercises: ObserveExercisesUseCase,
        observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase,
        saveCustomExercise: SaveCustomExerciseUseCase
    ) : this(
        observeExercises = observeExercises,
        observeLatestWorkoutLogs = observeLatestWorkoutLogs,
        saveCustomExercise = saveCustomExercise,
        searchDispatcher = Dispatchers.Default
    )

    internal constructor(
        observeExercises: ObserveExercisesUseCase,
        observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase,
        saveCustomExercise: SaveCustomExerciseUseCase,
        searchDispatcher: CoroutineDispatcher
    ) : super() {
        this.saveCustomExercise = saveCustomExercise
        val observedExercises = observeExercises()
        val filteredExercises = combine(
            observedExercises,
            searchQuery
        ) { exercises, query ->
            latestExerciseIds.value = exercises.map { it.id }.toSet()
            exercises.filterBySearchQuery(query)
        }.flowOn(searchDispatcher)

        uiState = combine(
            filteredExercises,
            observeLatestWorkoutLogs(),
            searchQuery,
            customExerciseForm
        ) { exercises, latestLogs, query, form ->
            ExerciseCatalogUiState(
                exercises = exercises,
                latestWorkoutLogs = latestLogs,
                searchQuery = query,
                customExerciseForm = form
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseCatalogUiState()
        )
    }

    internal fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    internal fun openCustomExerciseForm() {
        customExerciseForm.value = CustomExerciseFormUiState(visible = true)
    }

    internal fun closeCustomExerciseForm() {
        if (customExerciseForm.value.saving) return
        customExerciseForm.value = CustomExerciseFormUiState()
    }

    internal fun updateCustomExerciseName(value: String) = updateForm { copy(name = value, error = null) }
    internal fun updateCustomExerciseMuscleGroup(value: com.smarttrainner.core.model.MuscleGroup) =
        updateForm { copy(muscleGroup = value, error = null) }
    internal fun updateCustomExerciseEquipment(value: com.smarttrainner.core.model.EquipmentType) =
        updateForm { copy(equipment = value, error = null) }
    internal fun updateCustomExerciseDifficulty(value: com.smarttrainner.core.model.DifficultyLevel) =
        updateForm { copy(difficulty = value, error = null) }
    internal fun updateCustomExerciseImageUri(value: String) = updateForm { copy(imageUri = value, error = null) }
    internal fun updateCustomExerciseSummary(value: String) = updateForm { copy(summary = value, error = null) }
    internal fun updateCustomExerciseSets(value: String) = updateForm { copy(defaultSets = value, error = null) }
    internal fun updateCustomExerciseRepStart(value: String) = updateForm { copy(repRangeStart = value, error = null) }
    internal fun updateCustomExerciseRepEnd(value: String) = updateForm { copy(repRangeEnd = value, error = null) }
    internal fun updateCustomExerciseDuration(value: String) =
        updateForm { copy(defaultDurationMinutes = value, error = null) }
    internal fun updateCustomExerciseRest(value: String) = updateForm { copy(restSeconds = value, error = null) }

    internal fun updateCustomExerciseInstruction(index: Int, value: String) = updateForm {
        copy(instructions = instructions.updateAt(index, value), error = null)
    }

    internal fun addCustomExerciseInstruction() = updateForm {
        copy(instructions = instructions + "", error = null)
    }

    internal fun updateCustomExerciseSafetyCue(index: Int, value: String) = updateForm {
        copy(safetyCues = safetyCues.updateAt(index, value), error = null)
    }

    internal fun addCustomExerciseSafetyCue() = updateForm {
        copy(safetyCues = safetyCues + "", error = null)
    }

    internal fun saveCustomExercise() {
        val form = customExerciseForm.value
        if (form.saving) return
        val input = form.toInputOrError()?.getOrElse { error ->
            customExerciseForm.update {
                it.copy(error = (error as? CustomExerciseFormException)?.formError ?: CustomExerciseFormError.SAVE)
            }
            return
        } ?: return
        customExerciseForm.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            saveCustomExercise(input, latestExerciseIds.value).fold(
                onSuccess = {
                    customExerciseForm.value = CustomExerciseFormUiState()
                },
                onFailure = { error ->
                    customExerciseForm.update { state ->
                        state.copy(saving = false, error = error.toFormError())
                    }
                }
            )
        }
    }

    private fun updateForm(reducer: CustomExerciseFormUiState.() -> CustomExerciseFormUiState) {
        customExerciseForm.update { current -> current.reducer() }
    }
}

internal fun List<String>.updateAt(index: Int, value: String): List<String> =
    mapIndexed { itemIndex, item -> if (itemIndex == index) value else item }

internal fun CustomExerciseFormUiState.toInputOrError(): Result<CustomExerciseInput>? {
    val sets = defaultSets.toIntOrNull() ?: return formFailure(CustomExerciseFormError.SETS)
    val repStart = repRangeStart.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        ?: if (repRangeStart.isBlank()) null else return formFailure(CustomExerciseFormError.REPS)
    val repEnd = repRangeEnd.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        ?: if (repRangeEnd.isBlank()) null else return formFailure(CustomExerciseFormError.REPS)
    val duration = defaultDurationMinutes.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        ?: if (defaultDurationMinutes.isBlank()) null else return formFailure(CustomExerciseFormError.DURATION)
    val rest = restSeconds.toIntOrNull() ?: return formFailure(CustomExerciseFormError.REST)
    return Result.success(
        CustomExerciseInput(
            name = name,
            muscleGroup = muscleGroup,
            equipment = equipment,
            difficulty = difficulty,
            imageUri = imageUri,
            summary = summary,
            instructions = instructions,
            safetyCues = safetyCues,
            defaultSets = sets,
            repRangeStart = repStart,
            repRangeEnd = repEnd,
            defaultDurationMinutes = duration,
            restSeconds = rest
        )
    )
}

internal class CustomExerciseFormException(val formError: CustomExerciseFormError) : IllegalArgumentException()

internal fun formFailure(error: CustomExerciseFormError): Result<CustomExerciseInput> =
    Result.failure(CustomExerciseFormException(error))

internal fun Throwable.toFormError(): CustomExerciseFormError =
    (this as? CustomExerciseFormException)?.formError
        ?: message?.let { runCatching { CustomExerciseValidationError.valueOf(it) }.getOrNull() }?.toFormError()
        ?: CustomExerciseFormError.SAVE

internal fun CustomExerciseValidationError.toFormError(): CustomExerciseFormError = when (this) {
    CustomExerciseValidationError.NAME -> CustomExerciseFormError.NAME
    CustomExerciseValidationError.INSTRUCTIONS -> CustomExerciseFormError.INSTRUCTIONS
    CustomExerciseValidationError.SAFETY -> CustomExerciseFormError.SAFETY
    CustomExerciseValidationError.SETS -> CustomExerciseFormError.SETS
    CustomExerciseValidationError.TARGET -> CustomExerciseFormError.TARGET
    CustomExerciseValidationError.REPS -> CustomExerciseFormError.REPS
    CustomExerciseValidationError.DURATION -> CustomExerciseFormError.DURATION
    CustomExerciseValidationError.REST -> CustomExerciseFormError.REST
    CustomExerciseValidationError.ID -> CustomExerciseFormError.SAVE
}

private val searchTokenRegex = Regex("""[\p{L}\p{N}]+""")

internal fun List<Exercise>.filterBySearchQuery(query: String): List<Exercise> {
    val tokens = query.searchTokens()
    if (tokens.isEmpty()) return this

    return filter { exercise ->
        val searchText = exercise.searchText()
        tokens.all { token -> searchText.contains(token) }
    }
}

private fun String.searchTokens(): List<String> =
    searchTokenRegex.findAll(normalizedForSearch())
        .map { it.value }
        .distinct()
        .toList()

private fun Exercise.searchText(): String =
    "${name.normalizedForSearch()} ${id.value.normalizedForSearch()} " +
        "${muscleGroup.displayName.normalizedForSearch()} ${muscleGroup.name.normalizedForSearch()} " +
        "${equipment.displayName.normalizedForSearch()} ${equipment.name.normalizedForSearch()}"

private fun String.normalizedForSearch(): String =
    lowercase(Locale.ROOT)
        .replace('_', ' ')
        .replace('-', ' ')
