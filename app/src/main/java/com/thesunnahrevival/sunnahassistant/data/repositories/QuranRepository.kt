package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.data.local.AyahDao
import com.thesunnahrevival.sunnahassistant.data.local.FootnoteDao
import com.thesunnahrevival.sunnahassistant.data.local.LineDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.local.ToDoDao
import com.thesunnahrevival.sunnahassistant.data.local.TranslationDao
import com.thesunnahrevival.sunnahassistant.data.model.ResourceLinks
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.data.remote.ResourceApiInterface
import com.thesunnahrevival.sunnahassistant.utilities.retrofit

class QuranRepository private constructor(
    private val applicationContext: Context
) {

    private val ayahDao: AyahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahDao()

    private val lineDao: LineDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).lineDao()

    private val footnoteDao: FootnoteDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).footnoteDao()

    private val translationDao: TranslationDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).translationDao()

    private val toDoDao: ToDoDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).toDoDao()

    private val resourceLinksRestApi: ResourceApiInterface = retrofit.create(ResourceApiInterface::class.java)

    suspend fun getLinesByPageNumber(pageNumber: Int) = lineDao.getLineByPageNumber(pageNumber)

    suspend fun getLinesByAyahId(ayahId: Int) = lineDao.getLineByAyahId(ayahId)


    suspend fun getFullAyahDetailsById(ayahId: Int) = ayahDao.getFullAyahDetailsById(ayahId)

    fun getTranslations() = translationDao.getTranslations()

    suspend fun getFootnote(ayahTranslationId: Int, number: Int) =
        footnoteDao.getFootnote(ayahTranslationId, number)

    suspend fun updateTranslation(translation: Translation) =
        translationDao.updateTranslation(translation)

    suspend fun getFullAyahDetailsByPageNumber(pageNumber: Int) =
        ayahDao.getFullAyahDetailsByPageNumber(pageNumber)



    companion object {
        @Volatile
        private var instance: QuranRepository? = null

        fun getInstance(context: Context): QuranRepository {
            return instance ?: synchronized(this) {
                instance ?: QuranRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
