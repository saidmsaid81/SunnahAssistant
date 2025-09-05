package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.thesunnahrevival.sunnahassistant.data.local.*
import com.thesunnahrevival.sunnahassistant.data.model.dto.toGroupedFullAyahDetails
import com.thesunnahrevival.sunnahassistant.data.model.entity.AyahBookmark
import com.thesunnahrevival.sunnahassistant.data.model.entity.AyahTranslation
import com.thesunnahrevival.sunnahassistant.data.model.entity.Footnote
import com.thesunnahrevival.sunnahassistant.data.model.entity.Translation
import com.thesunnahrevival.sunnahassistant.data.remote.ResourceApiInterface
import com.thesunnahrevival.sunnahassistant.data.typeconverters.BooleanAsIntDeserializer
import com.thesunnahrevival.sunnahassistant.utilities.retrofit
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

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

    private val ayahTranslationDao: AyahTranslationDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahTranslationDao()

    private val ayahBookmarkDao: AyahBookmarkDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahBookmarkDao()

    private val resourceApiRestApi = retrofit.create(ResourceApiInterface::class.java)

    private var resourcesLink: String? = null

    fun getTranslations() = translationDao.getTranslations()

    suspend fun toggleAyahBookmarkStatus(ayahId: Int) {
        val ayahBookmark = ayahBookmarkDao.getAyahBookmark(ayahId)
        when {
            ayahBookmark != null -> {
                ayahBookmarkDao.delete(ayahBookmark)
            }
            else -> {
                ayahBookmarkDao.insert(AyahBookmark(ayahId = ayahId))
            }
        }
    }

    suspend fun updateTranslation(translation: Translation): Boolean {
        translationDao.updateTranslation(translation)
        if (translation.selected && !ayahTranslationDao.exists(translation.id)) {
            if (resourcesLink == null) {
                resourcesLink = resourceApiRestApi.getResourceLinks().body()?.translationLink ?: return false
            }

            val responseBody = resourceApiRestApi.downloadFile("$resourcesLink/${translation.key}.zip")
            val zipFile = File(applicationContext.cacheDir, "${translation.key}.zip")
            
            try {
                responseBody.body()?.byteStream().use { inputStream ->
                    FileOutputStream(zipFile).use { outputStream ->
                        inputStream?.copyTo(outputStream)
                    }
                }

                ZipInputStream(zipFile.inputStream()).use { zipInputStream ->
                    var entry = zipInputStream.nextEntry
                    var ayahTranslationsJson: String? = null
                    var footnotesJson: String? = null

                    while (entry != null) {
                        when (entry.name) {
                            "AyahTranslations.json" -> {
                                ayahTranslationsJson = zipInputStream.readBytes().toString(Charsets.UTF_8)
                            }
                            "Footnotes.json" -> {
                                footnotesJson = zipInputStream.readBytes().toString(Charsets.UTF_8)
                            }
                        }
                        entry = zipInputStream.nextEntry
                    }

                    ayahTranslationsJson?.let {
                        processAyahTranslations(it)
                    }
                    footnotesJson?.let {
                        processFootnotes(it)
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                return false
            } finally {
                if (zipFile.exists()) {
                    zipFile.delete()
                }
            }
        }
        return true
    }

    private suspend fun processAyahTranslations(jsonString: String) {
        try {
            val listType = object : TypeToken<List<AyahTranslation>>() {}.type
            val gson = getGson()
            val ayahTranslations: List<AyahTranslation> = gson.fromJson(jsonString, listType)
            ayahTranslations.forEach { ayahTranslation ->
                ayahTranslationDao.insert(ayahTranslation)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private suspend fun processFootnotes(jsonString: String) {
        try {
            val listType = object : TypeToken<List<Footnote>>() {}.type
            val gson = getGson()
            val footnotes: List<Footnote> = gson.fromJson(jsonString, listType)
            footnotes.forEach { footnote ->
                footnoteDao.insert(footnote)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun getGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.java, BooleanAsIntDeserializer())
        .create()

    suspend fun getFullAyahDetailsById(ayahId: Int) = ayahDao.getFullAyahDetailsById(ayahId).toGroupedFullAyahDetails().firstOrNull()

    suspend fun getFootnote(ayahTranslationId: Int, number: Int) =
        footnoteDao.getFootnote(ayahTranslationId, number)

    suspend fun getFullAyahDetailsByPageNumber(pageNumber: Int) =
        ayahDao.getFullAyahDetailsByPageNumber(pageNumber).toGroupedFullAyahDetails()

}