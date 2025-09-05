package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.entity.Translation
import com.thesunnahrevival.sunnahassistant.data.repositories.QuranTranslationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class TranslationViewModel(application: Application) : AndroidViewModel(application) {
    private val quranTranslationRepository =
        QuranTranslationRepository.getInstance(application)

    private val _translationsDownloadInProgress = MutableStateFlow<Set<Int>>(emptySet())

    val translationUiState = combine(
        quranTranslationRepository.getTranslations(),
        _translationsDownloadInProgress
    ) { translations, downloadingIds ->
        TranslationUiState(
            allTranslations = translations,
            selectedTranslations = translations.filter { it.selected },
            translationsDownloadInProgress = translations.filter { it.id in downloadingIds }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TranslationUiState()
    )

    fun toggleTranslationSelection(translation: Translation, order: Int, callback: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _translationsDownloadInProgress.value = _translationsDownloadInProgress.value + translation.id

            val updatedTranslation = translation.copy(selected = !translation.selected)
            updatedTranslation.order = if (updatedTranslation.selected) order else null
            quranTranslationRepository.updateTranslation(updatedTranslation)

            _translationsDownloadInProgress.value = _translationsDownloadInProgress.value - translation.id
            callback()
        }
    }

    data class TranslationUiState(
        val allTranslations: List<Translation> = listOf(),
        val selectedTranslations: List<Translation> = listOf(),
        val translationsDownloadInProgress: List<Translation> = listOf()
    )
}