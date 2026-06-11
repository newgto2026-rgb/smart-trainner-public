package com.smarttrainner.feature.routine.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.smarttrainner.core.ui.SmartTrainnerStatusIcon

@Composable
internal fun StatusIcon(
    completed: Boolean,
    modifier: Modifier = Modifier
) {
    SmartTrainnerStatusIcon(
        completed = completed,
        contentDescription = stringResource(
            if (completed) R.string.routine_completed else R.string.routine_incomplete
        ),
        modifier = modifier
    )
}
