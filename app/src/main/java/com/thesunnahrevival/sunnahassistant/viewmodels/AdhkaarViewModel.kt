package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarItem
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarItemRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarResourcesNextActionRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.FlagRepository
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.NextAction
import com.thesunnahrevival.sunnahassistant.data.repositories.ResourcesNextActionRepository.NextActionsData
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val HAS_SEEN_SWIPE_ADHKAAR_TUTORIAL = "has_seen_swipe_adhkaar_tutorial"

class AdhkaarViewModel(application: Application) : AndroidViewModel(application) {
    private val adhkaarItemRepository = AdhkaarItemRepository.getInstance(application)
    private val isLoading: MutableSharedFlow<Boolean> = MutableSharedFlow()
    private val flagRepository = FlagRepository.getInstance(getApplication())
    private val adhkaarResourcesNextActionRepository = AdhkaarResourcesNextActionRepository.getInstance(application)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.emit(true)
            adhkaarItemRepository.loadAdhkaarItems()
            isLoading.emit(false)
        }
    }

    fun getAdhkaarItemsByChapterId(chapterId: Int): StateFlow<AdhkaarUiState> {
        return combine(
            adhkaarItemRepository.getAdhkaarItemsByChapterId(chapterId).onStart { emit(listOf()) },
            adhkaarResourcesNextActionRepository.getAdhkaarNextActions(chapterId).onStart { emit(NextActionsData()) }
        ){ adhkaarItems, nextActions ->
            val processedAdhkaarItems = processAdhkaarItems(items = adhkaarItems)
            AdhkaarUiState(
                adhkaarItems = processedAdhkaarItems,
                adhkaarGroups = groupAdhkaarItemsByReference(processedAdhkaarItems),
                isLoading = adhkaarItems.isEmpty(),
                nextAction = nextActions.nextActions.firstOrNull()
            )
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

    fun setBookmarks(items: List<AdhkaarDisplayItem>, shouldBookmark: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            items.forEach { item ->
                if (item.bookmarked != shouldBookmark) {
                    adhkaarItemRepository.toggleBookmark(item.itemId)
                }
            }
        }
    }

    suspend fun setHasSeenSwipeAdhkaarTutorial() {
        flagRepository.setFlag(HAS_SEEN_SWIPE_ADHKAAR_TUTORIAL, 1)
    }

    fun swipeAdhkaarTutorialStatus(): Flow<Int?> {
        return flagRepository.getIntFlagFlow(HAS_SEEN_SWIPE_ADHKAAR_TUTORIAL)
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

    private fun groupAdhkaarItemsByReference(items: List<AdhkaarDisplayItem>): List<AdhkaarDisplayGroup> {
        val groups = mutableListOf<MutableList<AdhkaarDisplayItem>>()
        var currentGroup = mutableListOf<AdhkaarDisplayItem>()
        var previousReferenceKey: String? = null

        items.forEach { item ->
            val currentReferenceKey = item.reference?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

            if (currentGroup.isNotEmpty()) {
                if (currentReferenceKey != null && previousReferenceKey == currentReferenceKey) {
                    currentGroup.add(item)
                } else {
                    groups.add(currentGroup)
                    currentGroup = mutableListOf(item)
                }
            } else {
                currentGroup.add(item)
            }

            previousReferenceKey = currentReferenceKey
        }

        if (currentGroup.isNotEmpty()) {
            groups.add(currentGroup)
        }

        return groups.map { groupItems ->
            val groupItemIds = groupItems.map { it.itemId }
            AdhkaarDisplayGroup(
                itemIds = groupItemIds,
                items = groupItems,
                reference = groupItems.firstOrNull { !it.reference.isNullOrBlank() }?.reference,
                bookmarked = groupItems.all { it.bookmarked }
            )
        }
    }
    
    data class AdhkaarUiState(
        val adhkaarItems: List<AdhkaarDisplayItem> = emptyList(),
        val adhkaarGroups: List<AdhkaarDisplayGroup> = emptyList(),
        val isLoading: Boolean = false,
        val nextAction: NextAction? = null
    )
    
    data class AdhkaarDisplayItem(
        val itemId: Int,
        val arabicText: String?,
        val englishText: String?,
        val reference: String?,
        val bookmarked: Boolean = false
    )

    data class AdhkaarDisplayGroup(
        val itemIds: List<Int>,
        val items: List<AdhkaarDisplayItem>,
        val reference: String?,
        val bookmarked: Boolean
    )
}
