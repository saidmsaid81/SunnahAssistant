package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.data.local.AyahDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase

class BookmarksRepository private constructor(
    private val applicationContext: Context
) {

    companion object {
        @Volatile
        private var instance: BookmarksRepository? = null

        fun getInstance(context: Context): BookmarksRepository {
            return instance ?: synchronized(this) {
                instance ?: BookmarksRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val ayahDao: AyahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahDao()

    fun getBookmarkedAyahs() = ayahDao.getBookmarkedAyahs()
}