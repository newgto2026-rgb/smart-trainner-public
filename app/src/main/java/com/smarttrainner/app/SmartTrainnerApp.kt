package com.smarttrainner.app

import com.smarttrainner.R
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.designsystem.SmartTrainnerBrandSplashImage
import com.smarttrainner.core.designsystem.SmartTrainnerBrandWordmarkImage
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.designsystem.SmartTrainnerGradients
import com.smarttrainner.core.designsystem.SmartTrainnerThemeTone
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.toProfileSetupOrNull
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.calendar.api.CalendarFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.friend.api.FriendFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SmartTrainnerApp(
    analysisFeatureEntry: AnalysisFeatureEntry,
    calendarFeatureEntry: CalendarFeatureEntry,
    exerciseCatalogFeatureEntry: ExerciseCatalogFeatureEntry,
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    friendFeatureEntry: FriendFeatureEntry,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    selectedThemeTone: SmartTrainnerThemeTone,
    friendNavigationRequest: Int,
    onThemeToneSelected: (SmartTrainnerThemeTone) -> Unit,
    viewModel: SmartTrainnerAppViewModel = hiltViewModel()
) {
    val shouldShowBrandSplash = remember { BrandSplashGate.shouldShow() }
    var showSplash by remember { mutableStateOf(shouldShowBrandSplash) }
    LaunchedEffect(shouldShowBrandSplash) {
        if (shouldShowBrandSplash && BrandSplashGate.markShownIfNeeded()) {
            delay(1_350)
        }
        showSplash = false
    }
    if (showSplash) {
        BrandSplashScreen()
        return
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    fun requestGoogleSignIn(nickname: String? = null) {
        scope.launch {
            viewModel.beginGoogleCredentialRequest()
            when (val credential = requestGoogleCredential(context)) {
                is GoogleCredentialResult.Success -> viewModel.signInWithGoogle(
                    idToken = credential.idToken,
                    nickname = nickname
                )
                GoogleCredentialResult.Cancelled -> viewModel.googleSignInCancelled()
                GoogleCredentialResult.Failed -> viewModel.googleCredentialFailed()
            }
        }
    }
    if (state.deviceLoginConflict) {
        DeviceLoginConflictDialog(
            activeDeviceName = state.deviceLoginConflictDeviceName,
            onConfirm = viewModel::confirmDeviceLoginTakeover,
            onDismiss = viewModel::dismissDeviceLoginConflict
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartTrainnerGradients.screen())
            .testTag("app_root_surface")
    ) {
        when {
            state.isLoading -> LoadingScreen()
            state.activeSession == null -> LoginScreen(
                state = state,
                onGoogleSignIn = { requestGoogleSignIn() }
            )
            state.activeSession?.profile?.toProfileSetupOrNull() == null -> ProfileSetupScreen(
                state = state,
                onNicknameChanged = viewModel::updateLoginNickname,
                onGenderSelected = viewModel::updateLoginGender,
                onHeightCmChanged = viewModel::updateLoginHeightCm,
                onWeightKgChanged = viewModel::updateLoginWeightKg,
                onCheckNickname = viewModel::checkNickname,
                onSaveProfile = viewModel::completeProfileSetup
            )
            else -> SmartTrainnerMainScreen(
                analysisFeatureEntry = analysisFeatureEntry,
                calendarFeatureEntry = calendarFeatureEntry,
                exerciseCatalogFeatureEntry = exerciseCatalogFeatureEntry,
                exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
                friendFeatureEntry = friendFeatureEntry,
                routineFeatureEntry = routineFeatureEntry,
                workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
                activeSession = requireNotNull(state.activeSession),
                trainingExperience = state.trainingExperience,
                googleSignInInProgress = state.googleSignInInProgress,
                friendNavigationRequest = friendNavigationRequest,
                selectedThemeTone = selectedThemeTone,
                onThemeToneSelected = onThemeToneSelected,
                onTrainingExperienceSelected = viewModel::updateTrainingExperience,
                onBodyProfileSaved = viewModel::updateBodyProfile,
                onLinkGoogle = { requestGoogleSignIn(requireNotNull(state.activeSession).nickname) },
                onLogout = viewModel::logout
            )
        }
        if (state.syncInProgress) {
            SyncProgressPill(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun DeviceLoginConflictDialog(
    activeDeviceName: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val deviceName = activeDeviceName ?: stringResource(R.string.device_login_conflict_unknown_device)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.device_login_conflict_title)) },
        text = { Text(stringResource(R.string.device_login_conflict_message, deviceName)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("device_login_takeover_confirm")
            ) {
                Text(stringResource(R.string.device_login_conflict_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("device_login_takeover_dismiss")
            ) {
                Text(stringResource(R.string.device_login_conflict_dismiss))
            }
        },
        modifier = Modifier.testTag("device_login_conflict_dialog")
    )
}

@Composable
private fun BrandSplashScreen() {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        started = true
    }
    val splashScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.98f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "brandSplashScale"
    )
    val splashAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0.95f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "brandSplashAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackgroundColor)
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

private val SplashBackgroundColor = Color(0xFFF6FAFC)

internal object BrandSplashGate {
    private var hasShownBrandSplash = false

    @Synchronized
    fun shouldShow(): Boolean = !hasShownBrandSplash

    @Synchronized
    fun markShownIfNeeded(): Boolean {
        if (hasShownBrandSplash) return false
        hasShownBrandSplash = true
        return true
    }

    @Synchronized
    fun resetForTest() {
        hasShownBrandSplash = false
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
private fun SyncProgressPill(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.SurfaceRaised,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
        modifier = modifier.testTag("app_sync_progress")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                color = SmartTrainnerColors.Coral,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(18.dp)
                    .testTag("app_sync_progress_spinner")
            )
            Text(
                text = stringResource(R.string.app_sync_in_progress),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoginScreen(
    state: SmartTrainnerAppUiState,
    onGoogleSignIn: () -> Unit
) {
    val googleEnabled = !state.googleSignInInProgress
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartTrainnerGradients.screen())
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
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
                LoginGoogleMessage(state)
                Button(
                    onClick = onGoogleSignIn,
                    enabled = googleEnabled,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_continue_google")
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.login_continue_google))
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ProfileSetupScreen(
    state: SmartTrainnerAppUiState,
    onNicknameChanged: (String) -> Unit,
    onGenderSelected: (ProfileGender) -> Unit,
    onHeightCmChanged: (String) -> Unit,
    onWeightKgChanged: (String) -> Unit,
    onCheckNickname: () -> Unit,
    onSaveProfile: () -> Unit
) {
    val trimmedNickname = state.nicknameInput.trim()
    val nicknameInputValid = trimmedNickname.length >= MIN_NICKNAME_LENGTH
    val profileInputValid = state.loginProfileInputIsValid()
    val canSave = nicknameInputValid && profileInputValid && !state.googleSignInInProgress
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartTrainnerGradients.screen())
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .testTag("profile_setup_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SmartTrainnerBrandWordmarkImage(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(36.dp),
            contentDescription = stringResource(R.string.app_name)
        )
        Text(
            text = stringResource(R.string.profile_setup_detail),
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
                Text(
                    text = stringResource(R.string.profile_setup_title),
                    color = SmartTrainnerColors.Ink,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = state.nicknameInput,
                    onValueChange = onNicknameChanged,
                    singleLine = true,
                    label = { Text(stringResource(R.string.login_nickname_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_setup_nickname_input")
                )
                LoginProfileFields(
                    state = state,
                    onGenderSelected = onGenderSelected,
                    onHeightCmChanged = onHeightCmChanged,
                    onWeightKgChanged = onWeightKgChanged
                )
                OutlinedButton(
                    onClick = onCheckNickname,
                    enabled = state.nicknameCheckStatus != NicknameCheckStatus.Checking,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_setup_check_nickname")
                ) {
                    Text(stringResource(R.string.login_check_nickname))
                }
                LoginNicknameMessage(state)
                Button(
                    onClick = onSaveProfile,
                    enabled = canSave,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("profile_setup_save")
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.profile_setup_save))
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LoginProfileFields(
    state: SmartTrainnerAppUiState,
    onGenderSelected: (ProfileGender) -> Unit,
    onHeightCmChanged: (String) -> Unit,
    onWeightKgChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.login_gender_label),
            color = SmartTrainnerColors.Ink,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileGender.entries.forEach { gender ->
                OutlinedButton(
                    onClick = { onGenderSelected(gender) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("login_gender_${gender.name.lowercase()}")
                ) {
                    Text(
                        text = stringResource(gender.labelResId()),
                        color = if (state.genderInput == gender) {
                            SmartTrainnerColors.Coral
                        } else {
                            SmartTrainnerColors.Ink
                        }
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.heightCmInput,
                onValueChange = onHeightCmChanged,
                singleLine = true,
                label = { Text(stringResource(R.string.login_height_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("login_height_input")
            )
            OutlinedTextField(
                value = state.weightKgInput,
                onValueChange = onWeightKgChanged,
                singleLine = true,
                label = { Text(stringResource(R.string.login_weight_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .weight(1f)
                    .testTag("login_weight_input")
            )
        }
        if (state.profileInputInvalid) {
            Text(
                text = stringResource(R.string.login_profile_invalid),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("login_profile_message")
            )
        }
    }
}

@Composable
private fun LoginNicknameMessage(state: SmartTrainnerAppUiState) {
    val message = when {
        state.googleSignInCancelled -> stringResource(R.string.login_google_cancelled)
        state.loginFailed -> stringResource(R.string.login_google_failed)
        state.nicknameCheckStatus == NicknameCheckStatus.Checking -> stringResource(R.string.login_nickname_checking)
        state.nicknameCheckStatus == NicknameCheckStatus.Available -> stringResource(R.string.login_nickname_available)
        state.nicknameCheckStatus == NicknameCheckStatus.Taken -> stringResource(R.string.login_nickname_taken)
        state.nicknameCheckStatus == NicknameCheckStatus.Invalid -> stringResource(R.string.login_nickname_invalid)
        state.nicknameCheckStatus == NicknameCheckStatus.Error -> stringResource(R.string.login_nickname_error)
        else -> stringResource(R.string.login_nickname_helper)
    }
    Text(
        text = message,
        color = if (
            state.nicknameCheckStatus == NicknameCheckStatus.Taken ||
            state.nicknameCheckStatus == NicknameCheckStatus.Invalid ||
            state.nicknameCheckStatus == NicknameCheckStatus.Error ||
            state.loginFailed
        ) {
            MaterialTheme.colorScheme.error
        } else {
            SmartTrainnerColors.Muted
        },
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag("login_nickname_message")
    )
}

@Composable
private fun LoginGoogleMessage(state: SmartTrainnerAppUiState) {
    val message = when {
        state.googleSignInCancelled -> stringResource(R.string.login_google_cancelled)
        state.loginFailed -> stringResource(R.string.login_google_failed)
        else -> stringResource(R.string.login_start_detail)
    }
    Text(
        text = message,
        color = if (state.loginFailed) MaterialTheme.colorScheme.error else SmartTrainnerColors.Muted,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag("login_google_message")
    )
}


private fun SmartTrainnerAppUiState.loginProfileInputIsValid(): Boolean =
    genderInput != null &&
        heightCmInput.toIntOrNull()?.let { it > 0 } == true &&
        weightKgInput.toDoubleOrNull()?.let { it > 0.0 } == true

private fun ProfileGender.labelResId(): Int = when (this) {
    ProfileGender.MALE -> R.string.profile_gender_male
    ProfileGender.FEMALE -> R.string.profile_gender_female
}

private const val MIN_NICKNAME_LENGTH = 2

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
