package com.smarttrainner.app

import android.annotation.SuppressLint
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.smarttrainner.R
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.smarttrainner.BuildConfig
import com.smarttrainner.core.designsystem.SmartTrainnerBrandSplashImage
import com.smarttrainner.core.designsystem.SmartTrainnerBrandWordmarkImage
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.designsystem.SmartTrainnerGradients
import com.smarttrainner.core.domain.SocialSignInCredential
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SmartTrainnerApp(
    analysisFeatureEntry: AnalysisFeatureEntry,
    exerciseCatalogFeatureEntry: ExerciseCatalogFeatureEntry,
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    when {
        state.isLoading -> LoadingScreen()
        state.activeSession == null && state.awaitingNickname -> NicknameScreen(
            state = state,
            onNicknameChanged = viewModel::onNicknameChanged,
            onCheckNickname = viewModel::checkNickname,
            onContinueGoogle = viewModel::continueWithSocialSession,
            onBackToLogin = viewModel::returnToLogin
        )
        state.activeSession == null -> LoginScreen(
            state = state,
            onContinueDefaultSession = viewModel::continueWithDefaultSession,
            onContinueGoogle = {
                scope.launch {
                    viewModel.onGoogleSignInStarted()
                    runCatching { requestGoogleSignInCredential(context) }
                        .onSuccess { credential ->
                            viewModel.requestNicknameForSocialSession(credential)
                        }
                        .onFailure { error ->
                            viewModel.onGoogleCredentialFailed(
                                cancelled = error is GetCredentialCancellationException,
                                credentialUnavailable = error is NoCredentialException
                            )
                        }
                }
            }
        )
        else -> SmartTrainnerMainScreen(
            analysisFeatureEntry = analysisFeatureEntry,
            exerciseCatalogFeatureEntry = exerciseCatalogFeatureEntry,
            exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
            routineFeatureEntry = routineFeatureEntry,
            workoutRecordingFeatureEntry = workoutRecordingFeatureEntry
        )
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
    state: SmartTrainnerAppUiState,
    onContinueDefaultSession: () -> Unit,
    onContinueGoogle: () -> Unit
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
            text = stringResource(R.string.login_title),
            color = SmartTrainnerColors.Ink,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

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
                Button(
                    onClick = onContinueGoogle,
                    enabled = !state.isSigningIn,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_continue_google")
                ) {
                    if (state.isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.login_continue_google))
                }
                LoginMessageText(message = state.loginMessage)
                Text(
                    text = stringResource(R.string.login_local_only_notice),
                    color = SmartTrainnerColors.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedButton(
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
private fun NicknameScreen(
    state: SmartTrainnerAppUiState,
    onNicknameChanged: () -> Unit,
    onCheckNickname: (String) -> Unit,
    onContinueGoogle: (String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var nickname by rememberSaveable { mutableStateOf("") }
    val trimmedNickname = nickname.trim()
    val canUseNickname = trimmedNickname.length >= 2 && !state.isCheckingNickname && !state.isSigningIn

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartTrainnerGradients.screen())
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .testTag("nickname_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SmartTrainnerBrandWordmarkImage(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(36.dp),
            contentDescription = stringResource(R.string.app_name)
        )
        Text(
            text = stringResource(R.string.login_nickname_title),
            color = SmartTrainnerColors.Ink,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
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
                OutlinedTextField(
                    value = nickname,
                    onValueChange = {
                        nickname = it
                        onNicknameChanged()
                    },
                    label = { Text(stringResource(R.string.login_nickname_label)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_nickname_input")
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onCheckNickname(trimmedNickname) },
                        enabled = canUseNickname,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("login_check_nickname")
                    ) {
                        if (state.isCheckingNickname) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.login_check_nickname))
                    }
                    LoginMessageText(
                        message = state.loginMessage,
                        modifier = Modifier.weight(1f)
                    )
                }
                Button(
                    onClick = { onContinueGoogle(trimmedNickname) },
                    enabled = canUseNickname,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_finish_google")
                ) {
                    if (state.isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.login_finish_google))
                }
                OutlinedButton(
                    onClick = onBackToLogin,
                    enabled = !state.isSigningIn,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_back")
                ) {
                    Text(stringResource(R.string.login_back))
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LoginMessageText(
    message: LoginMessage?,
    modifier: Modifier = Modifier
) {
    if (message == null) return
    val textRes = when (message) {
        LoginMessage.NICKNAME_AVAILABLE -> R.string.login_nickname_available
        LoginMessage.NICKNAME_TAKEN -> R.string.login_nickname_taken
        LoginMessage.NICKNAME_REQUIRED -> R.string.login_nickname_required
        LoginMessage.GOOGLE_CANCELLED -> R.string.login_google_cancelled
        LoginMessage.GOOGLE_UNAVAILABLE -> R.string.login_google_unavailable
        LoginMessage.LOGIN_FAILED -> R.string.login_failed
    }
    val color = when (message) {
        LoginMessage.NICKNAME_AVAILABLE -> SmartTrainnerColors.Green
        LoginMessage.NICKNAME_TAKEN,
        LoginMessage.NICKNAME_REQUIRED,
        LoginMessage.GOOGLE_UNAVAILABLE,
        LoginMessage.LOGIN_FAILED -> MaterialTheme.colorScheme.error
        LoginMessage.GOOGLE_CANCELLED -> SmartTrainnerColors.Muted
    }
    Text(
        text = stringResource(textRes),
        color = color,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier.testTag("login_message")
    )
}

@SuppressLint("CredentialManagerSignInWithGoogle")
private suspend fun requestGoogleSignInCredential(context: Context): SocialSignInCredential {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .setFilterByAuthorizedAccounts(false)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    val result = CredentialManager.create(context).getCredential(
        context = context,
        request = request
    )
    val credential = result.credential as? CustomCredential
        ?: error("Unsupported Google credential")
    val isGoogleIdCredential =
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
    check(isGoogleIdCredential) { "Unsupported Google credential" }

    val googleCredential = try {
        GoogleIdTokenCredential.createFrom(credential.data)
    } catch (error: GoogleIdTokenParsingException) {
        throw error
    }
    return SocialSignInCredential(
        provider = AuthProvider.GOOGLE,
        idToken = googleCredential.idToken,
        displayName = googleCredential.displayName,
        email = googleCredential.id,
        avatarUrl = googleCredential.profilePictureUri?.toString()
    )
}
