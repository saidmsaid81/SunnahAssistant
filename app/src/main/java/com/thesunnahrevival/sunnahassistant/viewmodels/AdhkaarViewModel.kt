package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.AdhkaarItem
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarItemRepository
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
    
    private fun processAdhkaarItems(items: List<AdhkaarItem>): List<AdhkaarDisplayItem> {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getApplication<Application>().resources.configuration.locales[0]
        } else {
            getApplication<Application>().resources.configuration.locale
        }
        
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
        val reference: String?
    )
}