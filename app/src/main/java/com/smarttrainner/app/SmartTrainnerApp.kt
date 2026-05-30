package com.smarttrainner.app

import com.smarttrainner.R
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.designsystem.SmartTrainnerBrandSplashImage
import com.smarttrainner.core.designsystem.SmartTrainnerBrandWordmarkImage
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.designsystem.SmartTrainnerGradients
import com.smarttrainner.feature.training.api.TrainingFeatureEntry
import kotlinx.coroutines.delay

@Composable
fun SmartTrainnerApp(
    trainingFeatureEntry: TrainingFeatureEntry,
    viewModel: SmartTrainnerAppViewModel = hiltViewModel()
) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1_650)
        showSplash = false
    }
    if (showSplash) {
        BrandSplashScreen()
        return
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when {
        state.isLoading -> LoadingScreen()
        state.activeSession == null -> LoginScreen(
            onContinueDefaultSession = viewModel::continueWithDefaultSession
        )
        else -> SmartTrainnerMainScreen(trainingFeatureEntry = trainingFeatureEntry)
    }
}

@Composable
private fun BrandSplashScreen() {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        started = true
    }
    val splashScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.88f,
        animationSpec = tween(durationMillis = 780, easing = FastOutSlowInEasing),
        label = "brandSplashScale"
    )
    val splashAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "brandSplashAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartTrainnerColors.Paper)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 28.dp, vertical = 34.dp)
            .testTag("brand_splash"),
        contentAlignment = Alignment.Center
    ) {
        SmartTrainnerBrandSplashImage(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.82f)
                .graphicsLayer {
                    alpha = splashAlpha
                    scaleX = splashScale
                    scaleY = splashScale
                },
            contentDescription = stringResource(R.string.app_name)
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartTrainnerColors.Paper)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = SmartTrainnerColors.Coral)
    }
}

@Composable
private fun LoginScreen(
    onContinueDefaultSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartTrainnerGradients.screen())
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .testTag("login_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SmartTrainnerBrandWordmarkImage(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(36.dp),
                contentDescription = stringResource(R.string.app_name)
            )
        }

        Text(
            text = stringResource(R.string.login_subtitle),
            color = SmartTrainnerColors.Muted,
            style = MaterialTheme.typography.bodyLarge
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.SurfaceRaised,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SessionPreviewRow()
                Button(
                    onClick = onContinueDefaultSession,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_continue_default")
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.login_continue_default))
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SessionPreviewRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SmartTrainnerColors.CoralSoft
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier
                    .padding(PaddingValues(10.dp))
                    .size(28.dp),
                tint = SmartTrainnerColors.Coral
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.login_start_title),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.login_start_detail),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
