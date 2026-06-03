package com.smarttrainner.feature.exercise.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveExercisesUseCase
import com.smarttrainner.core.domain.ObserveLatestWorkoutLogsUseCase
import com.smarttrainner.core.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExerciseCatalogViewModel @Inject constructor(
    observeExercises: ObserveExercisesUseCase,
    observeLatestWorkoutLogs: ObserveLatestWorkoutLogsUseCase
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")

    internal val uiState = combine(
        observeExercises(),
        observeLatestWorkoutLogs(),
        searchQuery
    ) { exercises, latestLogs, query ->
        ExerciseCatalogUiState(
            exercises = exercises.filterBySearchQuery(query),
            latestWorkoutLogs = latestLogs,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExerciseCatalogUiState()
    )

    internal fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }
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

private fun Exercise.searchText(): String = listOf(
    name,
    id.value,
    muscleGroup.displayName,
    muscleGroup.name,
    equipment.displayName,
    equipment.name
).joinToString(separator = " ") { it.normalizedForSearch() }

private fun String.normalizedForSearch(): String =
    lowercase(Locale.ROOT)
        .replace('_', ' ')
        .replace('-', ' ')
