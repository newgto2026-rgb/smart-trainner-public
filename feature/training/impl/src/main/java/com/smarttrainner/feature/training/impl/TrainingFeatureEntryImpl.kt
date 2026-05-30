package com.smarttrainner.feature.training.impl

import androidx.compose.runtime.Composable
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseMediaFeatureEntry
import com.smarttrainner.feature.training.api.TrainingDestination
import com.smarttrainner.feature.training.api.TrainingFeatureEntry
import javax.inject.Inject

class TrainingFeatureEntryImpl @Inject constructor(
    private val analysisFeatureEntry: AnalysisFeatureEntry,
    private val exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    private val exerciseMediaFeatureEntry: ExerciseMediaFeatureEntry
) : TrainingFeatureEntry {
    @Composable
    override fun Content(destination: TrainingDestination) {
        TrainingRoute(
            destination = destination,
            analysisFeatureEntry = analysisFeatureEntry,
            exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
            exerciseMediaFeatureEntry = exerciseMediaFeatureEntry
        )
    }
}
