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
    private val _translationsWithUpdatesAvailable = MutableStateFlow<Set<Int>>(emptySet())
    private val _isCheckingForTranslationUpdates = MutableStateFlow(false)
    private val _isUpdatingInstalledTranslations = MutableStateFlow(false)

    val translationUiState = combine(
        quranTranslationRepository.getTranslations(),
        _translationsDownloadInProgress,
        _translationsWithUpdatesAvailable,
        _isCheckingForTranslationUpdates,
        _isUpdatingInstalledTranslations
    ) { translations, downloadingIds, updatesAvailableIds, isCheckingForUpdates, isUpdatingInstalled ->
        TranslationUiState(
            allTranslations = translations,
            selectedTranslations = translations.filter { it.selected },
            translationsDownloadInProgress = translations.filter { it.id in downloadingIds },
            translationsWithUpdatesAvailable = translations.filter { it.id in updatesAvailableIds },
            updatesAvailableCount = updatesAvailableIds.size,
            isCheckingForUpdates = isCheckingForUpdates,
            isUpdatingInstalledTranslations = isUpdatingInstalled
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
            val didUpdate = quranTranslationRepository.updateTranslation(updatedTranslation)
            if (!didUpdate) {
                quranTranslationRepository.updateTranslation(translation)
            }

            _translationsDownloadInProgress.value = _translationsDownloadInProgress.value - translation.id
            refreshInstalledTranslationUpdates()
            if (didUpdate) {
                callback()
            }
        }
    }

    fun refreshInstalledTranslationUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            _isCheckingForTranslationUpdates.value = true
            _translationsWithUpdatesAvailable.value = quranTranslationRepository.getInstalledTranslationUpdates()
            _isCheckingForTranslationUpdates.value = false
        }
    }

    fun updateInstalledTranslations(onComplete: suspend (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _isUpdatingInstalledTranslations.value = true
            val updatesToDownload = _translationsWithUpdatesAvailable.value
            _translationsDownloadInProgress.value = _translationsDownloadInProgress.value + updatesToDownload

            val result = quranTranslationRepository.updateInstalledTranslations()
            _translationsDownloadInProgress.value = _translationsDownloadInProgress.value - updatesToDownload
            _translationsWithUpdatesAvailable.value = quranTranslationRepository.getInstalledTranslationUpdates()

            _isUpdatingInstalledTranslations.value = false
            onComplete(result.failedTranslationIds.isEmpty())
        }
    }

    fun updateSingleInstalledTranslation(
        translation: Translation,
        onComplete: suspend (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _translationsDownloadInProgress.value = _translationsDownloadInProgress.value + translation.id
            val didUpdate = quranTranslationRepository.updateInstalledTranslation(translation.id)
            _translationsDownloadInProgress.value = _translationsDownloadInProgress.value - translation.id
            _translationsWithUpdatesAvailable.value = quranTranslationRepository.getInstalledTranslationUpdates()
            onComplete(didUpdate)
        }
    }

    data class TranslationUiState(
        val allTranslations: List<Translation> = listOf(),
        val selectedTranslations: List<Translation> = listOf(),
        val translationsDownloadInProgress: List<Translation> = listOf(),
        val translationsWithUpdatesAvailable: List<Translation> = listOf(),
        val updatesAvailableCount: Int = 0,
        val isCheckingForUpdates: Boolean = false,
        val isUpdatingInstalledTranslations: Boolean = false
    )
}
