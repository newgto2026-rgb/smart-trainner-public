package com.smarttrainner.feature.training.impl

import androidx.compose.runtime.Composable
import com.smarttrainner.feature.training.api.TrainingDestination
import com.smarttrainner.feature.training.api.TrainingFeatureEntry
import javax.inject.Inject

class TrainingFeatureEntryImpl @Inject constructor() : TrainingFeatureEntry {
    @Composable
    override fun Content(destination: TrainingDestination) {
        TrainingRoute(destination = destination)
    }
}
