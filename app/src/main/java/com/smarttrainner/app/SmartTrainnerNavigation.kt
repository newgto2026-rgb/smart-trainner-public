package com.smarttrainner.app

import androidx.compose.foundation.layout.padding
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
import com.smarttrainner.feature.training.api.TrainingDestination
import com.smarttrainner.feature.training.api.TrainingFeatureEntry

@Composable
fun SmartTrainnerMainScreen(
    trainingFeatureEntry: TrainingFeatureEntry
) {
    val navController = rememberNavController()
    val destinations = TrainingDestination.entries
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: TrainingDestination.Home.route

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
            startDestination = TrainingDestination.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            destinations.forEach { destination ->
                composable(destination.route) {
                    trainingFeatureEntry.Content(destination)
                }
            }
        }
    }
}

@Composable
private fun SmartTrainnerBottomBar(
    destinations: List<TrainingDestination>,
    currentRoute: String,
    onDestinationSelected: (TrainingDestination) -> Unit
) {
    NavigationBar(
        containerColor = SmartTrainnerColors.SurfaceRaised
    ) {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute.startsWith(destination.route),
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

private fun TrainingDestination.icon(): ImageVector = when (this) {
    TrainingDestination.Home -> Icons.Default.Home
    TrainingDestination.Routine -> Icons.Default.DateRange
    TrainingDestination.Exercises -> Icons.Default.FitnessCenter
    TrainingDestination.Analysis -> Icons.Default.BarChart
}
