package com.smarttrainner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smarttrainner.core.designsystem.SmartTrainnerTheme
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.training.api.TrainingFeatureEntry
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var analysisFeatureEntry: AnalysisFeatureEntry

    @Inject
    lateinit var trainingFeatureEntry: TrainingFeatureEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartTrainnerTheme {
                SmartTrainnerApp(
                    analysisFeatureEntry = analysisFeatureEntry,
                    trainingFeatureEntry = trainingFeatureEntry
                )
            }
        }
    }
}
