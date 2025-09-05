package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarItem
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarItemRepository
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdhkaarViewModel(application: Application) : AndroidViewModel(application) {
    private val adhkaarItemRepository = AdhkaarItemRepository.getInstance(application)
    private val isLoading: MutableSharedFlow<Boolean> = MutableSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.emit(true)
            adhkaarItemRepository.loadAdhkaarItems()
            isLoading.emit(false)
        }
    }

    fun getAdhkaarItemsByChapterId(chapterId: Int): StateFlow<AdhkaarUiState> {
        return adhkaarItemRepository.getAdhkaarItemsByChapterId(chapterId).map { adhkaarItems ->
            AdhkaarUiState(processAdhkaarItems(adhkaarItems), isLoading = adhkaarItems.isEmpty())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AdhkaarUiState(adhkaarItems = listOf(), isLoading = false)
        )
    }

    suspend fun getChapterNameByChapterId(id: Int): String? {
        return adhkaarItemRepository.getChapterNameByChapterId(id, getLocale().language)
    }

    fun toggleBookmark(itemId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            adhkaarItemRepository.toggleBookmark(itemId)
        }
    }
    
    private fun processAdhkaarItems(items: List<AdhkaarItem>): List<AdhkaarDisplayItem> {
        val locale = getApplication<Application>().getLocale()

        val isDeviceArabic = locale.language.equals("ar", ignoreCase = true)
        
        val groupedItems = items.groupBy { it.itemId }
        
        return groupedItems.map { (itemId, itemList) ->
            val arabicItem = itemList.find { it.language == "ar" }
            val englishItem = itemList.find { it.language == "en" }
            
            AdhkaarDisplayItem(
                itemId = itemId,
                arabicText = arabicItem?.itemTranslation,
                englishText = if (!isDeviceArabic) englishItem?.itemTranslation else null,
                reference = if (!isDeviceArabic) englishItem?.reference else arabicItem?.reference,
                bookmarked = arabicItem?.bookmarked ?: englishItem?.bookmarked ?: false
            )
        }.sortedBy { it.itemId }
    }
    
    data class AdhkaarUiState(
        val adhkaarItems: List<AdhkaarDisplayItem> = emptyList(),
        val isLoading: Boolean = false
    )
    
    data class AdhkaarDisplayItem(
        val itemId: Int,
        val arabicText: String?,
        val englishText: String?,
        val reference: String?,
        val bookmarked: Boolean = false
    )
}