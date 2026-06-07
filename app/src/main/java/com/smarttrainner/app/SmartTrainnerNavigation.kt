package com.smarttrainner.app

import com.smarttrainner.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.designsystem.SmartTrainnerThemeTone
import com.smarttrainner.core.designsystem.swatchColor
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.UserProfile
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.ui.LocalSmartTrainnerHeaderAction
import com.smarttrainner.app.training.TrainingExercisesRoute
import com.smarttrainner.app.training.TrainingHomeRoute
import com.smarttrainner.app.training.TrainingRoutineRoute
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.calendar.api.CalendarFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.friend.api.FriendFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry

private const val TrainingGraphRoute = "training_graph"

@Composable
fun SmartTrainnerMainScreen(
    analysisFeatureEntry: AnalysisFeatureEntry,
    calendarFeatureEntry: CalendarFeatureEntry,
    exerciseCatalogFeatureEntry: ExerciseCatalogFeatureEntry,
    exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry,
    friendFeatureEntry: FriendFeatureEntry,
    routineFeatureEntry: RoutineFeatureEntry,
    workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry,
    activeSession: UserSession,
    trainingExperience: TrainingExperience,
    googleSignInInProgress: Boolean,
    friendNavigationRequest: Int,
    selectedThemeTone: SmartTrainnerThemeTone,
    onThemeToneSelected: (SmartTrainnerThemeTone) -> Unit,
    onTrainingExperienceSelected: (TrainingExperience) -> Unit,
    onBodyProfileSaved: (ProfileGender?, Int, Double) -> Unit,
    onLinkGoogle: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val destinations = SmartTrainnerDestination.entries
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: SmartTrainnerDestination.Home.route
    var profileOpen by rememberSaveable { mutableStateOf(false) }
    var routineChangePromptOpen by rememberSaveable { mutableStateOf(false) }
    var routineLibraryOpenRequest by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(friendNavigationRequest) {
        if (friendNavigationRequest > 0) {
            navController.navigate(SmartTrainnerDestination.Friends.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalSmartTrainnerHeaderAction provides {
                ProfileButton(
                    session = activeSession,
                    onClick = { profileOpen = true }
                )
            }
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                bottomBar = {
                    SmartTrainnerBottomBar(
                        destinations = destinations,
                        currentRoute = currentRoute,
                        onDestinationSelected = { destination ->
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = TrainingGraphRoute
                    ) {
                        navigation(
                            startDestination = SmartTrainnerDestination.Home.route,
                            route = TrainingGraphRoute
                        ) {
                            destinations
                                .filter { it.isTrainingDestination }
                                .forEach { destination ->
                                    composable(destination.route) { backStackEntry ->
                                        val trainingViewModelStoreOwner = remember(backStackEntry) {
                                            navController.getBackStackEntry(TrainingGraphRoute)
                                        }
                                        when (destination) {
                                            SmartTrainnerDestination.Home -> TrainingHomeRoute(
                                                exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
                                                routineFeatureEntry = routineFeatureEntry,
                                                workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
                                                viewModelStoreOwner = trainingViewModelStoreOwner
                                            )
                                            SmartTrainnerDestination.Routine -> TrainingRoutineRoute(
                                                exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
                                                routineFeatureEntry = routineFeatureEntry,
                                                workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
                                                viewModelStoreOwner = trainingViewModelStoreOwner,
                                                routineLibraryOpenRequest = routineLibraryOpenRequest,
                                                onRoutineLibraryOpenRequestConsumed = { request ->
                                                    if (routineLibraryOpenRequest == request) {
                                                        routineLibraryOpenRequest = 0
                                                    }
                                                }
                                            )
                                            SmartTrainnerDestination.Exercises -> TrainingExercisesRoute(
                                                exerciseCatalogFeatureEntry = exerciseCatalogFeatureEntry,
                                                exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
                                                routineFeatureEntry = routineFeatureEntry,
                                                workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
                                                viewModelStoreOwner = trainingViewModelStoreOwner
                                            )
                                            SmartTrainnerDestination.Calendar,
                                            SmartTrainnerDestination.Friends,
                                            SmartTrainnerDestination.Analysis -> Unit
                                        }
                                    }
                                }
                        }
                        composable(SmartTrainnerDestination.Calendar.route) {
                            calendarFeatureEntry.Route()
                        }
                        composable(SmartTrainnerDestination.Analysis.route) {
                            analysisFeatureEntry.Route()
                        }
                        composable(SmartTrainnerDestination.Friends.route) {
                            friendFeatureEntry.Route()
                        }
                    }
                }
            }
        }
        if (profileOpen) {
            ProfileDrawer(
                session = activeSession,
                trainingExperience = trainingExperience,
                googleSignInInProgress = googleSignInInProgress,
                selectedThemeTone = selectedThemeTone,
                onDismiss = { profileOpen = false },
                onThemeToneSelected = onThemeToneSelected,
                onTrainingExperienceSelected = { experience ->
                    val changed = experience != trainingExperience
                    onTrainingExperienceSelected(experience)
                    profileOpen = false
                    if (changed) {
                        routineChangePromptOpen = true
                    }
                },
                onBodyProfileSaved = onBodyProfileSaved,
                onLinkGoogle = onLinkGoogle,
                onLogout = {
                    profileOpen = false
                    onLogout()
                },
                modifier = Modifier.zIndex(3f)
            )
        }
        if (routineChangePromptOpen) {
            AlertDialog(
                onDismissRequest = { routineChangePromptOpen = false },
                modifier = Modifier.testTag("profile_routine_change_prompt"),
                shape = RoundedCornerShape(8.dp),
                containerColor = SmartTrainnerColors.SurfaceRaised,
                title = {
                    Text(
                        text = stringResource(R.string.profile_routine_change_prompt_title),
                        color = SmartTrainnerColors.Ink,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.profile_routine_change_prompt_body),
                        color = SmartTrainnerColors.Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            routineChangePromptOpen = false
                            routineLibraryOpenRequest += 1
                            navController.navigate(SmartTrainnerDestination.Routine.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.testTag("profile_confirm_routine_change")
                    ) {
                        Text(stringResource(R.string.profile_routine_change_prompt_confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { routineChangePromptOpen = false },
                        modifier = Modifier.testTag("profile_keep_current_routine")
                    ) {
                        Text(stringResource(R.string.profile_routine_change_prompt_dismiss))
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileButton(
    session: UserSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(SmartTrainnerColors.Coral)
            .clickable(onClick = onClick)
            .testTag("profile_button"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = session.profileInitial(),
            color = SmartTrainnerColors.SurfaceRaised,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileDrawer(
    session: UserSession,
    trainingExperience: TrainingExperience,
    googleSignInInProgress: Boolean,
    selectedThemeTone: SmartTrainnerThemeTone,
    onDismiss: () -> Unit,
    onThemeToneSelected: (SmartTrainnerThemeTone) -> Unit,
    onTrainingExperienceSelected: (TrainingExperience) -> Unit,
    onBodyProfileSaved: (ProfileGender?, Int, Double) -> Unit,
    onLinkGoogle: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var themeSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var trainingLevelSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var bodyProfileSettingsOpen by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f))
            .clickable(onClick = onDismiss)
            .testTag("profile_scrim")
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(304.dp)
                .clickable(onClick = {}),
            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            color = SmartTrainnerColors.SurfaceRaised,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = SmartTrainnerColors.CoralSoft,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = session.profileInitial(),
                            color = SmartTrainnerColors.Coral,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = session.nickname,
                        color = SmartTrainnerColors.Ink,
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (session.isLinked) {
                            stringResource(R.string.profile_linked_account)
                        } else {
                            stringResource(R.string.profile_local_account)
                        },
                        color = SmartTrainnerColors.Muted,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
                BodyProfileSettingsEntry(onClick = { bodyProfileSettingsOpen = true })
                TrainingLevelSettingsEntry(
                    trainingExperience = trainingExperience,
                    onClick = { trainingLevelSettingsOpen = true }
                )
                ThemeSettingsEntry(
                    selectedThemeTone = selectedThemeTone,
                    onClick = { themeSettingsOpen = true }
                )
                if (!session.isLinked) {
                    OutlinedButton(
                        onClick = onLinkGoogle,
                        enabled = !googleSignInInProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_link_google")
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                        Text(
                            text = stringResource(R.string.profile_link_google),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_logout")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Text(
                        text = stringResource(R.string.profile_logout),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        if (themeSettingsOpen) {
            ThemeSettingsDialog(
                selectedThemeTone = selectedThemeTone,
                onDismiss = { themeSettingsOpen = false },
                onThemeToneSelected = { themeTone ->
                    onThemeToneSelected(themeTone)
                    themeSettingsOpen = false
                }
            )
        }
        if (trainingLevelSettingsOpen) {
            TrainingLevelSettingsDialog(
                trainingExperience = trainingExperience,
                onDismiss = { trainingLevelSettingsOpen = false },
                onTrainingExperienceSelected = { experience ->
                    onTrainingExperienceSelected(experience)
                    trainingLevelSettingsOpen = false
                }
            )
        }
        if (bodyProfileSettingsOpen) {
            BodyProfileSettingsDialog(
                profile = session.profile,
                onDismiss = { bodyProfileSettingsOpen = false },
                onBodyProfileSaved = { gender, heightCm, weightKg ->
                    onBodyProfileSaved(gender, heightCm, weightKg)
                    bodyProfileSettingsOpen = false
                }
            )
        }
    }
}

@Composable
private fun BodyProfileSettingsEntry(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("profile_body_entry")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = SmartTrainnerColors.Coral,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.profile_body_change_action),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BodyProfileSettingsDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onBodyProfileSaved: (ProfileGender?, Int, Double) -> Unit
) {
    val measurement = profile.latestBodyMeasurement
    var selectedGender by rememberSaveable(profile.gender) { mutableStateOf(profile.gender) }
    var heightCmInput by rememberSaveable(measurement?.heightCm) {
        mutableStateOf(measurement?.heightCm?.toString() ?: "")
    }
    var weightKgInput by rememberSaveable(measurement?.weightKg) {
        mutableStateOf(measurement?.weightKg?.toDisplayWeight() ?: "")
    }
    val genderCanBeSet = profile.gender == null
    val heightCm = heightCmInput.toIntOrNull()
    val weightKg = weightKgInput.toDoubleOrNull()
    val canSave = selectedGender != null &&
        heightCm?.let { it > 0 } == true &&
        weightKg?.let { it > 0.0 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(8.dp),
        containerColor = SmartTrainnerColors.SurfaceRaised,
        title = {
            Text(
                text = stringResource(R.string.profile_body_title),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.login_gender_label),
                    color = SmartTrainnerColors.Ink,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                if (genderCanBeSet) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileGender.entries.forEach { gender ->
                            OutlinedButton(
                                onClick = { selectedGender = gender },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("profile_gender_${gender.name.lowercase()}")
                            ) {
                                Text(
                                    text = stringResource(gender.labelResId()),
                                    color = if (selectedGender == gender) {
                                        SmartTrainnerColors.Coral
                                    } else {
                                        SmartTrainnerColors.Ink
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SmartTrainnerColors.Surface,
                        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("profile_gender_locked")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(requireNotNull(profile.gender).labelResId()),
                                color = SmartTrainnerColors.Muted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = heightCmInput,
                    onValueChange = { heightCmInput = it.filter { character -> character.isDigit() }.take(3) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.login_height_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_height_input")
                )
                OutlinedTextField(
                    value = weightKgInput,
                    onValueChange = {
                        weightKgInput = it.filter { character -> character.isDigit() || character == '.' }
                            .take(6)
                    },
                    singleLine = true,
                    label = { Text(stringResource(R.string.login_weight_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_weight_input")
                )
                measurement?.let {
                    Text(
                        text = stringResource(R.string.profile_measurement_date, it.recordedDate.toString()),
                        color = SmartTrainnerColors.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onBodyProfileSaved(
                        if (genderCanBeSet) selectedGender else null,
                        requireNotNull(heightCm),
                        requireNotNull(weightKg)
                    )
                },
                enabled = canSave,
                modifier = Modifier.testTag("profile_save_body")
            ) {
                Text(stringResource(R.string.profile_body_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profile_theme_close))
            }
        }
    )
}

@Composable
private fun TrainingLevelSettingsEntry(
    trainingExperience: TrainingExperience,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("profile_training_level_entry")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = SmartTrainnerColors.Coral,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.profile_training_level_title),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(trainingExperience.profileLabelResId()),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ThemeSettingsEntry(
    selectedThemeTone: SmartTrainnerThemeTone,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, SmartTrainnerColors.Line),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("profile_theme_entry")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = selectedThemeTone.swatchColor(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
                modifier = Modifier.size(18.dp)
            ) {}
            Text(
                text = stringResource(R.string.profile_theme_title),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(selectedThemeTone.labelResId()),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TrainingLevelSettingsDialog(
    trainingExperience: TrainingExperience,
    onDismiss: () -> Unit,
    onTrainingExperienceSelected: (TrainingExperience) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(8.dp),
        containerColor = SmartTrainnerColors.SurfaceRaised,
        title = {
            Text(
                text = stringResource(R.string.profile_training_level_title),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TrainingExperience.entries.forEach { experience ->
                    TrainingExperienceOption(
                        experience = experience,
                        selected = trainingExperience == experience,
                        onClick = { onTrainingExperienceSelected(experience) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profile_theme_close))
            }
        }
    )
}

@Composable
private fun TrainingExperienceOption(
    experience: TrainingExperience,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) SmartTrainnerColors.CoralSoft else SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, if (selected) SmartTrainnerColors.Coral else SmartTrainnerColors.Line),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("profile_training_level_${experience.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(experience.profileLabelResId()),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SmartTrainnerColors.Coral,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Spacer(Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ThemeSettingsDialog(
    selectedThemeTone: SmartTrainnerThemeTone,
    onDismiss: () -> Unit,
    onThemeToneSelected: (SmartTrainnerThemeTone) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(8.dp),
        containerColor = SmartTrainnerColors.SurfaceRaised,
        title = {
            Text(
                text = stringResource(R.string.profile_theme_title),
                color = SmartTrainnerColors.Ink,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmartTrainnerThemeTone.entries.forEach { themeTone ->
                    ThemeToneOption(
                        themeTone = themeTone,
                        selected = selectedThemeTone == themeTone,
                        onClick = { onThemeToneSelected(themeTone) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.profile_theme_close))
            }
        }
    )
}

@Composable
private fun ThemeToneOption(
    themeTone: SmartTrainnerThemeTone,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) SmartTrainnerColors.CoralSoft else SmartTrainnerColors.Surface,
        border = BorderStroke(1.dp, if (selected) SmartTrainnerColors.Coral else SmartTrainnerColors.Line),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("profile_theme_${themeTone.storageValue}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = themeTone.swatchColor(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
                modifier = Modifier.size(18.dp)
            ) {}
            Text(
                text = stringResource(themeTone.labelResId()),
                color = SmartTrainnerColors.Ink,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SmartTrainnerColors.Coral,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Spacer(Modifier.size(18.dp))
            }
        }
    }
}

private fun SmartTrainnerThemeTone.labelResId(): Int = when (this) {
    SmartTrainnerThemeTone.Red -> R.string.profile_theme_red
    SmartTrainnerThemeTone.Blue -> R.string.profile_theme_blue
    SmartTrainnerThemeTone.Green -> R.string.profile_theme_green
    SmartTrainnerThemeTone.Black -> R.string.profile_theme_black
}

private fun TrainingExperience.profileLabelResId(): Int = when (this) {
    TrainingExperience.BEGINNER -> R.string.profile_training_level_beginner
    TrainingExperience.INTERMEDIATE -> R.string.profile_training_level_intermediate
    TrainingExperience.ADVANCED -> R.string.profile_training_level_advanced
}

private fun ProfileGender.labelResId(): Int = when (this) {
    ProfileGender.MALE -> R.string.profile_gender_male
    ProfileGender.FEMALE -> R.string.profile_gender_female
}

private fun Double.toDisplayWeight(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }

private fun UserSession.profileInitial(): String =
    nickname.ifBlank { displayName }.trim().take(1).uppercase()

@Composable
private fun SmartTrainnerBottomBar(
    destinations: List<SmartTrainnerDestination>,
    currentRoute: String,
    onDestinationSelected: (SmartTrainnerDestination) -> Unit
) {
    val dividerColor = SmartTrainnerColors.Line

    NavigationBar(
        containerColor = SmartTrainnerColors.SurfaceRaised,
        modifier = Modifier
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onDestinationSelected(destination) },
                modifier = Modifier.testTag(destination.testTag),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SmartTrainnerColors.Coral,
                    selectedTextColor = SmartTrainnerColors.Coral,
                    indicatorColor = SmartTrainnerColors.CoralSoft,
                    unselectedIconColor = SmartTrainnerColors.Muted,
                    unselectedTextColor = SmartTrainnerColors.Muted
                ),
                icon = { Icon(destination.icon(), contentDescription = null) },
                label = { Text(stringResource(destination.labelResId)) }
            )
        }
    }
}

private enum class SmartTrainnerDestination(
    val route: String,
    val labelResId: Int,
    val testTag: String
) {
    Home(
        route = "training/home",
        labelResId = R.string.app_destination_home,
        testTag = "training_tab_home"
    ),
    Routine(
        route = "training/routine",
        labelResId = R.string.app_destination_routine,
        testTag = "training_tab_plan"
    ),
    Exercises(
        route = "training/exercises",
        labelResId = R.string.app_destination_exercises,
        testTag = "training_tab_exercises"
    ),
    Calendar(
        route = "training/calendar",
        labelResId = R.string.app_destination_calendar,
        testTag = "training_tab_calendar"
    ),
    Analysis(
        route = "training/analysis",
        labelResId = R.string.app_destination_analysis,
        testTag = "training_tab_analysis"
    ),
    Friends(
        route = "training/friends",
        labelResId = R.string.app_destination_friends,
        testTag = "training_tab_friends"
    )
}

private fun SmartTrainnerDestination.icon(): ImageVector = when (this) {
    SmartTrainnerDestination.Home -> Icons.Default.Home
    SmartTrainnerDestination.Routine -> Icons.Default.DateRange
    SmartTrainnerDestination.Exercises -> Icons.Default.FitnessCenter
    SmartTrainnerDestination.Calendar -> Icons.Default.CalendarMonth
    SmartTrainnerDestination.Analysis -> Icons.Default.BarChart
    SmartTrainnerDestination.Friends -> Icons.Default.Group
}

private val SmartTrainnerDestination.isTrainingDestination: Boolean
    get() = when (this) {
        SmartTrainnerDestination.Home,
        SmartTrainnerDestination.Routine,
        SmartTrainnerDestination.Exercises -> true
        SmartTrainnerDestination.Calendar,
        SmartTrainnerDestination.Friends,
        SmartTrainnerDestination.Analysis -> false
    }
