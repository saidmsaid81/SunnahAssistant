package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarResourcesNextActionRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.NextActionsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResourcesNextActionViewModel(application: Application) : AndroidViewModel(application) {
    private val resourcesNextActionRepository = ResourcesNextActionRepository.getInstance(getApplication())
    private val adhkaarResourcesNextActionRepository = AdhkaarResourcesNextActionRepository.getInstance(getApplication())

    private val _nextActionsData = MutableStateFlow<NextActionsData?>(null)
    val nextActionsData: StateFlow<NextActionsData?> = _nextActionsData.asStateFlow()

    private var lastQuranPage: Int? = null
    private var lastAdhkaarChapterId: Int? = null

    fun loadQuranNextActions(page: Int) {
        lastQuranPage = page
        lastAdhkaarChapterId = null
        viewModelScope.launch(Dispatchers.IO) {
            _nextActionsData.value = resourcesNextActionRepository.getNextActions(page)
        }
    }

    fun loadAdhkaarNextActions(chapterId: Int) {
        lastAdhkaarChapterId = chapterId
        lastQuranPage = null
        viewModelScope.launch {
            adhkaarResourcesNextActionRepository.getAdhkaarNextActions(chapterId).collect {
                _nextActionsData.value = it
            }
        }
    }

    fun markToDoAsComplete(toDoId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            resourcesNextActionRepository.markToDoAsComplete(toDoId)
            lastQuranPage?.let {
                _nextActionsData.value = resourcesNextActionRepository.getNextActions(it)
            }
            lastAdhkaarChapterId?.let {
                _nextActionsData.value = adhkaarResourcesNextActionRepository.getAdhkaarNextActions(it).first()
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(getApplication(), R.string.marked_as_complete, Toast.LENGTH_SHORT).show()
            }
        }
    }

}
