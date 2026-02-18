package com.thesunnahrevival.sunnahassistant.data.model.dto

import com.thesunnahrevival.sunnahassistant.data.model.entity.PageBookmark
import com.thesunnahrevival.sunnahassistant.data.model.entity.Surah

data class PageBookmarkWithSurah(
    val pageBookmark: PageBookmark,
    val surah: Surah
)