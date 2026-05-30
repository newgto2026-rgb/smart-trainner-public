package com.smarttrainner.feature.training.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.designsystem.SmartTrainnerGradients

@Composable
internal fun Header(state: TrainingUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .background(SmartTrainnerGradients.brandLight(), RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.training_app_title),
                    modifier = Modifier.testTag("training_app_title"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = SmartTrainnerColors.Ink
                )
                Text(
                    text = state.plan?.localizedName().orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SmartTrainnerColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun TrainingBottomBar(
    selectedTab: TrainingTab,
    onTabSelected: (TrainingTab) -> Unit
) {
    NavigationBar(
        containerColor = SmartTrainnerColors.SurfaceRaised,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        TrainingTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.testTag(tab.testTag()),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SmartTrainnerColors.Coral,
                    selectedTextColor = SmartTrainnerColors.Coral,
                    indicatorColor = SmartTrainnerColors.CoralSoft,
                    unselectedIconColor = SmartTrainnerColors.Muted,
                    unselectedTextColor = SmartTrainnerColors.Muted
                ),
                icon = { Icon(tab.icon(), contentDescription = null) },
                label = { Text(tab.label()) }
            )
        }
    }
}

@Composable
internal fun TrainingTab.label(): String = when (this) {
    TrainingTab.HOME -> stringResource(R.string.training_tab_home)
    TrainingTab.PLAN -> stringResource(R.string.training_tab_plan)
    TrainingTab.EXERCISES -> stringResource(R.string.training_tab_exercises)
    TrainingTab.ANALYSIS -> stringResource(R.string.training_tab_analysis)
}

internal fun TrainingTab.icon(): ImageVector = when (this) {
    TrainingTab.HOME -> Icons.Default.Home
    TrainingTab.PLAN -> Icons.Default.DateRange
    TrainingTab.EXERCISES -> Icons.Default.FitnessCenter
    TrainingTab.ANALYSIS -> Icons.Default.BarChart
}

internal fun TrainingTab.testTag(): String = when (this) {
    TrainingTab.HOME -> "training_tab_home"
    TrainingTab.PLAN -> "training_tab_plan"
    TrainingTab.EXERCISES -> "training_tab_exercises"
    TrainingTab.ANALYSIS -> "training_tab_analysis"
}

@Composable
internal fun StatusChip(completed: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (completed) SmartTrainnerColors.GreenSoft else SmartTrainnerColors.SteelSoft
    ) {
        Text(
            text = stringResource(if (completed) R.string.training_completed else R.string.training_incomplete),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = if (completed) SmartTrainnerColors.Green else SmartTrainnerColors.Muted,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun StatusIcon(completed: Boolean) {
    Icon(
        imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
        contentDescription = stringResource(
            if (completed) R.string.training_completed else R.string.training_incomplete
        ),
        tint = if (completed) SmartTrainnerColors.Green else SmartTrainnerColors.Muted
    )
}

@Composable
internal fun SectionTitle(stringResourceId: Int, testTag: String) {
    Text(
        text = stringResource(stringResourceId),
        modifier = Modifier.testTag(testTag),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
internal fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SmartTrainnerColors.SurfaceRaised, RoundedCornerShape(8.dp))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = SmartTrainnerColors.Muted)
    }
}
