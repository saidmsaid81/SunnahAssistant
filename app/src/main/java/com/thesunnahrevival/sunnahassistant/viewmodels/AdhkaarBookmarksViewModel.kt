package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.thesunnahrevival.sunnahassistant.data.model.BookmarkedAdhkaarData
import com.thesunnahrevival.sunnahassistant.data.repositories.AdhkaarItemRepository
import com.thesunnahrevival.sunnahassistant.utilities.getLocale
import kotlinx.coroutines.flow.Flow

class AdhkaarBookmarksViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AdhkaarItemRepository.getInstance(application)

    fun getBookmarkedItems(): Flow<List<BookmarkedAdhkaarData>> {
        val language = if (getLocale().language.equals("ar", ignoreCase = true)) "ar" else "en"
        return repo.getBookmarkedAdhkaarData(language)
    }
}
