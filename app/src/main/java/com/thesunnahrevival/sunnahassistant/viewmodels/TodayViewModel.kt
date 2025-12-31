package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.repositories.ToDoNudgeRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.ToDoNudgeRepository.Nudge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodayViewModel(application: Application) : AndroidViewModel(application) {

    private val toDoNudgeRepository = ToDoNudgeRepository.getInstance(getApplication())

    private val _settingsState = MutableStateFlow<SettingsState?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val nudge: StateFlow<Nudge?> = _settingsState.flatMapLatest { state ->
        if (state != null) {
            toDoNudgeRepository.getNudge(
                isPrayerAlertsEnabled = state.isPrayerAlertsEnabled,
                hasLocation = state.hasLocation
            )
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun updateSettings(isPrayerAlertsEnabled: Boolean, hasLocation: Boolean) {
        _settingsState.value = SettingsState(isPrayerAlertsEnabled, hasLocation)
    }

    fun dismissNudgesForToday() {
        viewModelScope.launch(Dispatchers.IO) {
            toDoNudgeRepository.dismissNudgesForToday()
        }
    }

    private data class SettingsState(
        val isPrayerAlertsEnabled: Boolean,
        val hasLocation: Boolean
    )
}
