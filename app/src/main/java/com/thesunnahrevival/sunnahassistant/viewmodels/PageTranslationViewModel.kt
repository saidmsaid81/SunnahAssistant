package com.thesunnahrevival.sunnahassistant.viewmodels

import android.app.Application
import com.thesunnahrevival.sunnahassistant.data.repositories.QuranTranslationRepository

class PageTranslationViewModel(application: Application) : AyahTranslationViewModel(application) {
    private val quranTranslationRepository = QuranTranslationRepository.getInstance(getApplication())

    suspend fun getFullAyahDetailsByPageNumber(pageNumber: Int) = quranTranslationRepository.getFullAyahDetailsByPageNumber(pageNumber)
}