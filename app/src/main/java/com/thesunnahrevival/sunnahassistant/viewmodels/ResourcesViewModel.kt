package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.*
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarChapterRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.SurahRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.TrainingInfoRepository
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ResourcesViewModel(application: Application) : AndroidViewModel(application), AdhkaarChapterPinnable,
    SurahPinnable {
    private val repository: ResourcesRepository =
        ResourcesRepository.getInstance(application)

    private val surahRepository: SurahRepository =
        SurahRepository.getInstance(application)

    private val adhkaarChapterRepository: AdhkaarChapterRepository =
        AdhkaarChapterRepository.getInstance(application)

    private val trainingInfoRepository: TrainingInfoRepository =
        TrainingInfoRepository.getInstance(application)

    private val pinHelper = PinHelper(this, adhkaarChapterRepository, surahRepository)

    private val _uiState = MutableStateFlow(ResourcesUIState())
    val uiState: StateFlow<ResourcesUIState> = _uiState.asStateFlow()

    private val _lastReadSurah = MutableStateFlow<Surah?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                repository.prepopulateResourcesData()

                val locale = getApplication<Application>().getLocale()
                val language = if (locale.language.startsWith("ar")) "ar" else "en"

                
                combine(
                    surahRepository.getFirst3Surahs(),
                    _lastReadSurah,
                    adhkaarChapterRepository.getFirstThreeChapters(language)
                ) { surahs, lastReadSurah, adhkaarChapters ->
                    ResourcesUIState(
                        isLoading = false,
                        surahs = surahs,
                        lastReadSurah = lastReadSurah,
                        resourceItems = repository.resourceItems(),
                        adhkaarChapters = adhkaarChapters,
                        error = null
                    )
                }.collect { newState ->
                    _uiState.value = newState
                    loadTrainingSteps()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setLastReadPage(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val surah = surahRepository.getSurahByPage(page)
                surah?.let {
                    val modifiedSurah = it.copy(startPage = page)
                    _lastReadSurah.emit(modifiedSurah)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    override fun toggleChapterPin(chapterId: Int, onResult: (AdhkaarChapterRepository.PinResult) -> Unit) {
        pinHelper.toggleChapterPin(chapterId, onResult)
    }

    override fun toggleSurahPin(surahId: Int, onResult: (SurahRepository.PinResult) -> Unit) {
        pinHelper.toggleSurahPin(surahId, onResult)
    }

    private fun loadTrainingSteps() {
        viewModelScope.launch(Dispatchers.IO) {
            val nextQuranStep = trainingInfoRepository.getNextTrainingStep(TrainingSection.QURAN_RESOURCE_SECTION)
            val nextAdhkaarStep = trainingInfoRepository.getNextTrainingStep(TrainingSection.ADHKAAR_RESOURCE_SECTION)

            _uiState.value = _uiState.value.copy(
                currentQuranTrainingStep = nextQuranStep,
                currentAdhkaarTrainingStep = nextAdhkaarStep
            )
        }
    }

    fun onTrainingActionCompleted(section: TrainingSection) {
        viewModelScope.launch(Dispatchers.IO) {
            trainingInfoRepository.completeCurrentTrainingStep(section)
            loadTrainingSteps()
        }
    }
}

data class ResourcesUIState(
    val isLoading: Boolean = true,
    val surahs: List<Surah> = emptyList(),
    val lastReadSurah: Surah? = null,
    val resourceItems: List<ResourceItem> = emptyList(),
    val adhkaarChapters: List<AdhkaarChapter> = emptyList(),
    val error: String? = null,
    val currentQuranTrainingStep: TrainingStep? = null,
    val currentAdhkaarTrainingStep: TrainingStep? = null
)