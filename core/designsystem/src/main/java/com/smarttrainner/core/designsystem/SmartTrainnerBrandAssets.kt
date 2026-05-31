package com.smarttrainner.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun SmartTrainnerBrandSymbolImage(
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    AsyncImage(
        model = R.drawable.brand_ai_runner_symbol,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun SmartTrainnerBrandWordmarkImage(
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    AsyncImage(
        model = R.drawable.brand_ai_runner_wordmark,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun SmartTrainnerBrandLockupImage(
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    AsyncImage(
        model = R.drawable.brand_ai_runner_lockup,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun SmartTrainnerBrandSplashImage(
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    AsyncImage(
        model = R.drawable.brand_ai_trainer_splash_transparent,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}
