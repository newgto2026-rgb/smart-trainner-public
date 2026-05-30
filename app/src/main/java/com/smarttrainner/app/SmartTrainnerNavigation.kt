package com.smarttrainner.app

import com.smarttrainner.R
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.feature.training.api.TrainingFeatureEntry

@Composable
fun SmartTrainnerMainScreen(
    trainingFeatureEntry: TrainingFeatureEntry
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
        NavHost(
            navController = navController,
            startDestination = SmartTrainnerDestination.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            destinations.forEach { destination ->
                composable(destination.route) {
                    when (destination) {
                        SmartTrainnerDestination.Home -> trainingFeatureEntry.Home()
                        SmartTrainnerDestination.Routine -> trainingFeatureEntry.Routine()
                        SmartTrainnerDestination.Exercises -> trainingFeatureEntry.Exercises()
                        SmartTrainnerDestination.Analysis -> trainingFeatureEntry.Analysis()
                    }
                }
            }
        }
    }
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
