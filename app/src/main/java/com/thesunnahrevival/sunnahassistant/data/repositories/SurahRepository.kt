package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.local.SurahDao

class SurahRepository private constructor(
    private val applicationContext: Context
) {

    companion object {
        @Volatile
        private var instance: SurahRepository? = null

        fun getInstance(context: Context): SurahRepository {
            return instance ?: synchronized(this) {
                instance ?: SurahRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val surahDao: SurahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).surahDao()

    fun getAllSurahs() = surahDao.getAllSurahs()
}