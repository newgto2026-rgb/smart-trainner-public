package com.smarttrainner.feature.analysis.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import com.smarttrainner.feature.analysis.api.AnalysisUiState
import javax.inject.Inject

class AnalysisFeatureEntryImpl @Inject constructor() : AnalysisFeatureEntry {
    @Composable
    override fun Route() {
        val viewModel: AnalysisViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        AnalysisContent(state = state)
    }

    @Composable
    override fun Content(state: AnalysisUiState) {
        AnalysisContent(state = state)
    }
}
