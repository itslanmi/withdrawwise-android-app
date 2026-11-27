package io.github.itslanmi.withdrawwise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.itslanmi.withdrawwise.domain.model.ThemeMode
import io.github.itslanmi.withdrawwise.domain.repository.PreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface AppUiState {
    data object Loading : AppUiState

    data class Ready(val onboardingComplete: Boolean, val themeMode: ThemeMode) : AppUiState
}

@HiltViewModel
class AppViewModel
@Inject
constructor(preferences: PreferencesRepository) : ViewModel() {
    val state: StateFlow<AppUiState> =
        preferences.preferences
            .map { AppUiState.Ready(onboardingComplete = it.onboardingComplete, themeMode = it.themeMode) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, AppUiState.Loading)
}
