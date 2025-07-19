package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.data.local.AyahDao
import com.thesunnahrevival.sunnahassistant.data.local.FootnoteDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.local.TranslationDao
import com.thesunnahrevival.sunnahassistant.data.model.Translation

class QuranTranslationRepository private constructor(
    private val applicationContext: Context
) {

    companion object {
        @Volatile
        private var instance: QuranTranslationRepository? = null

        fun getInstance(context: Context): QuranTranslationRepository {
            return instance ?: synchronized(this) {
                instance ?: QuranTranslationRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val translationDao: TranslationDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).translationDao()

    private val ayahDao: AyahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahDao()

    private val footnoteDao: FootnoteDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).footnoteDao()

    fun getTranslations() = translationDao.getTranslations()

    suspend fun updateTranslation(translation: Translation) =
        translationDao.updateTranslation(translation)

    suspend fun getFullAyahDetailsById(ayahId: Int) = ayahDao.getFullAyahDetailsById(ayahId)

    suspend fun getFootnote(ayahTranslationId: Int, number: Int) =
        footnoteDao.getFootnote(ayahTranslationId, number)

    suspend fun getFullAyahDetailsByPageNumber(pageNumber: Int) =
        ayahDao.getFullAyahDetailsByPageNumber(pageNumber)

}