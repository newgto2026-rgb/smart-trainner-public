package com.smarttrainner.feature.analysis.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttrainner.core.ui.SmartTrainnerScreenChrome
import com.smarttrainner.core.ui.SmartTrainnerScreenScaffold
import com.smarttrainner.feature.analysis.api.AnalysisFeatureEntry
import javax.inject.Inject

class AnalysisFeatureEntryImpl @Inject constructor() : AnalysisFeatureEntry {
    @Composable
    override fun Route() {
        val viewModel: AnalysisViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        SmartTrainnerScreenScaffold(
            chrome = SmartTrainnerScreenChrome(
                title = stringResource(R.string.analysis_route_title),
                subtitle = stringResource(R.string.analysis_route_subtitle)
            )
        ) {
            item {
                AnalysisContent(state = state)
            }
        }
    }
}
