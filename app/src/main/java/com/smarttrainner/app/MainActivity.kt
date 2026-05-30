package com.smarttrainner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smarttrainner.core.designsystem.SmartTrainnerTheme
import com.smarttrainner.core.ui.ExerciseMediaRenderer
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseCatalogFeatureEntry
import com.smarttrainner.feature.exercise.api.ExerciseDetailFeatureEntry
import com.smarttrainner.feature.routine.api.RoutineFeatureEntry
import com.smarttrainner.feature.workout.api.WorkoutRecordingFeatureEntry
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var analysisFeatureEntry: AnalysisFeatureEntry

    @Inject
    lateinit var exerciseCatalogFeatureEntry: ExerciseCatalogFeatureEntry

    @Inject
    lateinit var exerciseDetailFeatureEntry: ExerciseDetailFeatureEntry

    @Inject
    lateinit var exerciseMediaRenderer: ExerciseMediaRenderer

    @Inject
    lateinit var routineFeatureEntry: RoutineFeatureEntry

    @Inject
    lateinit var workoutRecordingFeatureEntry: WorkoutRecordingFeatureEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartTrainnerTheme {
                SmartTrainnerApp(
                    analysisFeatureEntry = analysisFeatureEntry,
                    exerciseCatalogFeatureEntry = exerciseCatalogFeatureEntry,
                    exerciseDetailFeatureEntry = exerciseDetailFeatureEntry,
                    exerciseMediaRenderer = exerciseMediaRenderer,
                    routineFeatureEntry = routineFeatureEntry,
                    workoutRecordingFeatureEntry = workoutRecordingFeatureEntry
                )
            }
        }
    }
}
