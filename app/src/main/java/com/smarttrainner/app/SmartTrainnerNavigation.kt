package com.smarttrainner.app

import com.smarttrainner.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.app.training.TrainingExercisesRoute
import com.smarttrainner.app.training.TrainingHomeRoute
import com.smarttrainner.app.training.TrainingRoutineRoute
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import com.smarttrainner.core.model.UserSession

@Composable
fun SmartTrainnerMainScreen(
    analysisFeatureEntry: AnalysisFeatureEntry,
    exerciseCatalogFeatureEntry: ExerciseCatalogFeatureEntry,
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    activeSession: UserSession,
    onLoginRequested: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val destinations = SmartTrainnerDestination.entries
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: SmartTrainnerDestination.Home.route

    Scaffold(
        containerColor = Color.Transparent,
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
                startDestination = SmartTrainnerDestination.Home.route,
                modifier = Modifier.fillMaxSize()
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
            ProfileMenuButton(
                session = activeSession,
                onLoginRequested = onLoginRequested,
                onLogout = onLogout,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(top = 16.dp, end = 18.dp)
            )
        }
    }
}

@Composable
private fun ProfileMenuButton(
    session: UserSession,
    onLoginRequested: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val linked = session.isLinked
    val actionLabel = stringResource(
        if (linked) R.string.profile_menu_logout else R.string.login_title
    )
    val actionIcon = if (linked) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier
                .size(44.dp)
                .testTag("profile_menu_button"),
            shape = CircleShape,
            color = SmartTrainnerColors.Coral,
            contentColor = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = profileInitial(session),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(actionLabel) },
                leadingIcon = { Icon(actionIcon, contentDescription = null) },
                onClick = {
                    expanded = false
                    if (linked) onLogout() else onLoginRequested()
                },
                modifier = Modifier.testTag(
                    if (linked) "profile_menu_logout" else "profile_menu_login"
                )
            )
        }
    }
}

private fun profileInitial(session: UserSession): String {
    val label = session.nickname.ifBlank { session.displayName }.trim()
    return label.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

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
