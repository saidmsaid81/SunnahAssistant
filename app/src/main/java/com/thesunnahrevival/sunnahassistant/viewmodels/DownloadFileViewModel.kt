package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.QuranRepository
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadFileViewModel(application: Application) : AndroidViewModel(application) {
    private val mQuranRepository: QuranRepository =
        QuranRepository.getInstance(application)

    val translations = mQuranRepository.getTranslations()

    val selectedTranslations = translations.map { translationsList ->
        translationsList.filter { it.selected }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleTranslationSelection(translation: Translation) {
        translation.selected = !translation.selected
        viewModelScope.launch(Dispatchers.IO) {
            mQuranRepository.updateTranslation(translation)
        }
    }
}