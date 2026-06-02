package com.smarttrainner.app

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.app.di.ThemePreferenceStore
import com.smarttrainner.core.designsystem.SmartTrainnerTheme
import com.smarttrainner.core.designsystem.SmartTrainnerThemeTone
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var analysisFeatureEntry: AnalysisFeatureEntry

    @Inject
    lateinit var exerciseCatalogFeatureEntry: ExerciseCatalogFeatureEntry

    @Inject
    lateinit var exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry

    @Inject
    lateinit var routineFeatureEntry: RoutineFeatureEntry

    @Inject
    lateinit var workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry

    @Inject
    lateinit var themePreferenceStore: ThemePreferenceStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val selectedThemeTone by themePreferenceStore.selectedThemeTone.collectAsStateWithLifecycle(
                initialValue = SmartTrainnerThemeTone.Default
            )
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
                    exerciseCatalogFeatureEntry = exerciseCatalogFeatureEntry,
                    exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
                    routineFeatureEntry = routineFeatureEntry,
                    workoutRecordingFeatureEntry = workoutRecordingFeatureEntry,
                    selectedThemeTone = selectedThemeTone,
                    onThemeToneSelected = { themeTone ->
                        scope.launch {
                            themePreferenceStore.setSelectedThemeTone(themeTone)
                        }
                    }
                )
            }
        }
    }
}
