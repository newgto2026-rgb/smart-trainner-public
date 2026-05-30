package com.smarttrainner.feature.analysis.impl

import androidx.compose.runtime.Composable
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.analysis.api.AnalysisUiState
import javax.inject.Inject

class AnalysisFeatureEntryImpl @Inject constructor() : AnalysisFeatureEntry {
    @Composable
    override fun Content(state: AnalysisUiState) {
        AnalysisContent(state = state)
    }
}
