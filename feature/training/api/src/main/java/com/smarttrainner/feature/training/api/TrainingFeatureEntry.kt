package com.smarttrainner.feature.training.api

import androidx.compose.runtime.Composable

enum class TrainingDestination(
    val route: String,
    val labelResId: Int,
    val testTag: String
) {
    Home(
        route = "training/home",
        labelResId = R.string.training_destination_home,
        testTag = "training_tab_home"
    ),
    Routine(
        route = "training/routine",
        labelResId = R.string.training_destination_routine,
        testTag = "training_tab_plan"
    ),
    Exercises(
        route = "training/exercises",
        labelResId = R.string.training_destination_exercises,
        testTag = "training_tab_exercises"
    ),
    Analysis(
        route = "training/analysis",
        labelResId = R.string.training_destination_analysis,
        testTag = "training_tab_analysis"
    )
}

interface TrainingFeatureEntry {
    @Composable
    fun Content(destination: TrainingDestination)
}
