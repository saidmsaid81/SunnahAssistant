package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.NextAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResourcesNextActionViewModel(application: Application) : AndroidViewModel(application) {
    private val resourcesNextActionRepository = ResourcesNextActionRepository.getInstance(getApplication())

    private val _nextActions = MutableStateFlow<List<NextAction>>(emptyList())
    val nextActions: StateFlow<List<NextAction>> = _nextActions.asStateFlow()

    fun loadNextActions(page: Int) {
        viewModelScope.launch {
            _nextActions.value = resourcesNextActionRepository.getNextActions(page)
        }
    }

}