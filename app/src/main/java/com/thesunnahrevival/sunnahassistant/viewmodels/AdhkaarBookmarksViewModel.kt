package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.thesunnahrevival.sunnahassistant.data.model.embedded.BookmarkedAdhkaarDataEmbedded
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarItemRepository
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class AdhkaarBookmarksViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AdhkaarItemRepository.getInstance(application)

    private val searchQuery = MutableStateFlow<String?>(null)

    @OptIn(FlowPreview::class)
    val bookmarkedItemsFlow: Flow<List<BookmarkedAdhkaarDataEmbedded>> = searchQuery
        .map { it?.trim().orEmpty() }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            val language = if (getLocale().language.equals("ar", ignoreCase = true)) "ar" else "en"
            if (query.isBlank()) {
                repo.getBookmarkedAdhkaarData(language)
            } else {
                val q = "%$query%"
                repo.searchBookmarkedAdhkaarData(language, q)
            }
        }

    fun setSearchQuery(query: String?) {
        searchQuery.value = query
    }
}
