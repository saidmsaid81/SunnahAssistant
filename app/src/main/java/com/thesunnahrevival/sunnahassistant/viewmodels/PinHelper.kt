package com.thesunnahrevival.sunnahassistant.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarChapterRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.SurahRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface AdhkaarChapterPinnable {
    fun toggleChapterPin(chapterId: Int, onResult: (AdhkaarChapterRepository.PinResult) -> Unit)
}

interface SurahPinnable {
    fun toggleSurahPin(surahId: Int, onResult: (SurahRepository.PinResult) -> Unit)
}

class PinHelper(
    private val viewModel: ViewModel,
    private val adhkaarChapterRepository: AdhkaarChapterRepository? = null,
    private val surahRepository: SurahRepository? = null
) : AdhkaarChapterPinnable, SurahPinnable {

    override fun toggleChapterPin(chapterId: Int, onResult: (AdhkaarChapterRepository.PinResult) -> Unit) {
        requireNotNull(adhkaarChapterRepository) { "AdhkaarChapterRepository must be provided for chapter pinning" }
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val result = adhkaarChapterRepository.toggleChapterPin(chapterId)
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    override fun toggleSurahPin(surahId: Int, onResult: (SurahRepository.PinResult) -> Unit) {
        requireNotNull(surahRepository) { "SurahRepository must be provided for surah pinning" }
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val result = surahRepository.toggleSurahPin(surahId)
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }
}
