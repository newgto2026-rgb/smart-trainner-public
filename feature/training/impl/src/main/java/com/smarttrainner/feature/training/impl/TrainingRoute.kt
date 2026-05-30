package com.smarttrainner.feature.training.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.designsystem.SmartTrainnerGradients
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TrainingRoute(
    viewModel: TrainingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrainingScreen(
        state = state,
        onTabSelected = viewModel::selectTab,
        onTemplateSelected = viewModel::selectTemplate,
        onRoutineDaysPerWeekChanged = viewModel::updateRoutineDaysPerWeek,
        onRoutineSessionMinutesChanged = viewModel::updateRoutineSessionMinutes,
        onRoutineExperienceChanged = viewModel::updateRoutineExperience,
        onRoutineFeelingChanged = viewModel::updateRoutineFeeling,
        onShowRoutineLibrary = viewModel::showRoutineLibrary,
        onRoutineLibraryDismiss = viewModel::dismissRoutineLibrary,
        onShowRoutineSettings = viewModel::showRoutineSettings,
        onRoutineSettingsDismiss = viewModel::dismissRoutineSettings,
        onShowRoutineRecommendations = viewModel::showRoutineRecommendations,
        onRoutineRecommendationsDismiss = viewModel::dismissRoutineRecommendations,
        onRoutinePreviewSelected = viewModel::selectRoutinePreview,
        onStartPreviewRoutine = viewModel::startPreviewRoutine,
        onCreateCustomRoutine = viewModel::showCreateCustomRoutine,
        onCopyTemplateToCustom = viewModel::copyTemplateToCustom,
        onEditCustomRoutine = viewModel::editCustomRoutine,
        onCustomRoutineNameChanged = viewModel::updateCustomRoutineName,
        onCustomRoutineDaySelected = viewModel::selectCustomRoutineDay,
        onCustomRoutineDayFocusChanged = viewModel::updateCustomRoutineDayFocus,
        onCustomRoutineDayAdded = viewModel::addCustomRoutineDay,
        onCustomRoutineDayRemoved = viewModel::removeCustomRoutineDay,
        onCustomRoutineExerciseGroupToggled = viewModel::toggleCustomRoutineExerciseGroup,
        onCustomRoutineExerciseAdded = viewModel::addExerciseToCustomRoutine,
        onCustomRoutineExerciseRemoved = viewModel::removeExerciseFromCustomRoutine,
        onCustomRoutineExerciseMovedUp = viewModel::moveCustomRoutineExerciseUp,
        onCustomRoutineExerciseMovedDown = viewModel::moveCustomRoutineExerciseDown,
        onCustomRoutineSaved = viewModel::saveCustomRoutine,
        onCustomRoutineBuilderDismiss = viewModel::dismissCustomRoutineBuilder,
        onExerciseSelected = viewModel::selectExercise,
        onExerciseMethodSelected = viewModel::showExerciseMethod,
        onWorkoutStarted = viewModel::startWorkout,
        onRecordSelected = viewModel::selectPlannedExercise,
        onCompleteRoutineDay = viewModel::completeCurrentRoutineDay,
        onSetRepsChanged = viewModel::updateSetReps,
        onSetWeightChanged = viewModel::updateSetWeight,
        onSetDurationChanged = viewModel::updateSetDuration,
        onSetRestChanged = viewModel::updateSetRest,
        onAddSet = viewModel::addSetEntry,
        onRemoveSet = viewModel::removeSetEntry,
        onMemoChanged = viewModel::updateMemo,
        onSaveRecord = viewModel::saveRecord,
        onExerciseDetailDismiss = viewModel::dismissExerciseDetail,
        onRecordDialogDismiss = viewModel::dismissRecordDialog
    )
}

@Composable
private fun TrainingScreen(
    state: TrainingUiState,
    onTabSelected: (TrainingTab) -> Unit,
    onTemplateSelected: (String) -> Unit,
    onRoutineDaysPerWeekChanged: (Int) -> Unit,
    onRoutineSessionMinutesChanged: (Int) -> Unit,
    onRoutineExperienceChanged: (TrainingExperience) -> Unit,
    onRoutineFeelingChanged: (RoutineFeeling) -> Unit,
    onShowRoutineLibrary: () -> Unit,
    onRoutineLibraryDismiss: () -> Unit,
    onShowRoutineSettings: () -> Unit,
    onRoutineSettingsDismiss: () -> Unit,
    onShowRoutineRecommendations: () -> Unit,
    onRoutineRecommendationsDismiss: () -> Unit,
    onRoutinePreviewSelected: (String) -> Unit,
    onStartPreviewRoutine: () -> Unit,
    onCreateCustomRoutine: () -> Unit,
    onCopyTemplateToCustom: (String) -> Unit,
    onEditCustomRoutine: (String) -> Unit,
    onCustomRoutineNameChanged: (String) -> Unit,
    onCustomRoutineDaySelected: (Int) -> Unit,
    onCustomRoutineDayFocusChanged: (RoutineFocus?) -> Unit,
    onCustomRoutineDayAdded: () -> Unit,
    onCustomRoutineDayRemoved: (Int) -> Unit,
    onCustomRoutineExerciseGroupToggled: (MuscleGroup) -> Unit,
    onCustomRoutineExerciseAdded: (com.smarttrainner.core.model.ExerciseId) -> Unit,
    onCustomRoutineExerciseRemoved: (Int) -> Unit,
    onCustomRoutineExerciseMovedUp: (Int) -> Unit,
    onCustomRoutineExerciseMovedDown: (Int) -> Unit,
    onCustomRoutineSaved: (Boolean) -> Unit,
    onCustomRoutineBuilderDismiss: () -> Unit,
    onExerciseSelected: (com.smarttrainner.core.model.ExerciseId) -> Unit,
    onExerciseMethodSelected: (com.smarttrainner.core.model.ExerciseId) -> Unit,
    onWorkoutStarted: (PlannedExercise) -> Unit,
    onRecordSelected: (PlannedExercise) -> Unit,
    onCompleteRoutineDay: () -> Unit,
    onSetRepsChanged: (Int, String) -> Unit,
    onSetWeightChanged: (Int, String) -> Unit,
    onSetDurationChanged: (Int, String) -> Unit,
    onSetRestChanged: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onMemoChanged: (String) -> Unit,
    onSaveRecord: () -> Unit,
    onExerciseDetailDismiss: () -> Unit,
    onRecordDialogDismiss: () -> Unit
) {
    val selectedExercise = state.selectedExercise
    val recordingPlannedExercise = state.recordingPlannedExercise
    if (recordingPlannedExercise != null && selectedExercise == null) {
        RecordDialog(
            state = state,
            planned = recordingPlannedExercise,
            onSetRepsChanged = onSetRepsChanged,
            onSetWeightChanged = onSetWeightChanged,
            onSetDurationChanged = onSetDurationChanged,
            onSetRestChanged = onSetRestChanged,
            onAddSet = onAddSet,
            onRemoveSet = onRemoveSet,
            onMemoChanged = onMemoChanged,
            onSaveRecord = onSaveRecord,
            onExerciseMethodSelected = { onExerciseMethodSelected(recordingPlannedExercise.exercise.id) },
            onDismissRequest = onRecordDialogDismiss
        )
    }
    if (state.showRoutineLibraryDialog) {
        RoutineLibraryDialog(
            state = state,
            onTemplateSelected = onTemplateSelected,
            onShowRoutineSettings = onShowRoutineSettings,
            onCreateCustomRoutine = onCreateCustomRoutine,
            onCopyTemplateToCustom = onCopyTemplateToCustom,
            onEditCustomRoutine = onEditCustomRoutine,
            onDismissRequest = onRoutineLibraryDismiss
        )
    }
    if (state.showRoutineSettingsDialog) {
        RoutineSettingsDialog(
            form = state.routineRecommendationInput,
            onDaysPerWeekChanged = onRoutineDaysPerWeekChanged,
            onSessionMinutesChanged = onRoutineSessionMinutesChanged,
            onExperienceChanged = onRoutineExperienceChanged,
            onFeelingChanged = onRoutineFeelingChanged,
            onShowRecommendations = onShowRoutineRecommendations,
            onDismissRequest = onRoutineSettingsDismiss
        )
    }
    if (state.showRoutineRecommendationsDialog) {
        RoutineRecommendationsDialog(
            state = state,
            onTemplatePreviewSelected = onRoutinePreviewSelected,
            onStartRoutine = onStartPreviewRoutine,
            onDismissRequest = onRoutineRecommendationsDismiss
        )
    }
    if (state.customRoutineBuilder.visible) {
        CustomRoutineBuilderSheet(
            builder = state.customRoutineBuilder,
            exercises = state.exercises,
            onNameChanged = onCustomRoutineNameChanged,
            onDaySelected = onCustomRoutineDaySelected,
            onDayFocusChanged = onCustomRoutineDayFocusChanged,
            onAddDay = onCustomRoutineDayAdded,
            onRemoveDay = onCustomRoutineDayRemoved,
            onExerciseGroupToggled = onCustomRoutineExerciseGroupToggled,
            onExerciseDetailRequested = onExerciseMethodSelected,
            onAddExercise = onCustomRoutineExerciseAdded,
            onRemoveExercise = onCustomRoutineExerciseRemoved,
            onMoveExerciseUp = onCustomRoutineExerciseMovedUp,
            onMoveExerciseDown = onCustomRoutineExerciseMovedDown,
            onSave = { onCustomRoutineSaved(false) },
            onDismissRequest = onCustomRoutineBuilderDismiss
        )
    }
    if (selectedExercise != null) {
        ExerciseDetailDialog(
            exercise = selectedExercise,
            plannedExercise = if (state.recordingPlannedExercise == null && !state.customRoutineBuilder.visible) {
                state.plan?.days
                    ?.flatMap { it.exercises }
                    ?.firstOrNull { it.exercise.id == selectedExercise.id }
            } else {
                null
            },
            onRecordSelected = onRecordSelected,
            onDismissRequest = onExerciseDetailDismiss,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartTrainnerGradients.screen())
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                TrainingBottomBar(
                    selectedTab = state.selectedTab,
                    onTabSelected = onTabSelected
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                contentPadding = PaddingValues(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Header(state) }
                when (state.selectedTab) {
                    TrainingTab.HOME -> homeContent(state, onWorkoutStarted, onCompleteRoutineDay)
                    TrainingTab.PLAN -> planContent(
                        state = state,
                        onShowRoutineLibrary = onShowRoutineLibrary,
                        onCreateCustomRoutine = onCreateCustomRoutine,
                        onEditCustomRoutine = onEditCustomRoutine,
                        onRecordSelected = onRecordSelected
                    )
                    TrainingTab.EXERCISES -> exerciseContent(state, onExerciseSelected)
                    TrainingTab.ANALYSIS -> analysisContent(state)
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.homeContent(
    state: TrainingUiState,
    onWorkoutStarted: (PlannedExercise) -> Unit,
    onCompleteRoutineDay: () -> Unit
) {
    item {
        SectionTitle(
            stringResourceId = R.string.training_today_title,
            testTag = "training_section_title_today"
        )
    }
    val nextRoutineDay = state.nextRoutineDayUi
    if (nextRoutineDay == null) {
        item { EmptyState(text = stringResource(R.string.training_empty_plan)) }
    } else {
        item {
            NextRoutineDayCard(
                routineDay = nextRoutineDay,
                onRecordSelected = onWorkoutStarted,
                onCompleteRoutineDay = onCompleteRoutineDay
            )
        }
    }
    state.formError?.takeIf { it == RecordFormError.COMPLETE_DAY_FAILED }?.let {
        item {
            Text(
                text = it.message(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.planContent(
    state: TrainingUiState,
    onShowRoutineLibrary: () -> Unit,
    onCreateCustomRoutine: () -> Unit,
    onEditCustomRoutine: (String) -> Unit,
    onRecordSelected: (PlannedExercise) -> Unit
) {
    item {
        Text(
            text = stringResource(R.string.training_current_routine),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val selectedTemplate = state.templates.firstOrNull { it.id == state.selectedTemplateId }
            if (selectedTemplate != null) {
                CurrentRoutineSummaryCard(
                    template = selectedTemplate,
                    onEditCustomRoutine = onEditCustomRoutine,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                EmptyState(text = stringResource(R.string.training_empty_plan))
            }
            Button(
                onClick = onShowRoutineLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_find_routine_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.training_change_routine))
            }
            OutlinedButton(
                onClick = onCreateCustomRoutine,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_create_custom_routine_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.training_create_custom_routine))
            }
        }
    }
    item {
        Text(
            text = stringResource(R.string.training_routine_schedule),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    state.plan?.days.orEmpty().forEach { day ->
        item {
            DayPlanSection(
                title = day.title,
                focus = day.focus,
                primaryFocus = day.primaryFocus,
                dayNumber = day.dayNumber,
                exercises = day.exercises,
                weeklyLogs = state.logs,
                latestLogs = state.latestWorkoutLogs,
                completedIds = state.completedPlannedExerciseIds,
                onRecordSelected = onRecordSelected
            )
        }
    }
}

@Composable
private fun CurrentRoutineSummaryCard(
    template: PlanTemplate,
    onEditCustomRoutine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("training_current_routine_card"),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.localizedName(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                RoutineSourceChip(template.source, template.source.currentRoutineSourceTag())
            }
            Text(
                text = template.localizedMeta(),
                color = SmartTrainnerColors.Coral,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            RoutineFocusFlow(template)
            if (template.source == RoutineSource.CUSTOM) {
                OutlinedButton(
                    onClick = { onEditCustomRoutine(template.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("training_edit_current_custom_routine"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.training_edit_custom_routine))
                }
            }
        }
    }
}

@Composable
private fun NextRoutineDayCard(
    routineDay: NextRoutineDayUiModel,
    onRecordSelected: (PlannedExercise) -> Unit,
    onCompleteRoutineDay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("training_next_routine_day_card"),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            routineDay.routineTemplate?.let { template ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoutineSourceChip(template.source, template.source.homeRoutineSourceTag())
                    Text(
                        text = template.localizedName(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_home_current_routine_name"),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SmartTrainnerColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val isCustomRoutine = routineDay.routineTemplate?.source == RoutineSource.CUSTOM
                val fallbackDayTitle = planDayDisplayTitle(routineDay.day.title, routineDay.dayNumber)
                val hasCustomDayTitle = routineDay.day.title
                    .hasMeaningfulPlanDayTitle(routineDay.dayNumber)
                val shouldShowCustomDayLabel = isCustomRoutine &&
                    (routineDay.primaryFocus != null || hasCustomDayTitle)
                Text(
                    text = routineDay.primaryFocus?.let { focus ->
                        stringResource(R.string.training_today_focus_title, focus.localizedTodayFocusLabel())
                    } ?: fallbackDayTitle,
                    modifier = Modifier.testTag("training_next_routine_day_${routineDay.dayNumber}"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = SmartTrainnerColors.Ink
                )
                if (shouldShowCustomDayLabel) {
                    Text(
                        text = stringResource(R.string.training_day_label, routineDay.dayNumber),
                        color = SmartTrainnerColors.Coral,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.training_routine_day_subtitle,
                            routineDay.dayNumber,
                            routineDay.focus.localizedPlanFocus(),
                            routineDay.sessionMinutes
                        ),
                        modifier = Modifier.testTag("training_next_routine_time_estimate"),
                        color = SmartTrainnerColors.Coral,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            RoutineProgressBar(
                progress = if (routineDay.totalExerciseCount == 0) {
                    0f
                } else {
                    routineDay.completedExerciseCount.toFloat() / routineDay.totalExerciseCount
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Text(
                text = stringResource(
                    R.string.training_routine_completed_progress,
                    routineDay.completedExerciseCount,
                    routineDay.totalExerciseCount
                ),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodySmall
            )
            val focusItems = (listOfNotNull(routineDay.primaryFocus) + routineDay.secondaryFocuses).distinct()
            if (focusItems.isNotEmpty()) {
                Column(
                    modifier = Modifier.testTag("training_next_routine_focus_section"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.training_routine_main_focus),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    WrappedChipRows(
                        items = focusItems,
                        maxItemsPerRow = 4
                    ) { focus ->
                        AssistChip(
                            onClick = {},
                            label = { Text(focus.localizedShortLabel()) },
                            modifier = Modifier.testTag("training_next_routine_focus_${focus.name}")
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val previewNames = routineDay.previewExercises.map { it.exercise.localizedName() }
                Text(
                    text = stringResource(R.string.training_routine_examples),
                    modifier = Modifier.testTag("training_next_routine_plan_title"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = previewNames.joinToString(" · "),
                    modifier = Modifier.testTag("training_next_routine_plan_exercises"),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            routineDay.startExercise?.let { startExercise ->
                Button(
                    onClick = { onRecordSelected(startExercise) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("training_home_start_workout")
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.training_start_record))
                }
            }
            OutlinedButton(
                onClick = onCompleteRoutineDay,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("training_complete_routine_day"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.training_complete_routine_day))
            }
            routineDay.nextPrimaryFocus?.let { nextFocus ->
                Text(
                    text = stringResource(R.string.training_next_routine_day, nextFocus.localizedTodayFocusLabel()),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun RoutineRecommendationControls(
    form: RoutineRecommendationFormState,
    onDaysPerWeekChanged: (Int) -> Unit,
    onSessionMinutesChanged: (Int) -> Unit,
    onExperienceChanged: (TrainingExperience) -> Unit,
    onFeelingChanged: (RoutineFeeling) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RoutineOptionRow(
            label = stringResource(R.string.training_routine_days_question),
            options = listOf(2, 3, 4, 5).map {
                it to stringResource(R.string.training_days_per_week_option, it)
            },
            selected = form.daysPerWeek,
            onSelected = onDaysPerWeekChanged
        )
        RoutineOptionRow(
            label = stringResource(R.string.training_routine_minutes_question),
            options = listOf(30, 45, 60).map {
                it to stringResource(R.string.training_minutes_option, it)
            },
            selected = form.sessionMinutes,
            onSelected = onSessionMinutesChanged
        )
        RoutineOptionRow(
            label = stringResource(R.string.training_routine_experience_question),
            options = TrainingExperience.entries.map { it to it.localizedLabel() },
            selected = form.experience,
            onSelected = onExperienceChanged
        )
        RoutineOptionRow(
            label = stringResource(R.string.training_routine_feeling_question),
            options = RoutineFeeling.entries.map { it to it.localizedLabel() },
            selected = form.feeling,
            onSelected = onFeelingChanged
        )
    }
}

@Composable
private fun <T> RoutineOptionRow(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowOptions.forEach { (option, optionLabel) ->
                        RoutineFilterChip(
                            selected = option == selected,
                            label = optionLabel,
                            onClick = { onSelected(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowOptions.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SmartTrainnerColors.SurfaceRaised,
            labelColor = SmartTrainnerColors.Muted,
            selectedContainerColor = SmartTrainnerColors.CoralSoft,
            selectedLabelColor = SmartTrainnerColors.Ink
        )
    )
}

@Composable
private fun RoutineLibraryDialog(
    state: TrainingUiState,
    onTemplateSelected: (String) -> Unit,
    onShowRoutineSettings: () -> Unit,
    onCreateCustomRoutine: () -> Unit,
    onCopyTemplateToCustom: (String) -> Unit,
    onEditCustomRoutine: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .heightIn(max = 640.dp)
                .testTag("training_routine_library_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = stringResource(R.string.training_routine_library_title),
                    onDismissRequest = onDismissRequest
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.training_my_routines),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.customTemplates.isEmpty()) {
                        EmptyState(text = stringResource(R.string.training_custom_routine_empty))
                    } else {
                        state.customTemplates.forEach { template ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                PlanTemplateCard(
                                    template = template,
                                    selected = template.id == state.selectedTemplateId,
                                    onClick = { onTemplateSelected(template.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    cardTestTag = "training_custom_template_card"
                                )
                                OutlinedButton(
                                    onClick = { onEditCustomRoutine(template.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("training_edit_custom_template_card"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.size(8.dp))
                                    Text(stringResource(R.string.training_edit_custom_routine))
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = onCreateCustomRoutine,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("training_create_custom_routine_from_library_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.training_create_custom_routine))
                    }
                    Text(
                        text = stringResource(R.string.training_default_routines),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    state.systemTemplates.forEach { template ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PlanTemplateCard(
                                template = template,
                                selected = template.id == state.selectedTemplateId,
                                onClick = { onTemplateSelected(template.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedButton(
                                onClick = { onCopyTemplateToCustom(template.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("training_copy_template_${template.id}"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(stringResource(R.string.training_copy_to_custom_routine))
                            }
                        }
                    }
                }
                Button(
                    onClick = onShowRoutineSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("training_find_recommended_routine_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.training_find_recommended_routine))
                }
            }
        }
    }
}

@Composable
private fun RoutineSettingsDialog(
    form: RoutineRecommendationFormState,
    onDaysPerWeekChanged: (Int) -> Unit,
    onSessionMinutesChanged: (Int) -> Unit,
    onExperienceChanged: (TrainingExperience) -> Unit,
    onFeelingChanged: (RoutineFeeling) -> Unit,
    onShowRecommendations: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .heightIn(max = 640.dp)
                .testTag("training_routine_settings_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = stringResource(R.string.training_routine_settings_title),
                    onDismissRequest = onDismissRequest
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.training_routine_settings_body),
                        color = SmartTrainnerColors.Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    RoutineRecommendationControls(
                        form = form,
                        onDaysPerWeekChanged = onDaysPerWeekChanged,
                        onSessionMinutesChanged = onSessionMinutesChanged,
                        onExperienceChanged = onExperienceChanged,
                        onFeelingChanged = onFeelingChanged
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.training_cancel))
                    }
                    Button(
                        onClick = onShowRecommendations,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_show_recommendations"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.training_show_recommendations))
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineRecommendationsDialog(
    state: TrainingUiState,
    onTemplatePreviewSelected: (String) -> Unit,
    onStartRoutine: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val primary = state.templates.firstOrNull { it.id == state.recommendedTemplateId }
    val alternatives = state.alternativeTemplateIds.mapNotNull { templateId ->
        state.templates.firstOrNull { it.id == templateId }
    }
    val options = listOfNotNull(primary) + alternatives
    val previewTemplate = state.templates.firstOrNull { it.id == state.routinePreviewTemplateId }
        ?: primary
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .heightIn(max = 640.dp)
                .testTag("training_routine_recommendations_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.Surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = stringResource(R.string.training_routine_recommendations_title),
                    onDismissRequest = onDismissRequest
                )
                Text(
                    text = stringResource(
                        R.string.training_routine_recommendations_body,
                        state.routineRecommendationInput.daysPerWeek,
                        state.routineRecommendationInput.sessionMinutes
                    ),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    options.forEachIndexed { index, template ->
                        if (index == 0) {
                            Text(
                                text = stringResource(R.string.training_recommended_routine),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (index == 1) {
                            Text(
                                text = stringResource(R.string.training_alternative_routines),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        PlanTemplateCard(
                            template = template,
                            selected = template.id == previewTemplate?.id,
                            onClick = { onTemplatePreviewSelected(template.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    previewTemplate?.let { template ->
                        RoutinePreviewSchedule(
                            template = template,
                            exercises = state.exercises
                        )
                    }
                }
                Button(
                    onClick = onStartRoutine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("training_start_preview_routine"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.training_start_routine))
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(
    title: String,
    onDismissRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onDismissRequest) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.training_close_detail))
        }
    }
}

@Composable
private fun RoutinePreviewSchedule(
    template: PlanTemplate,
    exercises: List<Exercise>
) {
    val exercisesById = exercises.associateBy { it.id }
    Column(
        modifier = Modifier.testTag("training_routine_preview_${template.id}"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.training_routine_preview_schedule),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        template.days.forEach { day ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SmartTrainnerColors.Line),
                colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = day.previewTitle(template.source),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = day.exercises
                            .mapNotNull { exercisesById[it.exerciseId]?.localizedName() }
                            .joinToString(" · "),
                        color = SmartTrainnerColors.Muted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayProgressLine(state: TrainingUiState) {
    val todaysExercises = state.plan?.days
        ?.firstOrNull { it.date == LocalDate.now() }
        ?.exercises
        .orEmpty()
    val completed = todaysExercises.count { it.id in state.completedPlannedExerciseIds }
    val total = todaysExercises.size
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.training_today_progress, completed, total),
            color = SmartTrainnerColors.Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        RoutineProgressBar(
            progress = if (total == 0) 0f else completed.toFloat() / total,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
private fun RoutineProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = SmartTrainnerColors.Green,
    trackColor: Color = SmartTrainnerColors.Line
) {
    val boundedProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .background(trackColor, RoundedCornerShape(8.dp))
    ) {
        if (boundedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(boundedProgress)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
private fun <T> WrappedChipRows(
    items: List<T>,
    maxItemsPerRow: Int,
    content: @Composable (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(maxItemsPerRow.coerceAtLeast(1)).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    content(item)
                }
            }
        }
    }
}

@Composable
private fun PlanTemplateCard(
    template: PlanTemplate,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardTestTag: String = "training_template_card_${template.id}",
    sourceTestTag: String = template.source.routineSourceTag()
) {
    Card(
        modifier = modifier
            .testTag(cardTestTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) SmartTrainnerColors.Coral else SmartTrainnerColors.Line
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) SmartTrainnerColors.CoralSoft else SmartTrainnerColors.SurfaceRaised
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.localizedName(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (selected) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SmartTrainnerColors.GreenSoft
                    ) {
                        Text(
                            text = stringResource(R.string.training_selected),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            color = SmartTrainnerColors.Green,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            RoutineSourceChip(template.source, sourceTestTag)
            Text(
                text = template.localizedMeta(),
                color = SmartTrainnerColors.Coral,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            if (template.source == RoutineSource.SYSTEM) {
                Text(
                    text = template.structure.localizedLabel(),
                    color = SmartTrainnerColors.Ink,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            RoutineFocusFlow(template)
            Text(
                text = template.localizedDescription(),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RoutineSourceChip(source: RoutineSource, testTag: String = source.routineSourceTag()) {
    Surface(
        modifier = Modifier.testTag(testTag),
        shape = RoundedCornerShape(8.dp),
        color = if (source == RoutineSource.CUSTOM) SmartTrainnerColors.GreenSoft else SmartTrainnerColors.CoralSoft
    ) {
        Text(
            text = stringResource(
                if (source == RoutineSource.CUSTOM) {
                    R.string.training_custom_routine_badge
                } else {
                    R.string.training_default_routine_badge
                }
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            color = if (source == RoutineSource.CUSTOM) SmartTrainnerColors.Green else SmartTrainnerColors.Coral,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun RoutineSource.routineSourceTag(): String = if (this == RoutineSource.CUSTOM) {
    "training_routine_source_custom"
} else {
    "training_routine_source_default"
}

private fun RoutineSource.currentRoutineSourceTag(): String = if (this == RoutineSource.CUSTOM) {
    "training_current_routine_source_custom"
} else {
    "training_current_routine_source_default"
}

private fun RoutineSource.homeRoutineSourceTag(): String = if (this == RoutineSource.CUSTOM) {
    "training_home_routine_source_custom"
} else {
    "training_home_routine_source_default"
}

private fun androidx.compose.foundation.lazy.LazyListScope.exerciseContent(
    state: TrainingUiState,
    onExerciseSelected: (com.smarttrainner.core.model.ExerciseId) -> Unit
) {
    val selected = state.selectedExercise
    item {
        Text(
            text = stringResource(R.string.training_all_exercises, state.exercises.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    if (selected == null) {
        item {
            EmptyState(text = stringResource(R.string.training_select_exercise_hint))
        }
    }
    MuscleGroup.entries
        .filterNot { it in armDetailGroups }
        .forEach { group ->
        val groupExercises = state.exercises.filter {
            if (group == MuscleGroup.ARMS) {
                it.muscleGroup == MuscleGroup.ARMS || it.muscleGroup in armDetailGroups
            } else {
                it.muscleGroup == group
            }
        }
        if (groupExercises.isNotEmpty()) {
            item {
                Text(
                    text = group.localizedLabel(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (group == MuscleGroup.ARMS) {
                armDetailGroups.forEach { armGroup ->
                    val armExercises = groupExercises.filter { it.muscleGroup == armGroup }
                    if (armExercises.isNotEmpty()) {
                        item {
                            Text(
                                text = armGroup.localizedLabel(),
                                color = SmartTrainnerColors.Muted,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(armExercises, key = { it.id.value }) { exercise ->
                            ExerciseRow(
                                exercise = exercise,
                                latestLog = state.latestWorkoutLogs.latestForExercise(exercise.id),
                                selected = exercise.id == state.selectedExerciseId,
                                onClick = { onExerciseSelected(exercise.id) },
                                modifier = Modifier.testTag("training_exercise_row_${exercise.id.value}")
                            )
                        }
                    }
                }
                val uncategorizedArms = groupExercises.filter { it.muscleGroup == MuscleGroup.ARMS }
                items(uncategorizedArms, key = { it.id.value }) { exercise ->
                    ExerciseRow(
                        exercise = exercise,
                        latestLog = state.latestWorkoutLogs.latestForExercise(exercise.id),
                        selected = exercise.id == state.selectedExerciseId,
                        onClick = { onExerciseSelected(exercise.id) },
                        modifier = Modifier.testTag("training_exercise_row_${exercise.id.value}")
                    )
                }
            } else {
                items(groupExercises, key = { it.id.value }) { exercise ->
                    ExerciseRow(
                        exercise = exercise,
                        latestLog = state.latestWorkoutLogs.latestForExercise(exercise.id),
                        selected = exercise.id == state.selectedExerciseId,
                        onClick = { onExerciseSelected(exercise.id) },
                        modifier = Modifier.testTag("training_exercise_row_${exercise.id.value}")
                    )
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.analysisContent(
    state: TrainingUiState
) {
    item {
        SummaryBand(state.summary)
    }
    item {
        Text(
            text = stringResource(R.string.training_muscle_balance),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    val summary = state.summary
    if (summary == null || summary.muscleBalance.isEmpty()) {
        item { EmptyState(text = stringResource(R.string.training_empty_logs)) }
    } else {
        items(summary.muscleBalance.entries.toList(), key = { it.key.name }) { entry ->
            MuscleBalanceRow(
                label = entry.key.localizedLabel(),
                count = entry.value,
                max = summary.muscleBalance.values.maxOrNull() ?: 1
            )
        }
        item {
            InsightCard(text = summary.insight)
        }
    }
}

@Composable
private fun Header(state: TrainingUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .background(SmartTrainnerGradients.brandLight(), RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.training_app_title),
                    modifier = Modifier.testTag("training_app_title"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = SmartTrainnerColors.Ink
                )
                Text(
                    text = state.plan?.localizedName().orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SmartTrainnerColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TrainingBottomBar(
    selectedTab: TrainingTab,
    onTabSelected: (TrainingTab) -> Unit
) {
    NavigationBar(
        containerColor = SmartTrainnerColors.SurfaceRaised,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        TrainingTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.testTag(tab.testTag()),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SmartTrainnerColors.Coral,
                    selectedTextColor = SmartTrainnerColors.Coral,
                    indicatorColor = SmartTrainnerColors.CoralSoft,
                    unselectedIconColor = SmartTrainnerColors.Muted,
                    unselectedTextColor = SmartTrainnerColors.Muted
                ),
                icon = { Icon(tab.icon(), contentDescription = null) },
                label = { Text(tab.label()) }
            )
        }
    }
}

@Composable
private fun TrainingTab.label(): String = when (this) {
    TrainingTab.HOME -> stringResource(R.string.training_tab_home)
    TrainingTab.PLAN -> stringResource(R.string.training_tab_plan)
    TrainingTab.EXERCISES -> stringResource(R.string.training_tab_exercises)
    TrainingTab.ANALYSIS -> stringResource(R.string.training_tab_analysis)
}

private fun TrainingTab.icon(): ImageVector = when (this) {
    TrainingTab.HOME -> Icons.Default.Home
    TrainingTab.PLAN -> Icons.Default.DateRange
    TrainingTab.EXERCISES -> Icons.Default.FitnessCenter
    TrainingTab.ANALYSIS -> Icons.Default.BarChart
}

private fun TrainingTab.testTag(): String = when (this) {
    TrainingTab.HOME -> "training_tab_home"
    TrainingTab.PLAN -> "training_tab_plan"
    TrainingTab.EXERCISES -> "training_tab_exercises"
    TrainingTab.ANALYSIS -> "training_tab_analysis"
}

@Composable
private fun SummaryBand(summary: WeeklySummary?) {
    Card(
        modifier = Modifier.testTag("training_summary_band"),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(SmartTrainnerGradients.brandLight())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.training_week_summary),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    label = stringResource(R.string.training_completion_rate),
                    value = "${summary?.completionRate ?: 0}%",
                    accent = SmartTrainnerColors.Coral,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = stringResource(R.string.training_total_sets),
                    value = "${summary?.totalSets ?: 0}",
                    accent = SmartTrainnerColors.Green,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = stringResource(R.string.training_streak),
                    value = stringResource(R.string.training_days_value, summary?.streakDays ?: 0),
                    accent = SmartTrainnerColors.Amber,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = summary?.localizedInsight() ?: stringResource(R.string.training_empty_logs),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.SurfaceRaised.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = value, color = accent, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, color = SmartTrainnerColors.Muted, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun FeaturedExerciseCard(
    exercise: PlannedExercise,
    displayLog: com.smarttrainner.core.model.WorkoutLog?,
    completed: Boolean,
    onRecordSelected: (PlannedExercise) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = TrainerExerciseImageBackground)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrainerExerciseImage(
                    exercise = exercise.exercise,
                    modifier = Modifier
                        .width(126.dp)
                        .height(140.dp),
                    cleanThumbnailCrop = true
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exercise.exercise.localizedName(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(exercise.localizedTrainingDisplayText(displayLog), color = SmartTrainnerColors.Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                        StatusChip(completed = completed)
                    }
                    Text(exercise.exercise.localizedSummary(), style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = { onRecordSelected(exercise) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("training_home_start_workout")
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.training_start_record))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayPlanSection(
    title: String,
    focus: String,
    primaryFocus: RoutineFocus?,
    dayNumber: Int,
    exercises: List<PlannedExercise>,
    weeklyLogs: List<com.smarttrainner.core.model.WorkoutLog>,
    latestLogs: List<com.smarttrainner.core.model.WorkoutLog>,
    completedIds: Set<com.smarttrainner.core.model.PlannedExerciseId>,
    onRecordSelected: (PlannedExercise) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = planDayScheduleTitle(title, dayNumber),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        val focusText = when {
            primaryFocus != null && (focus.isBlank() || focus == primaryFocus.name) -> {
                primaryFocus.localizedTodayFocusLabel()
            }
            focus.isNotBlank() -> focus.localizedPlanFocus()
            else -> null
        }
        focusText?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = SmartTrainnerColors.Muted)
        }
        exercises.forEach { exercise ->
            PlanExerciseRow(
                exercise = exercise,
                displayLog = weeklyLogs.firstOrNull { it.plannedExerciseId == exercise.id }
                    ?: latestLogs.latestForExercise(exercise.exercise.id),
                completed = exercise.id in completedIds,
                onClick = { onRecordSelected(exercise) }
            )
        }
    }
}

@Composable
private fun PlanExerciseRow(
    exercise: PlannedExercise,
    displayLog: com.smarttrainner.core.model.WorkoutLog?,
    completed: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("training_plan_exercise_${exercise.exercise.id.value}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrainerExerciseImage(
                exercise = exercise.exercise,
                modifier = Modifier.size(width = 76.dp, height = 84.dp),
                cleanThumbnailCrop = true
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.exercise.localizedName(), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(exercise.localizedTrainingDisplayText(displayLog), color = SmartTrainnerColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            if (completed) {
                StatusIcon(completed = true)
            } else {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SmartTrainnerColors.Coral),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("training_plan_record_button")
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.training_record_action), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun ExerciseDetailDialog(
    exercise: Exercise,
    plannedExercise: PlannedExercise?,
    onRecordSelected: (PlannedExercise) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 18.dp)
                .testTag("training_exercise_detail_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.SurfaceRaised
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = exercise.localizedName(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        ExerciseMetaChips(exercise)
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("training_close_exercise_detail")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.training_close_detail))
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExerciseDetailContent(
                        exercise = exercise,
                        plannedExercise = plannedExercise,
                        onRecordSelected = onRecordSelected,
                        showHeader = false,
                        showRecordAction = false
                    )
                }
                if (plannedExercise != null) {
                    Button(
                        onClick = { onRecordSelected(plannedExercise) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                            .testTag("training_detail_start_record")
                    ) {
                        Text(stringResource(R.string.training_start_record))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseDetailCard(
    exercise: Exercise,
    plannedExercise: PlannedExercise?,
    onRecordSelected: (PlannedExercise) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        colors = CardDefaults.cardColors(containerColor = SmartTrainnerColors.SurfaceRaised)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExerciseDetailContent(
                exercise = exercise,
                plannedExercise = plannedExercise,
                onRecordSelected = onRecordSelected
            )
        }
    }
}

@Composable
private fun ExerciseDetailContent(
    exercise: Exercise,
    plannedExercise: PlannedExercise?,
    onRecordSelected: (PlannedExercise) -> Unit,
    showHeader: Boolean = true,
    showRecordAction: Boolean = true
) {
    var imageViewerTarget by remember(exercise.id) {
        mutableStateOf<ExerciseImageViewerTarget?>(null)
    }
    imageViewerTarget?.let { target ->
        ExerciseImageViewerDialog(
            exercise = exercise,
            stepIndex = target.stepIndex,
            onDismissRequest = { imageViewerTarget = null }
        )
    }
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TrainerExerciseImage(
            exercise = exercise,
            modifier = Modifier
                .width(216.dp)
                .height(240.dp)
                .clickable { imageViewerTarget = ExerciseImageViewerTarget(stepIndex = null) }
                .testTag("training_detail_main_image"),
            cleanThumbnailCrop = true,
            contentDescription = exercise.localizedName()
        )
    }
    if (showHeader) {
        Text(exercise.localizedName(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        ExerciseMetaChips(exercise)
    }
    Text(exercise.localizedSummary(), style = MaterialTheme.typography.bodyMedium)
    StepImageSection(
        title = stringResource(R.string.training_instruction),
        exercise = exercise,
        onImageSelected = { stepIndex ->
            imageViewerTarget = ExerciseImageViewerTarget(stepIndex = stepIndex)
        }
    )
    BulletSection(title = stringResource(R.string.training_safety), bullets = exercise.localizedSafetyCues())
    if (plannedExercise != null && showRecordAction) {
        Button(
            onClick = { onRecordSelected(plannedExercise) },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_detail_start_record")
        ) {
            Text(stringResource(R.string.training_start_record))
        }
    }
}

private data class ExerciseImageViewerTarget(
    val stepIndex: Int?
)

@Composable
private fun ExerciseImageViewerDialog(
    exercise: Exercise,
    stepIndex: Int?,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .padding(horizontal = 12.dp)
                .testTag("training_exercise_image_viewer"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.SurfaceRaised
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.localizedName(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (stepIndex == null) {
                                stringResource(R.string.training_image_viewer_title)
                            } else {
                                "${stringResource(R.string.training_image_viewer_title)} ${stepIndex + 1}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SmartTrainnerColors.Muted
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("training_close_exercise_image_viewer")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.training_close_image_viewer)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TrainerExerciseImage(
                        exercise = exercise,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("training_exercise_image_viewer_image"),
                        stepIndex = stepIndex,
                        contentDescription = if (stepIndex == null) {
                            exercise.localizedName()
                        } else {
                            "${exercise.localizedName()} ${stepIndex + 1}"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseMetaChips(exercise: Exercise) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(exercise.muscleGroup.localizedLabel()) })
            AssistChip(onClick = {}, label = { Text(exercise.equipment.localizedLabel()) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(exercise.difficulty.localizedLabel()) })
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    latestLog: com.smarttrainner.core.model.WorkoutLog?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) SmartTrainnerColors.CoralSoft else SmartTrainnerColors.SurfaceRaised,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) SmartTrainnerColors.Coral else SmartTrainnerColors.Line
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrainerExerciseImage(
                exercise = exercise,
                modifier = Modifier.size(width = 72.dp, height = 80.dp),
                cleanThumbnailCrop = true
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.localizedName(), fontWeight = FontWeight.Bold)
                Text(
                    text = exercise.localizedTrainingDisplayText(latestLog),
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartTrainnerColors.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = exercise.equipment.localizedLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SmartTrainnerColors.Muted
                )
            }
        }
    }
}

@Composable
private fun RecordDialog(
    state: TrainingUiState,
    planned: PlannedExercise,
    onSetRepsChanged: (Int, String) -> Unit,
    onSetWeightChanged: (Int, String) -> Unit,
    onSetDurationChanged: (Int, String) -> Unit,
    onSetRestChanged: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onMemoChanged: (String) -> Unit,
    onSaveRecord: () -> Unit,
    onExerciseMethodSelected: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .padding(horizontal = 18.dp)
                .testTag("training_record_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.SurfaceRaised
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 14.dp, top = 38.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.training_record_dialog_title, planned.exercise.localizedName()),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    RecordForm(
                        state = state,
                        planned = planned,
                        onSetRepsChanged = onSetRepsChanged,
                        onSetWeightChanged = onSetWeightChanged,
                        onSetDurationChanged = onSetDurationChanged,
                        onSetRestChanged = onSetRestChanged,
                        onAddSet = onAddSet,
                        onRemoveSet = onRemoveSet,
                        onMemoChanged = onMemoChanged,
                        onSaveRecord = onSaveRecord,
                        onExerciseMethodSelected = onExerciseMethodSelected
                    )
                }
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.training_close_record))
                }
            }
        }
    }
}

@Composable
private fun RecordForm(
    state: TrainingUiState,
    planned: PlannedExercise,
    onSetRepsChanged: (Int, String) -> Unit,
    onSetWeightChanged: (Int, String) -> Unit,
    onSetDurationChanged: (Int, String) -> Unit,
    onSetRestChanged: (Int, String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onMemoChanged: (String) -> Unit,
    onSaveRecord: () -> Unit,
    onExerciseMethodSelected: () -> Unit
) {
    val showReps = planned.repRange != null
    val showDuration = planned.durationMinutes != null || !showReps
    val showWeight = showReps
    val displayLog = state.logs.firstOrNull { it.plannedExerciseId == planned.id }
        ?: state.latestWorkoutLogs.latestForExercise(planned.exercise.id)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.testTag("training_record_selected_exercise"),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrainerExerciseImage(
                exercise = planned.exercise,
                modifier = Modifier.size(width = 78.dp, height = 86.dp),
                cleanThumbnailCrop = true
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(planned.localizedTrainingDisplayText(displayLog), color = SmartTrainnerColors.Muted)
            }
        }
        OutlinedButton(
            onClick = onExerciseMethodSelected,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_show_exercise_method")
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.training_show_exercise_method))
        }
        state.formError?.let { error ->
            Text(
                text = error.message(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = stringResource(R.string.training_set_entries_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        state.recordForm.setEntries.forEachIndexed { index, setEntry ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.training_set_number, index + 1),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onRemoveSet(index) },
                        enabled = state.recordForm.setEntries.size > 1,
                        modifier = Modifier
                            .testTag("training_remove_set_button_$index")
                    ) {
                        Icon(
                            Icons.Default.RemoveCircleOutline,
                            contentDescription = stringResource(R.string.training_remove_set)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showReps) {
                        NumberField(
                            label = stringResource(R.string.training_reps),
                            value = setEntry.reps,
                            onValueChange = { onSetRepsChanged(index, it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("training_set_reps_input_$index")
                        )
                    }
                    if (showWeight) {
                        NumberField(
                            label = stringResource(R.string.training_weight_short),
                            value = setEntry.weightKg,
                            onValueChange = { onSetWeightChanged(index, it) },
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("training_set_weight_input_$index")
                        )
                    }
                    if (showDuration) {
                        NumberField(
                            label = stringResource(R.string.training_duration),
                            value = setEntry.durationMinutes,
                            onValueChange = { onSetDurationChanged(index, it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("training_set_duration_input_$index")
                        )
                    }
                    NumberField(
                        label = stringResource(R.string.training_rest_seconds),
                        value = setEntry.restSeconds,
                        onValueChange = { onSetRestChanged(index, it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("training_set_rest_input_$index")
                    )
                }
            }
        }
        OutlinedButton(
            onClick = onAddSet,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_add_set_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.training_add_set))
        }
        Button(
            onClick = onSaveRecord,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("training_save_record"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.training_save_record))
        }
        if (state.recordSaved) {
            Text(
                text = stringResource(R.string.training_saved),
                color = SmartTrainnerColors.Green,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("training_record_saved_message")
            )
        }
        OutlinedTextField(
            value = state.recordForm.memo,
            onValueChange = onMemoChanged,
            label = { Text(stringResource(R.string.training_memo)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 1,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    )
}

@Composable
private fun LogRow(
    exercise: Exercise,
    log: com.smarttrainner.core.model.WorkoutLog
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.SurfaceRaised,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIcon(completed = log.completed)
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.localizedName(), fontWeight = FontWeight.Bold)
                Text(
                    text = log.localizedRecordDisplayText(),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MuscleBalanceRow(
    label: String,
    count: Int,
    max: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.training_count_value, count), color = SmartTrainnerColors.Muted)
        }
        RoutineProgressBar(
            progress = count.toFloat() / max.coerceAtLeast(1),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
private fun InsightCard(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = SmartTrainnerColors.AmberSoft) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = SmartTrainnerColors.Ink
        )
    }
}

@Composable
private fun BulletSection(
    title: String,
    bullets: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        bullets.forEach { bullet ->
            Text("• $bullet", style = MaterialTheme.typography.bodyMedium, color = SmartTrainnerColors.Muted)
        }
    }
}

private data class LocalizedExerciseStep(
    val label: String,
    val instruction: String
)

@Composable
private fun StepImageSection(
    title: String,
    exercise: Exercise,
    onImageSelected: (Int) -> Unit = {}
) {
    val stepItems = exercise.localizedStepItems()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        stepItems.forEachIndexed { index, step ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SmartTrainnerColors.Surface,
                border = BorderStroke(1.dp, SmartTrainnerColors.Line)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrainerExerciseImage(
                        exercise = exercise,
                        modifier = Modifier
                            .size(width = 76.dp, height = 84.dp)
                            .clickable { onImageSelected(index) }
                            .testTag("training_step_image_$index"),
                        stepIndex = index,
                        contentDescription = "${exercise.localizedName()} ${index + 1}. ${step.label}"
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${index + 1}. ${step.label}",
                            style = MaterialTheme.typography.labelLarge,
                            color = SmartTrainnerColors.Coral,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = step.instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SmartTrainnerColors.Ink
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(completed: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (completed) SmartTrainnerColors.GreenSoft else SmartTrainnerColors.SteelSoft
    ) {
        Text(
            text = stringResource(if (completed) R.string.training_completed else R.string.training_incomplete),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = if (completed) SmartTrainnerColors.Green else SmartTrainnerColors.Muted,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusIcon(completed: Boolean) {
    Icon(
        imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
        contentDescription = stringResource(
            if (completed) R.string.training_completed else R.string.training_incomplete
        ),
        tint = if (completed) SmartTrainnerColors.Green else SmartTrainnerColors.Muted
    )
}

@Composable
private fun SectionTitle(stringResourceId: Int, testTag: String) {
    Text(
        text = stringResource(stringResourceId),
        modifier = Modifier.testTag(testTag),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SmartTrainnerColors.SurfaceRaised, RoundedCornerShape(8.dp))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = SmartTrainnerColors.Muted)
    }
}

@Composable
private fun RecordFormError.message(): String = when (this) {
    RecordFormError.SELECT_EXERCISE -> stringResource(R.string.training_error_select)
    RecordFormError.SETS -> stringResource(R.string.training_error_sets)
    RecordFormError.REPS -> stringResource(R.string.training_error_reps)
    RecordFormError.WEIGHT -> stringResource(R.string.training_error_weight)
    RecordFormError.DURATION -> stringResource(R.string.training_error_duration)
    RecordFormError.REST -> stringResource(R.string.training_error_rest)
    RecordFormError.SAVE_FAILED -> stringResource(R.string.training_error_save)
    RecordFormError.COMPLETE_DAY_FAILED -> stringResource(R.string.training_error_complete_day)
}

@Composable
private fun LocalDate.dayOfWeekShort(): String =
    if (isKoreanLocale()) {
        when (dayOfWeek.value) {
            1 -> "월"
            2 -> "화"
            3 -> "수"
            4 -> "목"
            5 -> "금"
            6 -> "토"
            else -> "일"
        }
    } else {
        dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    }

@Composable
private fun isKoreanLocale(): Boolean =
    LocalConfiguration.current.locales[0].language.equals("ko", ignoreCase = true)

@Composable
private fun Exercise.localizedName(): String =
    if (isKoreanLocale()) name else id.value.toExerciseTitle()

@Composable
private fun Exercise.localizedSummary(): String =
    if (isKoreanLocale()) {
        summary
    } else {
        stringResource(
            R.string.training_exercise_summary_template,
            localizedName(),
            muscleGroup.localizedLabel().lowercase(Locale.ENGLISH),
            equipment.localizedLabel().lowercase(Locale.ENGLISH)
        )
    }

@Composable
private fun Exercise.localizedInstructions(): List<String> =
    localizedStepItems().map { it.instruction }

@Composable
private fun Exercise.localizedStepItems(): List<LocalizedExerciseStep> {
    val visuals = exerciseStepVisuals(id.value)
    if (visuals.isNotEmpty()) {
        val isKo = isKoreanLocale()
        if (id.value in GENERATED_EXERCISE_TEXT_BACKED_IDS) {
            val copy = if (isKo) {
                koreanSeedStepItems()
            } else {
                generatedEnglishStepItems(id.value)
            }
            return visuals.mapIndexed { index, visual ->
                copy.getOrNull(index) ?: localizedExerciseStep(
                    label = if (isKo) visual.koLabel else visual.enLabel,
                    instruction = if (isKo) visual.koInstruction else visual.enInstruction
                )
            }
        }
        return visuals.map { visual ->
            localizedExerciseStep(
                label = if (isKo) visual.koLabel else visual.enLabel,
                instruction = if (isKo) visual.koInstruction else visual.enInstruction
            )
        }
    }

    if (!isKoreanLocale()) {
        kettlebellEnglishStepItems(id.value)?.let { return it }
    }

    return if (isKoreanLocale()) {
        instructions.mapIndexed { index, instruction ->
            val label = stringResource(R.string.training_step_number, index + 1)
            localizedExerciseStep(label = label, instruction = instruction)
        }
    } else {
        listOf(
            LocalizedExerciseStep(
                label = stringResource(R.string.training_step_number, 1),
                instruction = stringResource(R.string.training_instruction_setup_template, localizedName())
            ),
            LocalizedExerciseStep(
                label = stringResource(R.string.training_step_number, 2),
                instruction = stringResource(R.string.training_instruction_move_template)
            ),
            LocalizedExerciseStep(
                label = stringResource(R.string.training_step_number, 3),
                instruction = stringResource(R.string.training_instruction_return_template)
            )
        )
    }
}

private fun Exercise.koreanSeedStepItems(): List<LocalizedExerciseStep> =
    instructions.mapIndexed { index, instruction ->
        val label = instruction.substringBefore(":").takeIf { it != instruction }
            ?: instruction.substringBefore("：").takeIf { it != instruction }
            ?: "${index + 1}단계"
        localizedExerciseStep(label = label, instruction = instruction)
    }

private fun localizedExerciseStep(label: String, instruction: String): LocalizedExerciseStep =
    LocalizedExerciseStep(
        label = label,
        instruction = instructionWithoutRepeatedStepTitle(label, instruction)
    )

private fun generatedEnglishStepItems(exerciseId: String): List<LocalizedExerciseStep> =
    kettlebellEnglishStepItems(exerciseId) ?: when (exerciseId) {
        "dead_bug" -> listOf(
            LocalizedExerciseStep("Start tabletop", "Lie on your back, reach both arms over the shoulders, and keep hips and knees bent at 90 degrees."),
            LocalizedExerciseStep("Diagonal reach A", "Extend one arm overhead and the opposite leg toward the floor while the other arm and leg stay in tabletop."),
            LocalizedExerciseStep("Return tabletop", "Bring the moving arm and leg back slowly without lifting the lower back."),
            LocalizedExerciseStep("Diagonal reach B", "Repeat with the opposite arm and opposite leg, keeping the ribs down and pelvis quiet.")
        )
        else -> emptyList()
    }

@Composable
private fun Exercise.localizedSafetyCues(): List<String> =
    if (isKoreanLocale()) {
        safetyCues
    } else {
        kettlebellEnglishSafetyCues(id.value) ?:
        listOf(
            stringResource(R.string.training_safety_pain),
            stringResource(R.string.training_safety_control)
        )
    }

private fun kettlebellEnglishStepItems(exerciseId: String): List<LocalizedExerciseStep>? {
    fun steps(vararg items: Pair<String, String>) = items.map { (label, instruction) ->
        LocalizedExerciseStep(label = label, instruction = instruction)
    }
    return when (exerciseId) {
        "kettlebell_deadlift" -> steps(
            "Bell between feet" to "Place the kettlebell under your midline and press the whole foot into the floor.",
            "Hinge down" to "Slightly bend the knees, send the hips back, and keep the back neutral.",
            "Grip and brace" to "Pack the shoulders away from the ears and hold the handle firmly with both hands.",
            "Stand tall" to "Drive through the floor and keep the bell close as the hips and legs stand you up."
        )
        "kettlebell_romanian_deadlift" -> steps(
            "Tall start" to "Hold the kettlebell with both hands in front of the thighs and stand tall.",
            "Soft knees" to "Keep a small knee bend while bracing the trunk.",
            "Hips back" to "Slide the bell close to the legs until the hamstrings are loaded.",
            "Return" to "Press the floor away and squeeze the glutes to stand tall."
        )
        "kettlebell_sumo_deadlift" -> steps(
            "Wide stance" to "Open the toes slightly and center the kettlebell between the feet.",
            "Track knees" to "Keep the knees moving in the same direction as the toes.",
            "Tall grip" to "Reach down with a neutral spine and grip the handle.",
            "Vertical lift" to "Stand by pushing through the legs so the bell moves straight up and down."
        )
        "kettlebell_goblet_squat" -> steps(
            "Chest hold" to "Hold the kettlebell close to the chest with elbows near the body.",
            "Stack ribs" to "Brace the abs so the ribs stay stacked over the pelvis.",
            "Squat" to "Lower to a controlled depth with knees tracking over toes.",
            "Stand" to "Drive through the whole foot and keep the bell close as you stand."
        )
        "kettlebell_box_squat" -> steps(
            "Set box" to "Choose a stable box or bench height that allows a pain-free squat.",
            "Goblet hold" to "Hold the kettlebell close to the chest and set the feet.",
            "Light touch" to "Touch the box lightly without relaxing or collapsing onto it.",
            "Stand without bounce" to "Keep tension and stand by pressing through the whole foot."
        )
        "kettlebell_reverse_lunge" -> steps(
            "Choose hold" to "Hold the bell goblet-style or suitcase-style and stand tall.",
            "Right foot back" to "Step the right foot back while keeping weight over the front foot.",
            "Lower" to "Lower vertically with the front knee tracking over the toes.",
            "Alternate sides" to "Return to the start and repeat with the left foot for the same rep target."
        )
        "kettlebell_split_squat" -> steps(
            "Set stance" to "Fix a split stance and hold the kettlebell securely.",
            "Brace" to "Keep the pelvis square and the trunk quiet.",
            "Lower" to "Lower straight down while keeping the front foot grounded.",
            "Switch sides" to "Drive through the front foot, finish the set, then repeat the same reps on the other side."
        )
        "kettlebell_step_up" -> steps(
            "Check box" to "Use a stable box that is not too high for your hip and knee.",
            "Full foot" to "Place the full right foot on the box while the kettlebell stays quiet.",
            "Stand up" to "Drive through the top foot without bouncing from the floor leg.",
            "Step down" to "Step down under control and repeat the same reps on the other side."
        )
        "kettlebell_bent_over_row" -> steps(
            "Hinge" to "Hinge at the hips with a neutral spine.",
            "Bell under shoulder" to "Let the kettlebell hang below the shoulder without twisting.",
            "Row" to "Pull the elbow back toward the ribs.",
            "Lower" to "Lower the bell under control without swinging the torso."
        )
        "one_arm_kettlebell_row" -> steps(
            "Bench support" to "Support one hand and knee or foot on a bench.",
            "Square torso" to "Keep the shoulders and hips facing the floor.",
            "Pull" to "Row the kettlebell toward the ribs.",
            "Repeat other side" to "Lower without rotation, then complete the same reps on the other side."
        )
        "kettlebell_floor_press" -> steps(
            "Lie down" to "Lie on the floor with knees bent and the kettlebell stacked over the wrist.",
            "Set elbow" to "Find a comfortable elbow angle slightly away from the torso.",
            "Press" to "Press the kettlebell toward the ceiling while keeping the wrist straight.",
            "Switch sides" to "Lower until the elbow lightly touches the floor, then repeat the same reps on the other side."
        )
        "kettlebell_shoulder_press" -> steps(
            "Rack" to "Start in the rack position with a straight wrist and elbow near the body.",
            "Brace" to "Brace the abs and glutes so the ribs do not flare.",
            "Press" to "Press overhead without leaning back.",
            "Switch sides" to "Lower to the rack position, then repeat the same reps on the other side."
        )
        "half_kneeling_kettlebell_press" -> steps(
            "Half kneel" to "Set a half-kneeling stance with the pelvis square.",
            "Rack" to "Hold the kettlebell in a stable rack position.",
            "Press" to "Press overhead while the trunk stays vertical.",
            "Switch sides" to "Lower under control and repeat the same reps on the other side."
        )
        "kettlebell_halo" -> steps(
            "Light bell" to "Hold a light kettlebell upside down in front of the chest.",
            "Circle head" to "Circle it slowly around the head while the torso stays forward.",
            "Reverse direction" to "Complete the same number of reps in the opposite direction."
        )
        "kettlebell_suitcase_carry" -> steps(
            "Pick up" to "Pick up one kettlebell and stand tall.",
            "Stay vertical" to "Keep the ribs and pelvis stacked without leaning toward the bell.",
            "Walk" to "Walk with short controlled steps while the bell stays quiet.",
            "Switch sides" to "Set the bell down safely and repeat the same time or distance on the other side."
        )
        "kettlebell_farmer_carry" -> steps(
            "Set bells" to "Set two similar kettlebells beside the feet.",
            "Pick up" to "Hinge down and pick them up with a tall posture.",
            "Walk" to "Walk steadily without letting the bells swing.",
            "Set down" to "Hinge again and place the bells down quietly."
        )
        "kettlebell_rack_carry" -> steps(
            "Rack" to "Create a rack position with a straight wrist and the bell resting on the forearm.",
            "Brace" to "Keep the elbow close and the ribs down.",
            "Walk" to "Walk slowly while maintaining steady breathing.",
            "Switch sides" to "Set the bell down safely and repeat on the other side."
        )
        "two_hand_kettlebell_swing" -> steps(
            "Set bell" to "Place the bell in front of the feet and hinge to grip it with both hands.",
            "Hike pass" to "Hike the bell back between the legs to load the hamstrings.",
            "Hip snap" to "Snap the hips forward so the bell floats to chest height.",
            "Receive" to "Receive the bell by hinging again and continue with the same rhythm."
        )
        else -> null
    }
}

private fun kettlebellEnglishSafetyCues(exerciseId: String): List<String>? = when (exerciseId) {
    "kettlebell_deadlift" -> listOf("Raise the bell on a box if the lower back rounds.", "Do not pull the bell from far in front of the body.")
    "kettlebell_romanian_deadlift" -> listOf("Prioritize a neutral back over depth.", "Lower the load if the shoulders round forward.")
    "kettlebell_sumo_deadlift" -> listOf("Narrow the stance if the knees collapse inward.", "Do not start the lift by yanking with the lower back.")
    "kettlebell_goblet_squat" -> listOf("Reduce depth if the lower back rounds.", "Lower the weight if the bell drifts away from the chest.")
    "kettlebell_box_squat" -> listOf("Do not collapse onto the box.", "Lower the load if the torso folds forward.")
    "kettlebell_reverse_lunge" -> listOf("Regress to bodyweight if balance is unstable.", "Shorten the step or depth if the front knee hurts.")
    "kettlebell_split_squat" -> listOf("Adjust stance if the front knee collapses inward.", "Start light until balance is reliable.")
    "kettlebell_step_up" -> listOf("Do not use the floor leg to bounce upward.", "Stop if the box shifts or feels unstable.")
    "kettlebell_bent_over_row" -> listOf("Do not swing the torso to move the bell.", "Use a supported row if the hinge bothers the back.")
    "one_arm_kettlebell_row" -> listOf("Do not twist open to finish the rep.", "Keep the wrist neutral.")
    "kettlebell_floor_press" -> listOf("Do not let the bell pull the wrist backward.", "Reduce range if the front of the shoulder pinches.")
    "kettlebell_shoulder_press" -> listOf("Do not turn the press into a backbend.", "Use a lighter load or machine if the shoulder hurts.")
    "half_kneeling_kettlebell_press" -> listOf("Use padding if the knee is uncomfortable.", "Lower the load if the ribs flare.")
    "kettlebell_halo" -> listOf("Keep the load light and the motion smooth.", "Do not crane the neck or arch the lower back.")
    "kettlebell_suitcase_carry" -> listOf("Lower the load if the body leans toward the bell.", "Do not arch the lower back while walking.")
    "kettlebell_farmer_carry" -> listOf("Lower the load if the shoulders round forward.", "Do not round the back when setting the bells down.")
    "kettlebell_rack_carry" -> listOf("Re-rack if the wrist folds.", "Do not lean back to support the load.")
    "two_hand_kettlebell_swing" -> listOf("Do not squat the swing.", "Stop if the lower back is doing the work.", "Use this only after the hinge pattern is consistent.")
    else -> null
}

@Composable
private fun Exercise.localizedTargetText(): String {
    val reps = defaultRepRange
    return if (reps != null) {
        stringResource(
            R.string.training_target_reps,
            defaultSets,
            reps.first,
            reps.last
        )
    } else {
        stringResource(R.string.training_target_duration, defaultSets, defaultDurationMinutes ?: 10)
    }
}

@Composable
private fun Exercise.localizedTrainingDisplayText(latestLog: WorkoutLog?): String =
    latestLog?.localizedRecordDisplayText()?.let { recordText ->
        stringResource(R.string.training_latest_record, recordText)
    } ?: stringResource(R.string.training_recommended_record, localizedTargetText())

@Composable
private fun PlannedExercise.localizedTargetText(): String {
    val reps = repRange
    return if (reps != null) {
        stringResource(R.string.training_target_reps, sets, reps.first, reps.last)
    } else {
        stringResource(R.string.training_target_duration, sets, durationMinutes ?: 10)
    }
}

@Composable
private fun PlannedExercise.localizedTrainingDisplayText(latestLog: WorkoutLog?): String =
    latestLog?.localizedRecordDisplayText()?.let { recordText ->
        stringResource(R.string.training_latest_record, recordText)
    } ?: stringResource(
        R.string.training_recommended_record,
        listOf(
            localizedTargetText(),
            stringResource(R.string.training_rest, restSeconds)
        ).joinToString(" · ")
    )

@Composable
private fun WorkoutLog.localizedRecordDisplayText(): String {
    val entries = displaySetEntries()
    val reps = entries.mapNotNull { it.reps }.toCollapsedText()
    val weights = entries.mapNotNull { it.weightKg }.map { it.toRecordInput() }.toCollapsedText()
    val durations = entries.mapNotNull { it.durationMinutes }.toCollapsedText()
    val rests = entries.mapNotNull { it.restSeconds }.toCollapsedText()
    val parts = buildList {
        add(stringResource(R.string.training_set_number, entries.size.coerceAtLeast(sets)))
        reps?.let { add(stringResource(R.string.training_actual_reps, it)) }
        weights?.let { add(stringResource(R.string.training_actual_weight, it)) }
        durations?.let { add(stringResource(R.string.training_actual_duration, it)) }
        rests?.let { add(stringResource(R.string.training_actual_rest, it)) }
    }
    return parts.joinToString(" · ")
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
    firstOrNull { it.exerciseId == exerciseId }
        ?: filter { it.exerciseId == exerciseId }.maxByOrNull { it.performedAt }

@Composable
private fun WeeklyPlan.localizedName(): String =
    routineTemplateNameResource(templateId)?.let { stringResource(it) } ?: name

@Composable
private fun PlanTemplate.localizedName(): String =
    routineTemplateNameResource(id)?.let { stringResource(it) }
        ?: if (isKoreanLocale()) name else id.toExerciseTitle()

@Composable
private fun PlanTemplate.localizedDescription(): String =
    routineTemplateDescriptionResource(id)?.let { stringResource(it) } ?: description

@StringRes
private fun routineTemplateNameResource(templateId: String): Int? = when (templateId) {
    "beginner-full-body-2day" -> R.string.training_template_beginner_full_body_2day_name
    "beginner-full-body-3day" -> R.string.training_template_beginner_full_body_3day_name
    "intermediate-balanced-4day" -> R.string.training_template_intermediate_balanced_4day_name
    "intermediate-body-part-4day-30" -> R.string.training_template_intermediate_body_part_4day_30_name
    "intermediate-body-part-4day" -> R.string.training_template_intermediate_body_part_4day_name
    "intermediate-body-part-4day-60" -> R.string.training_template_intermediate_body_part_4day_60_name
    "intermediate-body-part-5day" -> R.string.training_template_intermediate_body_part_5day_name
    else -> null
}

@StringRes
private fun routineTemplateDescriptionResource(templateId: String): Int? = when (templateId) {
    "beginner-full-body-2day" -> R.string.training_template_beginner_full_body_2day_description
    "beginner-full-body-3day" -> R.string.training_template_beginner_full_body_3day_description
    "intermediate-balanced-4day" -> R.string.training_template_intermediate_balanced_4day_description
    "intermediate-body-part-4day-30" -> R.string.training_template_intermediate_body_part_4day_30_description
    "intermediate-body-part-4day" -> R.string.training_template_intermediate_body_part_4day_description
    "intermediate-body-part-4day-60" -> R.string.training_template_intermediate_body_part_4day_60_description
    "intermediate-body-part-5day" -> R.string.training_template_intermediate_body_part_5day_description
    else -> null
}

@Composable
private fun String.localizedPlanDayTitle(): String =
    generatedKoreanDayNumber()?.let { dayNumber -> stringResource(R.string.training_day_label, dayNumber) }
        ?: routineDayTitleResource(this)?.let { stringResource(it) }
        ?: this

@Composable
private fun planDayDisplayTitle(title: String, dayNumber: Int): String =
    if (title.isBlank() || title.generatedKoreanDayNumber() == dayNumber) {
        stringResource(R.string.training_day_label, dayNumber)
    } else {
        title.localizedPlanDayTitle()
    }

@Composable
private fun planDayScheduleTitle(title: String, dayNumber: Int): String =
    if (title.isBlank() || title.generatedKoreanDayNumber() == dayNumber) {
        stringResource(R.string.training_day_label, dayNumber)
    } else {
        stringResource(
            R.string.training_routine_schedule_day_title,
            dayNumber,
            title.localizedPlanDayTitle()
        )
    }

private fun String.hasMeaningfulPlanDayTitle(dayNumber: Int): Boolean =
    isNotBlank() && generatedKoreanDayNumber() != dayNumber

private fun String.generatedKoreanDayNumber(): Int? {
    val trimmed = trim()
    if (!trimmed.endsWith("일차")) return null
    return trimmed.removeSuffix("일차").toIntOrNull()?.takeIf { it > 0 }
}

@StringRes
private fun routineDayTitleResource(title: String): Int? = when (title) {
    "입문 전신 A" -> R.string.training_day_title_starter_full_body_a
    "입문 전신 B" -> R.string.training_day_title_starter_full_body_b
    "전신 A" -> R.string.training_day_title_full_body_a
    "전신 B" -> R.string.training_day_title_full_body_b
    "전신 C" -> R.string.training_day_title_full_body_c
    "상체 1" -> R.string.training_day_title_upper_1
    "하체 1" -> R.string.training_day_title_lower_1
    "상체 2" -> R.string.training_day_title_upper_2
    "하체 2" -> R.string.training_day_title_lower_2
    "등 집중" -> R.string.training_day_title_back_focus
    "가슴 집중" -> R.string.training_day_title_chest_focus
    "하체 집중" -> R.string.training_day_title_lower_body_focus
    "어깨+팔 집중" -> R.string.training_day_title_shoulders_arms_focus
    "어깨 집중" -> R.string.training_day_title_shoulders_focus
    "팔+유산소" -> R.string.training_day_title_arms_cardio
    else -> null
}

@Composable
private fun String.localizedPlanFocus(): String =
    routineDayFocusResource(this)?.let { stringResource(it) } ?: this

@StringRes
private fun routineDayFocusResource(focus: String): Int? = when (focus) {
    "기구 적응" -> R.string.training_day_focus_machine_basics
    "균형과 후면" -> R.string.training_day_focus_balance_posterior_chain
    "하체+밀기+당기기" -> R.string.training_day_focus_lower_push_pull
    "스쿼트 패턴과 등" -> R.string.training_day_focus_squat_pattern_back
    "어깨와 엉덩이" -> R.string.training_day_focus_shoulders_glutes
    "기본 프레스와 로우" -> R.string.training_day_focus_core_presses_rows
    "상체 균형" -> R.string.training_day_focus_upper_balance
    "프레스와 힙힌지" -> R.string.training_day_focus_press_hip_hinge
    "인클라인과 후면" -> R.string.training_day_focus_incline_posterior
    "스쿼트와 둔근" -> R.string.training_day_focus_squat_glutes
    "당기는 운동" -> R.string.training_day_focus_pulling_workout
    "미는 운동" -> R.string.training_day_focus_pushing_workout
    "스쿼트와 힙힌지" -> R.string.training_day_focus_squat_hip_hinge
    "측면·후면 어깨와 팔" -> R.string.training_day_focus_side_rear_delts_arms
    "측면·후면 어깨와 이두·삼두" -> R.string.training_day_focus_side_rear_delts_biceps_triceps
    "프레스와 플라이" -> R.string.training_day_focus_presses_flyes
    "수직·수평 당기기" -> R.string.training_day_focus_vertical_horizontal_pulling
    "스쿼트와 후면" -> R.string.training_day_focus_squat_posterior
    "측면·후면 우선" -> R.string.training_day_focus_side_rear_delts_first
    "팔 보조와 컨디셔닝" -> R.string.training_day_focus_arm_accessories_conditioning
    "이두·삼두 보조와 컨디셔닝" -> R.string.training_day_focus_biceps_triceps_conditioning
    else -> null
}

@Composable
private fun WeeklySummary.localizedInsight(): String {
    if (isKoreanLocale()) return insight
    val weakestMuscle = MuscleGroup.entries
        .filterNot {
            it == MuscleGroup.CARDIO ||
                it == MuscleGroup.ARMS ||
                it == MuscleGroup.FULL_BODY
        }
        .minByOrNull { muscleBalance[it] ?: 0 }
    return when {
        plannedExerciseCount == 0 -> stringResource(R.string.training_insight_empty_plan)
        completedExerciseCount == 0 -> stringResource(R.string.training_insight_no_logs)
        completionRate >= 80 -> stringResource(R.string.training_insight_good_rate)
        totalVolumeKg > 0 && weakestMuscle != null -> stringResource(
            R.string.training_insight_balance,
            weakestMuscle.localizedLabel()
        )
        else -> stringResource(R.string.training_insight_steady)
    }
}

@Composable
private fun RoutineStructure.localizedLabel(): String = stringResource(
    when (this) {
        RoutineStructure.FULL_BODY -> R.string.training_routine_structure_full_body
        RoutineStructure.BALANCED_SPLIT -> R.string.training_routine_structure_balanced_split
        RoutineStructure.BODY_PART_SPLIT -> R.string.training_routine_structure_body_part_split
    }
)

@Composable
private fun RoutineFocus.localizedShortLabel(): String = stringResource(
    when (this) {
        RoutineFocus.FULL_BODY -> R.string.training_muscle_full_body
        RoutineFocus.UPPER_BODY -> R.string.training_muscle_upper_body
        RoutineFocus.PUSH -> R.string.training_muscle_push
        RoutineFocus.PULL -> R.string.training_muscle_pull
        RoutineFocus.CHEST -> R.string.training_muscle_chest
        RoutineFocus.BACK -> R.string.training_muscle_back
        RoutineFocus.LOWER_BODY -> R.string.training_muscle_lower_body
        RoutineFocus.SHOULDERS -> R.string.training_muscle_shoulders
        RoutineFocus.ARMS -> R.string.training_muscle_arms
        RoutineFocus.BICEPS -> R.string.training_muscle_biceps
        RoutineFocus.TRICEPS -> R.string.training_muscle_triceps
        RoutineFocus.FOREARMS -> R.string.training_muscle_forearms
        RoutineFocus.CARDIO_CONDITIONING -> R.string.training_muscle_cardio
        RoutineFocus.CORE -> R.string.training_muscle_core
    }
)

@Composable
private fun RoutineFocus.localizedTodayFocusLabel(): String = stringResource(
    when (this) {
        RoutineFocus.FULL_BODY -> R.string.training_today_focus_full_body
        RoutineFocus.UPPER_BODY -> R.string.training_today_focus_upper_body
        RoutineFocus.PUSH -> R.string.training_today_focus_push
        RoutineFocus.PULL -> R.string.training_today_focus_pull
        RoutineFocus.CHEST -> R.string.training_today_focus_chest
        RoutineFocus.BACK -> R.string.training_today_focus_back
        RoutineFocus.LOWER_BODY -> R.string.training_today_focus_lower_body
        RoutineFocus.SHOULDERS -> R.string.training_today_focus_shoulders
        RoutineFocus.ARMS -> R.string.training_today_focus_arms
        RoutineFocus.BICEPS -> R.string.training_today_focus_biceps
        RoutineFocus.TRICEPS -> R.string.training_today_focus_triceps
        RoutineFocus.FOREARMS -> R.string.training_today_focus_forearms
        RoutineFocus.CARDIO_CONDITIONING -> R.string.training_today_focus_cardio_conditioning
        RoutineFocus.CORE -> R.string.training_today_focus_core
    }
)

@Composable
private fun TrainingExperience.localizedLabel(): String = stringResource(
    when (this) {
        TrainingExperience.BEGINNER -> R.string.training_experience_beginner
        TrainingExperience.INTERMEDIATE -> R.string.training_experience_intermediate
    }
)

@Composable
private fun RoutineFeeling.localizedLabel(): String = stringResource(
    when (this) {
        RoutineFeeling.BALANCED_FULL_BODY -> R.string.training_feeling_balanced_full_body
        RoutineFeeling.FOCUSED_BODY_PART -> R.string.training_feeling_focused_body_part
        RoutineFeeling.APP_RECOMMENDED -> R.string.training_feeling_app_recommended
    }
)

@Composable
private fun PlanTemplate.localizedMeta(): String =
    if (source == RoutineSource.CUSTOM) {
        stringResource(R.string.training_custom_template_meta, days.size)
    } else {
        stringResource(
            R.string.training_template_meta,
            level.localizedLabel(),
            daysPerWeek,
            sessionMinutes
        )
    }

@Composable
private fun RoutineFocusFlow(template: PlanTemplate) {
    val labels = template.focusFlowLabels()
    if (labels.isEmpty()) return
    if (template.source == RoutineSource.CUSTOM) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.training_routine_flow_label),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.testTag("training_routine_flow_${template.id}"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                labels.withIndex().chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowItems.forEach { item ->
                            RoutineFocusFlowChip(
                                label = item.value,
                                modifier = Modifier.testTag(
                                    "training_routine_flow_${template.id}_day_${item.index + 1}"
                                )
                            )
                        }
                    }
                }
            }
        }
    } else {
        Text(
            text = stringResource(
                R.string.training_routine_flow,
                labels.joinToString(separator = stringResource(R.string.training_routine_flow_separator))
            ),
            modifier = Modifier.testTag("training_routine_flow_${template.id}"),
            color = SmartTrainnerColors.Muted,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RoutineFocusFlowChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.GreenSoft
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            color = SmartTrainnerColors.Ink,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlanTemplate.focusFlowLabels(): List<String> =
    days.mapNotNull { it.primaryFocus?.localizedTodayFocusLabel() }

@Composable
private fun PlanTemplateDay.previewTitle(source: RoutineSource): String =
    if (source == RoutineSource.CUSTOM) {
        primaryFocus?.let { focus ->
            stringResource(
                R.string.training_routine_schedule_day_title,
                dayNumber,
                focus.localizedTodayFocusLabel()
            )
        } ?: planDayScheduleTitle(title, dayNumber)
    } else {
        stringResource(
            R.string.training_routine_preview_day_title,
            dayNumber,
            requireNotNull(primaryFocus).localizedTodayFocusLabel(),
            focus.localizedPlanFocus()
        )
    }

@Composable
private fun MuscleGroup.localizedLabel(): String = stringResource(
    when (this) {
        MuscleGroup.LOWER_BODY -> R.string.training_muscle_lower_body
        MuscleGroup.BACK -> R.string.training_muscle_back
        MuscleGroup.CHEST -> R.string.training_muscle_chest
        MuscleGroup.SHOULDERS -> R.string.training_muscle_shoulders
        MuscleGroup.ARMS -> R.string.training_muscle_arms
        MuscleGroup.BICEPS -> R.string.training_muscle_biceps
        MuscleGroup.TRICEPS -> R.string.training_muscle_triceps
        MuscleGroup.FOREARMS -> R.string.training_muscle_forearms
        MuscleGroup.CORE -> R.string.training_muscle_core
        MuscleGroup.CARDIO -> R.string.training_muscle_cardio
        MuscleGroup.FULL_BODY -> R.string.training_muscle_full_body
    }
)

private val armDetailGroups = listOf(
    MuscleGroup.BICEPS,
    MuscleGroup.TRICEPS,
    MuscleGroup.FOREARMS
)

@Composable
private fun EquipmentType.localizedLabel(): String = stringResource(
    when (this) {
        EquipmentType.BODYWEIGHT -> R.string.training_equipment_bodyweight
        EquipmentType.DUMBBELL -> R.string.training_equipment_dumbbell
        EquipmentType.KETTLEBELL -> R.string.training_equipment_kettlebell
        EquipmentType.BARBELL -> R.string.training_equipment_barbell
        EquipmentType.MACHINE -> R.string.training_equipment_machine
        EquipmentType.CABLE -> R.string.training_equipment_cable
        EquipmentType.BENCH -> R.string.training_equipment_bench
        EquipmentType.CARDIO_MACHINE -> R.string.training_equipment_cardio_machine
    }
)

@Composable
private fun DifficultyLevel.localizedLabel(): String = stringResource(
    when (this) {
        DifficultyLevel.BEGINNER -> R.string.training_difficulty_beginner
        DifficultyLevel.INTERMEDIATE -> R.string.training_difficulty_intermediate
        DifficultyLevel.ADVANCED -> R.string.training_difficulty_advanced
    }
)

@Composable
private fun PlanLevel.localizedLabel(): String = stringResource(
    when (this) {
        PlanLevel.INTRO -> R.string.training_level_intro
        PlanLevel.BEGINNER -> R.string.training_level_beginner
        PlanLevel.INTERMEDIATE -> R.string.training_level_intermediate
    }
)

private fun String.toExerciseTitle(): String =
    split('_', '-')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            when (word.lowercase(Locale.ENGLISH)) {
                "lat" -> "Lat"
                "rpe" -> "RPE"
                "y" -> "Y"
                "pushup" -> "Push-up"
                else -> word.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.ENGLISH) else char.toString()
                }
            }
        }
