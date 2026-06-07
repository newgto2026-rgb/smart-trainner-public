package com.smarttrainner.app

import android.Manifest
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.FirebaseApp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.app.di.ThemePreferenceStore
import com.smarttrainner.core.designsystem.SmartTrainnerTheme
import com.smarttrainner.core.designsystem.SmartTrainnerThemeTone
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.calendar.api.CalendarFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.friend.api.FriendFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal const val EXTRA_OPEN_DESTINATION = "com.smarttrainner.extra.OPEN_DESTINATION"
internal const val OPEN_DESTINATION_FRIENDS = "friends"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var analysisFeatureEntry: AnalysisFeatureEntry

    @Inject
    lateinit var calendarFeatureEntry: CalendarFeatureEntry

    @Inject
    lateinit var exerciseCatalogFeatureEntry: ExerciseCatalogFeatureEntry

    @Inject
    lateinit var exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry

    @Inject
    lateinit var friendFeatureEntry: FriendFeatureEntry

    @Inject
    lateinit var routineFeatureEntry: RoutineFeatureEntry

    @Inject
    lateinit var workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry

    @Inject
    lateinit var themePreferenceStore: ThemePreferenceStore

    private val friendNavigationRequests = MutableStateFlow(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consumeNotificationIntent(intent)
        requestNotificationPermissionIfNeeded()
        setContent {
            val selectedThemeTone by themePreferenceStore.selectedThemeTone.collectAsStateWithLifecycle(
                initialValue = SmartTrainnerThemeTone.Default
            )
            val friendNavigationRequest by friendNavigationRequests.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()
            LaunchedEffect(selectedThemeTone) {
                val transparent = AndroidColor.TRANSPARENT
                if (selectedThemeTone == SmartTrainnerThemeTone.Black) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(transparent),
                        navigationBarStyle = SystemBarStyle.dark(transparent)
                    )
                } else {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(transparent, transparent),
                        navigationBarStyle = SystemBarStyle.light(transparent, transparent)
                    )
                }
            }
            SmartTrainnerTheme(themeTone = selectedThemeTone) {
                SmartTrainnerApp(
                    analysisFeatureEntry = analysisFeatureEntry,
                    calendarFeatureEntry = calendarFeatureEntry,
                    exerciseCatalogFeatureEntry = exerciseCatalogFeatureEntry,
                    exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
                    friendFeatureEntry = friendFeatureEntry,
                    routineFeatureEntry = routineFeatureEntry,
                    workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
                    selectedThemeTone = selectedThemeTone,
                    friendNavigationRequest = friendNavigationRequest,
                    onThemeToneSelected = { themeTone ->
                        scope.launch {
                            themePreferenceStore.setSelectedThemeTone(themeTone)
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeNotificationIntent(intent)
    }

    private fun consumeNotificationIntent(intent: Intent?) {
        if (intent?.getStringExtra(EXTRA_OPEN_DESTINATION) == OPEN_DESTINATION_FRIENDS) {
            friendNavigationRequests.value += 1
            intent.removeExtra(EXTRA_OPEN_DESTINATION)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (FirebaseApp.getApps(this).isEmpty()) return
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
        }
    }

    private companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1027
    }
}
