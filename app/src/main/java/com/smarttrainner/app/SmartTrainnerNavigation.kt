package com.smarttrainner.app

import com.smarttrainner.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.designsystem.SmartTrainnerThemeTone
import com.smarttrainner.core.designsystem.swatchColor
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.ui.LocalSmartTrainnerHeaderAction
import com.smarttrainner.app.training.TrainingExercisesRoute
import com.smarttrainner.app.training.TrainingHomeRoute
import com.smarttrainner.app.training.TrainingRoutineRoute
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry

@Composable
fun SmartTrainnerMainScreen(
    analysisFeatureEntry: AnalysisFeatureEntry,
    exerciseCatalogFeatureEntry: ExerciseCatalogFeatureEntry,
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    activeSession: UserSession,
    trainingExperience: TrainingExperience,
    googleSignInInProgress: Boolean,
    selectedThemeTone: SmartTrainnerThemeTone,
    onThemeToneSelected: (SmartTrainnerThemeTone) -> Unit,
    onTrainingExperienceSelected: (TrainingExperience) -> Unit,
    onLinkGoogle: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val destinations = SmartTrainnerDestination.entries
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: SmartTrainnerDestination.Home.route
    var profileOpen by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalSmartTrainnerHeaderAction provides {
                ProfileButton(
                    session = activeSession,
                    onClick = { profileOpen = true }
                )
            }
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                bottomBar = {
                    SmartTrainnerBottomBar(
                        destinations = destinations,
                        currentRoute = currentRoute,
                        onDestinationSelected = { destination ->
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = SmartTrainnerDestination.Home.route
                    ) {
                        destinations.forEach { destination ->
                            composable(destination.route) {
                                when (destination) {
                                    SmartTrainnerDestination.Home -> TrainingHomeRoute(
                                        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
                                        routineFeatureEntry = routineFeatureEntry,
                                        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry
                                    )
                                    SmartTrainnerDestination.Routine -> TrainingRoutineRoute(
                                        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
                                        routineFeatureEntry = routineFeatureEntry,
                                        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry
                                    )
                                    SmartTrainnerDestination.Exercises -> TrainingExercisesRoute(
                                        exerciseCatalogFeatureEntry = exerciseCatalogFeatureEntry,
                                        exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
                                        routineFeatureEntry = routineFeatureEntry,
                                        workoutRecordingFeatureEntry = workoutRecordingFeatureEntry
                                    )
                                    SmartTrainnerDestination.Analysis -> analysisFeatureEntry.Route()
                                }
                            }
                        }
                    }
                }
            }
        }
        if (profileOpen) {
            ProfileDrawer(
                session = activeSession,
                trainingExperience = trainingExperience,
                googleSignInInProgress = googleSignInInProgress,
                selectedThemeTone = selectedThemeTone,
                onDismiss = { profileOpen = false },
                onThemeToneSelected = onThemeToneSelected,
                onTrainingExperienceSelected = onTrainingExperienceSelected,
                onLinkGoogle = onLinkGoogle,
                onLogout = {
                    profileOpen = false
                    onLogout()
                },
                modifier = Modifier.zIndex(3f)
            )
        }
    }
}

@Composable
private fun ProfileButton(
    session: UserSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(SmartTrainnerColors.Coral)
            .clickable(onClick = onClick)
            .testTag("profile_button"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = session.profileInitial(),
            color = SmartTrainnerColors.SurfaceRaised,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileDrawer(
    session: UserSession,
    trainingExperience: TrainingExperience,
    googleSignInInProgress: Boolean,
    selectedThemeTone: SmartTrainnerThemeTone,
    onDismiss: () -> Unit,
    onThemeToneSelected: (SmartTrainnerThemeTone) -> Unit,
    onTrainingExperienceSelected: (TrainingExperience) -> Unit,
    onLinkGoogle: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var themeSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var trainingLevelSettingsOpen by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f))
            .clickable(onClick = onDismiss)
            .testTag("profile_scrim")
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(304.dp)
                .clickable(onClick = {}),
            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            color = SmartTrainnerColors.SurfaceRaised,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = SmartTrainnerColors.CoralSoft,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = session.profileInitial(),
                            color = SmartTrainnerColors.Coral,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = session.nickname,
                        color = SmartTrainnerColors.Ink,
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (session.isLinked) {
                            stringResource(R.string.profile_linked_account)
                        } else {
                            stringResource(R.string.profile_local_account)
                        },
                        color = SmartTrainnerColors.Muted,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
                TrainingLevelSettingsEntry(
                    trainingExperience = trainingExperience,
                    onClick = { trainingLevelSettingsOpen = true }
                )
                ThemeSettingsEntry(
                    selectedThemeTone = selectedThemeTone,
                    onClick = { themeSettingsOpen = true }
                )
                if (!session.isLinked) {
                    OutlinedButton(
                        onClick = onLinkGoogle,
                        enabled = !googleSignInInProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_link_google")
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                        Text(
                            text = stringResource(R.string.profile_link_google),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_logout")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Text(
                        text = stringResource(R.string.profile_logout),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        if (themeSettingsOpen) {
            ThemeSettingsDialog(
                selectedThemeTone = selectedThemeTone,
                onDismiss = { themeSettingsOpen = false },
                onThemeToneSelected = { themeTone ->
                    onThemeToneSelected(themeTone)
                    themeSettingsOpen = false
                }
            )
        }
        if (trainingLevelSettingsOpen) {
            TrainingLevelSettingsDialog(
                trainingExperience = trainingExperience,
                onDismiss = { trainingLevelSettingsOpen = false },
                onTrainingExperienceSelected = { experience ->
                    onTrainingExperienceSelected(experience)
                    trainingLevelSettingsOpen = false
                }
            )
        }
    }
}

@Composable
private fun TrainingLevelSettingsEntry(
    trainingExperience: TrainingExperience,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("profile_training_level_entry")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = SmartTrainnerColors.Coral,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.profile_training_level_title),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(trainingExperience.profileLabelResId()),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ThemeSettingsEntry(
    selectedThemeTone: SmartTrainnerThemeTone,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("profile_theme_entry")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = selectedThemeTone.swatchColor(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
                modifier = Modifier.size(18.dp)
            ) {}
            Text(
                text = stringResource(R.string.profile_theme_title),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(selectedThemeTone.labelResId()),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TrainingLevelSettingsDialog(
    trainingExperience: TrainingExperience,
    onDismiss: () -> Unit,
    onTrainingExperienceSelected: (TrainingExperience) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(8.dp),
        containerColor = SmartTrainnerColors.SurfaceRaised,
        title = {
            Text(
                text = stringResource(R.string.profile_training_level_title),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TrainingExperience.entries.forEach { experience ->
                    TrainingExperienceOption(
                        experience = experience,
                        selected = trainingExperience == experience,
                        onClick = { onTrainingExperienceSelected(experience) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profile_theme_close))
            }
        }
    )
}

@Composable
private fun TrainingExperienceOption(
    experience: TrainingExperience,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) SmartTrainnerColors.CoralSoft else SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, if (selected) SmartTrainnerColors.Coral else SmartTrainnerColors.Line),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("profile_training_level_${experience.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(experience.profileLabelResId()),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SmartTrainnerColors.Coral,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Spacer(Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ThemeSettingsDialog(
    selectedThemeTone: SmartTrainnerThemeTone,
    onDismiss: () -> Unit,
    onThemeToneSelected: (SmartTrainnerThemeTone) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(8.dp),
        containerColor = SmartTrainnerColors.SurfaceRaised,
        title = {
            Text(
                text = stringResource(R.string.profile_theme_title),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmartTrainnerThemeTone.entries.forEach { themeTone ->
                    ThemeToneOption(
                        themeTone = themeTone,
                        selected = selectedThemeTone == themeTone,
                        onClick = { onThemeToneSelected(themeTone) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profile_theme_close))
            }
        }
    )
}

@Composable
private fun ThemeToneOption(
    themeTone: SmartTrainnerThemeTone,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) SmartTrainnerColors.CoralSoft else SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, if (selected) SmartTrainnerColors.Coral else SmartTrainnerColors.Line),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("profile_theme_${themeTone.storageValue}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = themeTone.swatchColor(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
                modifier = Modifier.size(18.dp)
            ) {}
            Text(
                text = stringResource(themeTone.labelResId()),
                color = SmartTrainnerColors.Ink,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SmartTrainnerColors.Coral,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Spacer(Modifier.size(18.dp))
            }
        }
    }
}

private fun SmartTrainnerThemeTone.labelResId(): Int = when (this) {
    SmartTrainnerThemeTone.Red -> R.string.profile_theme_red
    SmartTrainnerThemeTone.Blue -> R.string.profile_theme_blue
    SmartTrainnerThemeTone.Green -> R.string.profile_theme_green
    SmartTrainnerThemeTone.Black -> R.string.profile_theme_black
}

private fun TrainingExperience.profileLabelResId(): Int = when (this) {
    TrainingExperience.BEGINNER -> R.string.profile_training_level_beginner
    TrainingExperience.INTERMEDIATE -> R.string.profile_training_level_intermediate
    TrainingExperience.ADVANCED -> R.string.profile_training_level_advanced
}

private fun UserSession.profileInitial(): String =
    nickname.ifBlank { displayName }.trim().take(1).uppercase()

@Composable
private fun SmartTrainnerBottomBar(
    destinations: List<SmartTrainnerDestination>,
    currentRoute: String,
    onDestinationSelected: (SmartTrainnerDestination) -> Unit
) {
    NavigationBar(
        containerColor = SmartTrainnerColors.SurfaceRaised,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onDestinationSelected(destination) },
                modifier = Modifier.testTag(destination.testTag),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SmartTrainnerColors.Coral,
                    selectedTextColor = SmartTrainnerColors.Coral,
                    indicatorColor = SmartTrainnerColors.CoralSoft,
                    unselectedIconColor = SmartTrainnerColors.Muted,
                    unselectedTextColor = SmartTrainnerColors.Muted
                ),
                icon = { Icon(destination.icon(), contentDescription = null) },
                label = { Text(stringResource(destination.labelResId)) }
            )
        }
    }
}

private enum class SmartTrainnerDestination(
    val route: String,
    val labelResId: Int,
    val testTag: String
) {
    Home(
        route = "training/home",
        labelResId = R.string.app_destination_home,
        testTag = "training_tab_home"
    ),
    Routine(
        route = "training/routine",
        labelResId = R.string.app_destination_routine,
        testTag = "training_tab_plan"
    ),
    Exercises(
        route = "training/exercises",
        labelResId = R.string.app_destination_exercises,
        testTag = "training_tab_exercises"
    ),
    Analysis(
        route = "training/analysis",
        labelResId = R.string.app_destination_analysis,
        testTag = "training_tab_analysis"
    )
}

private fun SmartTrainnerDestination.icon(): ImageVector = when (this) {
    SmartTrainnerDestination.Home -> Icons.Default.Home
    SmartTrainnerDestination.Routine -> Icons.Default.DateRange
    SmartTrainnerDestination.Exercises -> Icons.Default.FitnessCenter
    SmartTrainnerDestination.Analysis -> Icons.Default.BarChart
}
