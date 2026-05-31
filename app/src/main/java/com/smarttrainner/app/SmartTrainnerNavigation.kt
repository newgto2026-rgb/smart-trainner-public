package com.smarttrainner.app

import com.smarttrainner.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.ui.LocalSmartTrainnerHeaderActions
import com.smarttrainner.app.training.TrainingExercisesRoute
import com.smarttrainner.app.training.TrainingHomeRoute
import com.smarttrainner.app.training.TrainingRoutineRoute
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import kotlinx.coroutines.launch

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
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val destinations = SmartTrainnerDestination.entries
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: SmartTrainnerDestination.Home.route
    val openDrawer: () -> Unit = {
        coroutineScope.launch { drawerState.open() }
    }
    val closeDrawerAndRun: (() -> Unit) -> Unit = { action ->
        coroutineScope.launch {
            drawerState.close()
            action()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ProfileNavigationDrawer(
                        session = activeSession,
                        onLoginRequested = { closeDrawerAndRun(onLoginRequested) },
                        onLogout = { closeDrawerAndRun(onLogout) }
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                CompositionLocalProvider(
                    LocalSmartTrainnerHeaderActions provides {
                        ProfileHeaderMenuButton(
                            session = activeSession,
                            onOpenMenu = openDrawer
                        )
                    }
                ) {
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
                        NavHost(
                            navController = navController,
                            startDestination = SmartTrainnerDestination.Home.route,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
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
        }
    }
}

@Composable
private fun ProfileHeaderMenuButton(
    session: UserSession,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val openProfileLabel = stringResource(R.string.profile_menu_open)
    Surface(
        onClick = onOpenMenu,
        modifier = modifier
            .size(46.dp)
            .semantics { contentDescription = openProfileLabel }
            .testTag("profile_menu_button"),
        shape = CircleShape,
        color = SmartTrainnerColors.Coral,
        contentColor = Color.White,
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = profileInitial(session),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileNavigationDrawer(
    session: UserSession,
    onLoginRequested: () -> Unit,
    onLogout: () -> Unit
) {
    val linked = session.isLinked
    val actionLabel = stringResource(
        if (linked) R.string.profile_menu_logout else R.string.login_title
    )
    val actionIcon = if (linked) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 320.dp),
        drawerContainerColor = SmartTrainnerColors.SurfaceRaised
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_drawer_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SmartTrainnerColors.Ink
            )
            DrawerProfileSummary(session = session)
            HorizontalDivider(color = SmartTrainnerColors.Line)
            NavigationDrawerItem(
                label = { Text(text = actionLabel, fontWeight = FontWeight.Bold) },
                selected = false,
                icon = { Icon(actionIcon, contentDescription = null) },
                onClick = if (linked) onLogout else onLoginRequested,
                modifier = Modifier.testTag(
                    if (linked) "profile_menu_logout" else "profile_menu_login"
                )
            )
        }
    }
}

@Composable
private fun DrawerProfileSummary(
    session: UserSession,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.CoralSoft,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                initial = profileInitial(session),
                size = 48.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = profileLabel(session),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SmartTrainnerColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = profileProviderLabel(session),
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartTrainnerColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    initial: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = SmartTrainnerColors.Coral,
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initial,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun profileInitial(session: UserSession): String {
    val label = profileLabel(session)
    return label.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

private fun profileLabel(session: UserSession): String =
    session.nickname.ifBlank { session.displayName }.trim()

@Composable
private fun profileProviderLabel(session: UserSession): String = stringResource(
    if (session.provider == AuthProvider.GOOGLE) {
        R.string.profile_google_account
    } else {
        R.string.profile_local_account
    }
)

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
