package com.smarttrainner.feature.training.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.model.RoutineSource

@Composable
internal fun RoutineSourceChip(source: RoutineSource, testTag: String = source.routineSourceTag()) {
    TrainingBadge(
        modifier = Modifier.testTag(testTag),
        text = stringResource(
            if (source == RoutineSource.CUSTOM) {
                R.string.training_custom_routine_badge
            } else {
                R.string.training_default_routine_badge
            }
        ),
        icon = if (source == RoutineSource.CUSTOM) Icons.Default.Edit else Icons.Default.FitnessCenter,
        containerColor = if (source == RoutineSource.CUSTOM) SmartTrainnerColors.GreenSoft else SmartTrainnerColors.CoralSoft,
        contentColor = SmartTrainnerColors.Ink
    )
}

internal fun RoutineSource.routineSourceTag(): String = if (this == RoutineSource.CUSTOM) {
    "training_routine_source_custom"
} else {
    "training_routine_source_default"
}

internal fun RoutineSource.homeRoutineSourceTag(): String = if (this == RoutineSource.CUSTOM) {
    "training_home_routine_source_custom"
} else {
    "training_home_routine_source_default"
}
