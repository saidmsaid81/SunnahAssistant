package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarResourcesNextActionRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.NextActionsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResourcesNextActionViewModel(application: Application) : AndroidViewModel(application) {
    private val resourcesNextActionRepository = ResourcesNextActionRepository.getInstance(getApplication())
    private val adhkaarResourcesNextActionRepository = AdhkaarResourcesNextActionRepository.getInstance(getApplication())

    private val _nextActionsData = MutableStateFlow<NextActionsData?>(null)
    val nextActionsData: StateFlow<NextActionsData?> = _nextActionsData.asStateFlow()

    fun loadQuranNextActions(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _nextActionsData.value = resourcesNextActionRepository.getNextActions(page)
        }
    }

    fun loadAdhkaarNextActions(chapterId: Int) {
        viewModelScope.launch {
            adhkaarResourcesNextActionRepository.getAdhkaarNextActions(chapterId).collect {
                _nextActionsData.value = it
            }
        }
    }

}