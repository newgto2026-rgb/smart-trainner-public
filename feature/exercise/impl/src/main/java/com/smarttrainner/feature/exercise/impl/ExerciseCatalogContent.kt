package com.smarttrainner.feature.exercise.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.exercisemedia.TrainerExerciseImage
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutSetLog
import com.smarttrainner.core.model.toRecommendedDisplayRepRange
import com.smarttrainner.core.ui.SmartTrainnerBadge
import com.smarttrainner.core.ui.SmartTrainnerBadgeSpec
import com.smarttrainner.core.ui.SmartTrainnerEmptyState
import com.smarttrainner.core.ui.SmartTrainnerMetricCluster

private val armDetailGroups = listOf(
    MuscleGroup.BICEPS,
    MuscleGroup.TRICEPS,
    MuscleGroup.FOREARMS
)

internal fun LazyListScope.exerciseCatalogContent(
    state: ExerciseCatalogUiState,
    onSearchQueryChanged: (String) -> Unit,
    onExerciseSelected: (ExerciseId) -> Unit
) {
    val selectedExerciseId = state.selectedExerciseId
    val isSearching = state.searchQuery.isNotBlank()
    item {
        ExerciseSearchField(
            query = state.searchQuery,
            onQueryChanged = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth()
        )
    }
    item {
        Text(
            text = stringResource(
                if (isSearching) {
                    R.string.exercise_search_results
                } else {
                    R.string.exercise_all_exercises
                },
                state.exercises.size
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    if (selectedExerciseId == null && !isSearching) {
        item {
            SmartTrainnerEmptyState(text = stringResource(R.string.exercise_select_exercise_hint))
        }
    }
    if (state.exercises.isEmpty() && isSearching) {
        item {
            SmartTrainnerEmptyState(text = stringResource(R.string.exercise_search_empty))
        }
    }
    MuscleGroup.entries
        .filterNot { it in armDetailGroups }
        .forEach { group ->
            exerciseGroupSection(
                group = group,
                state = state,
                selectedExerciseId = selectedExerciseId,
                onExerciseSelected = onExerciseSelected
            )
        }
}

@Composable
private fun ExerciseSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier.testTag("training_exercise_search"),
        label = { Text(stringResource(R.string.exercise_search_label)) },
        placeholder = { Text(stringResource(R.string.exercise_search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(
                    onClick = { onQueryChanged("") },
                    modifier = Modifier.testTag("training_exercise_search_clear")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.exercise_search_clear)
                    )
                }
            }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        shape = RoundedCornerShape(8.dp)
    )
}

private fun LazyListScope.exerciseGroupSection(
    group: MuscleGroup,
    state: ExerciseCatalogUiState,
    selectedExerciseId: ExerciseId?,
    onExerciseSelected: (ExerciseId) -> Unit
) {
    val exercises = state.exercises.primaryExercisesFor(group)
    val armExercises = if (group == MuscleGroup.ARMS) {
        armDetailGroups.associateWith { armGroup ->
            state.exercises.primaryExercisesFor(armGroup)
        }
    } else {
        emptyMap()
    }
    if (exercises.isEmpty() && armExercises.values.all { it.isEmpty() }) return

    item {
        Text(
            text = group.localizedLabel(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
    if (group == MuscleGroup.ARMS) {
        armExercises.forEach { (armGroup, groupExercises) ->
            if (groupExercises.isNotEmpty()) {
                item {
                    Text(
                        text = armGroup.localizedLabel(),
                        color = SmartTrainnerColors.Muted,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                exerciseRows(
                    exercises = groupExercises,
                    latestWorkoutLogs = state.latestWorkoutLogs,
                    selectedExerciseId = selectedExerciseId,
                    keyPrefix = "${group.name}_${armGroup.name}",
                    onExerciseSelected = onExerciseSelected
                )
            }
        }
        exerciseRows(
            exercises = exercises,
            latestWorkoutLogs = state.latestWorkoutLogs,
            selectedExerciseId = selectedExerciseId,
            keyPrefix = group.name,
            onExerciseSelected = onExerciseSelected
        )
    } else {
        exerciseRows(
            exercises = exercises,
            latestWorkoutLogs = state.latestWorkoutLogs,
            selectedExerciseId = selectedExerciseId,
            keyPrefix = group.name,
            onExerciseSelected = onExerciseSelected
        )
    }
}

private fun LazyListScope.exerciseRows(
    exercises: List<Exercise>,
    latestWorkoutLogs: List<WorkoutLog>,
    selectedExerciseId: ExerciseId?,
    keyPrefix: String,
    onExerciseSelected: (ExerciseId) -> Unit
) {
    items(exercises, key = { "$keyPrefix:${it.id.value}" }) { exercise ->
        ExerciseRow(
            exercise = exercise,
            latestLog = latestWorkoutLogs.latestForExercise(exercise.id),
            selected = exercise.id == selectedExerciseId,
            onClick = { onExerciseSelected(exercise.id) },
            modifier = Modifier.testTag("training_exercise_row_${exercise.id.value}")
        )
    }
}

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    latestLog: WorkoutLog?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) SmartTrainnerColors.CoralSoft else SmartTrainnerColors.SurfaceRaised
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) SmartTrainnerColors.Coral else SmartTrainnerColors.Line
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrainerExerciseImage(
                exercise = exercise,
                modifier = Modifier.size(width = 76.dp, height = 84.dp),
                cleanThumbnailCrop = true
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.localizedName(),
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    SmartTrainnerBadge(
                        text = exercise.equipment.localizedLabel(),
                        containerColor = SmartTrainnerColors.SteelSoft,
                        contentColor = SmartTrainnerColors.Ink,
                        borderColor = SmartTrainnerColors.Line
                    )
                }
                SmartTrainnerMetricCluster(
                    label = stringResource(
                        if (latestLog == null) {
                            R.string.exercise_metric_recommended
                        } else {
                            R.string.exercise_metric_latest
                        }
                    ),
                    metrics = exercise.catalogMetricBadges(latestLog),
                    maxItemsPerRow = 3,
                    labelContainerColor = if (latestLog == null) {
                        SmartTrainnerColors.CoralSoft
                    } else {
                        SmartTrainnerColors.SteelSoft
                    }
                )
            }
        }
    }
}

@Composable
private fun Exercise.catalogMetricBadges(latestLog: WorkoutLog?): List<SmartTrainnerBadgeSpec> =
    latestLog?.catalogRecordMetricBadges() ?: buildList {
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.exercise_set_number, defaultSets),
                icon = Icons.Default.FitnessCenter,
                containerColor = SmartTrainnerColors.GreenSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
        val reps = defaultRepRange
        if (reps != null) {
            val displayReps = reps.toRecommendedDisplayRepRange()
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(
                        R.string.exercise_actual_reps,
                        "${displayReps.first}-${displayReps.last}"
                    ),
                    containerColor = SmartTrainnerColors.CoralSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        } else {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(
                        R.string.exercise_actual_duration,
                        (defaultDurationMinutes ?: 10).toString()
                    ),
                    icon = Icons.Default.Timer,
                    containerColor = SmartTrainnerColors.AmberSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.exercise_actual_rest, restSeconds.toString()),
                icon = Icons.Default.Timer,
                containerColor = SmartTrainnerColors.SteelSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
    }

private fun List<Exercise>.primaryExercisesFor(group: MuscleGroup): List<Exercise> =
    filter { it.muscleGroup == group }
        .sortedWith(
            compareBy(
                { it.movementPattern.sortRank },
                { it.variantRank },
                { it.popularityRank },
                { it.catalogOrder },
                { it.id.value }
            )
        )

@Composable
private fun WorkoutLog.catalogRecordMetricBadges(): List<SmartTrainnerBadgeSpec> {
    val entries = displaySetEntries()
    val reps = entries.mapNotNull { it.reps }.toCollapsedText()
    val weights = entries.mapNotNull { it.weightKg }.map { it.toRecordInput() }.toCollapsedText()
    val durations = entries.mapNotNull { it.durationMinutes }.toCollapsedText()
    val rests = entries.mapNotNull { it.restSeconds }.toCollapsedText()
    return buildList {
        add(
            SmartTrainnerBadgeSpec(
                text = stringResource(R.string.exercise_set_number, entries.size.coerceAtLeast(sets)),
                icon = Icons.Default.FitnessCenter,
                containerColor = SmartTrainnerColors.GreenSoft,
                contentColor = SmartTrainnerColors.Ink
            )
        )
        reps?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.exercise_actual_reps, it),
                    containerColor = SmartTrainnerColors.CoralSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
        weights?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.exercise_actual_weight, it),
                    icon = Icons.Default.FitnessCenter,
                    containerColor = SmartTrainnerColors.SteelSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
        durations?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.exercise_actual_duration, it),
                    icon = Icons.Default.Timer,
                    containerColor = SmartTrainnerColors.AmberSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
        rests?.let {
            add(
                SmartTrainnerBadgeSpec(
                    text = stringResource(R.string.exercise_actual_rest, it),
                    icon = Icons.Default.Timer,
                    containerColor = SmartTrainnerColors.SteelSoft,
                    contentColor = SmartTrainnerColors.Ink
                )
            )
        }
    }
}

private fun WorkoutLog.displaySetEntries(): List<WorkoutSetLog> =
    setEntries.takeIf { it.isNotEmpty() }
        ?: List(sets.coerceIn(1, 12)) { index ->
            WorkoutSetLog(
                order = index + 1,
                reps = reps,
                weightKg = weightKg,
                durationMinutes = durationMinutes
            )
        }

private fun <T> List<T>.toCollapsedText(): String? =
    takeIf { it.isNotEmpty() }?.let { values ->
        val distinctValues = values.distinct()
        if (distinctValues.size == 1) distinctValues.single().toString() else values.joinToString("/")
    }

private fun Double.toRecordInput(): String =
    if (rem(1.0) == 0.0) toLong().toString() else toString()

private fun List<WorkoutLog>.latestForExercise(exerciseId: ExerciseId): WorkoutLog? =
    filter { it.exerciseId == exerciseId }.maxByOrNull { it.performedAt }
