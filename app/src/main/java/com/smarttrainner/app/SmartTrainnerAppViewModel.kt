package com.smarttrainner.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrainner.core.domain.ObserveActiveSessionUseCase
import com.smarttrainner.core.domain.StartDefaultSessionUseCase
import com.smarttrainner.core.model.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SmartTrainnerAppUiState(
    val activeSession: UserSession? = null,
    val isLoading: Boolean = true,
    val loginFailed: Boolean = false
)

@HiltViewModel
class SmartTrainnerAppViewModel @Inject constructor(
    observeActiveSession: ObserveActiveSessionUseCase,
    private val startDefaultSession: StartDefaultSessionUseCase
) : ViewModel() {
    val uiState: StateFlow<SmartTrainnerAppUiState> = observeActiveSession()
        .map { session ->
            SmartTrainnerAppUiState(activeSession = session, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SmartTrainnerAppUiState()
        )

    fun continueWithDefaultSession() {
        viewModelScope.launch {
            startDefaultSession()
        }
    }
}
