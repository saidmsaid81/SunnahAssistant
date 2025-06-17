package com.thesunnahrevival.sunnahassistant.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.thesunnahrevival.sunnahassistant.data.local.AyahDao
import com.thesunnahrevival.sunnahassistant.data.local.AyahTranslationDao
import com.thesunnahrevival.sunnahassistant.data.local.FootnoteDao
import com.thesunnahrevival.sunnahassistant.data.local.LanguageDao
import com.thesunnahrevival.sunnahassistant.data.local.LineDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.local.SurahDao
import com.thesunnahrevival.sunnahassistant.data.local.TranslationDao
import com.thesunnahrevival.sunnahassistant.data.model.Ayah
import com.thesunnahrevival.sunnahassistant.data.model.AyahTranslation
import com.thesunnahrevival.sunnahassistant.data.model.Footnote
import com.thesunnahrevival.sunnahassistant.data.model.Language
import com.thesunnahrevival.sunnahassistant.data.model.Line
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.data.typeconverters.BooleanAsIntDeserializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class QuranRepository private constructor(
    private val applicationContext: Context
) {

    private val surahDao: SurahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).surahDao()

    private val ayahDao: AyahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahDao()

    private val lineDao: LineDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).lineDao()

    private val ayahTranslationDao: AyahTranslationDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahTranslationDao()

    private val footnoteDao: FootnoteDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).footnoteDao()

    private val languageDao: LanguageDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).languageDao()

    private val translationDao: TranslationDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).translationDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            if (surahDao.countSurah() == 0) {
                prepopulateSurahData()
                prepopulateAyahData()
                prepopulateLineData()
                prepopulateLanguageData()
                prepopulateTranslationData()
                prepopulateAyahTranslationData()
                prepopulateFootnoteData()
            }
        }
    }

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

    private suspend fun prepopulateSurahData() {
        try {
            val jsonString = applicationContext.assets.open("Surahs.json")
                .bufferedReader()
                .use { it.readText() }

            val listSurahType = object : TypeToken<List<Surah>>() {}.type
            val gson = getGson()
            val surahs: List<Surah> = gson.fromJson(jsonString, listSurahType)
            surahs.forEach {
                surahDao.insert(it)
            }


        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }
    }

    private suspend fun prepopulateAyahData() {
        try {
            val jsonString = applicationContext.assets.open("Ayahs.json")
                .bufferedReader()
                .use { it.readText() }

            val listAyahType = object : TypeToken<List<Ayah>>() {}.type
            val gson = getGson()
            val ayahs: List<Ayah> = gson.fromJson(jsonString, listAyahType)
            ayahs.forEach {
                ayahDao.insert(it)
            }

        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }
    }

    private suspend fun prepopulateLineData() {
        try {
            val jsonString = applicationContext.assets.open("Lines.json")
                .bufferedReader()
                .use { it.readText() }

            val listType = object : TypeToken<List<Line>>() {}.type
            val gson = getGson()
            val lines: List<Line> = gson.fromJson(jsonString, listType)
            lines.forEach {
                lineDao.insert(it)
            }

        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }
    }

    private suspend fun prepopulateAyahTranslationData() {
        try {
            val jsonString = applicationContext.assets.open("AyahTranslations.json")
                .bufferedReader()
                .use { it.readText() }

            val listType = object : TypeToken<List<AyahTranslation>>() {}.type
            val gson = getGson()
            val ayahTranslations: List<AyahTranslation> = gson.fromJson(jsonString, listType)
            ayahTranslations.forEach {
                ayahTranslationDao.insert(it)
            }

        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }
    }

    private suspend fun prepopulateFootnoteData() {
        try {
            val jsonString = applicationContext.assets.open("Footnotes.json")
                .bufferedReader()
                .use { it.readText() }

            val listType = object : TypeToken<List<Footnote>>() {}.type
            val gson = getGson()
            val footnotes: List<Footnote> = gson.fromJson(jsonString, listType)
            footnotes.forEach {
                footnoteDao.insert(it)
            }

        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }
    }

    private suspend fun prepopulateLanguageData() {
        try {
            val jsonString = applicationContext.assets.open("Languages.json")
                .bufferedReader()
                .use { it.readText() }

            val listType = object : TypeToken<List<Language>>() {}.type
            val gson = getGson()
            val languages: List<Language> = gson.fromJson(jsonString, listType)
            languages.forEach {
                languageDao.insert(it)
            }

        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }
    }

    private suspend fun prepopulateTranslationData() {
        try {
            val jsonString = applicationContext.assets.open("Translations.json")
                .bufferedReader()
                .use { it.readText() }

            val listType = object : TypeToken<List<Translation>>() {}.type
            val gson = getGson()
            val translations: List<Translation> = gson.fromJson(jsonString, listType)
            translations.forEach {
                translationDao.insert(it)
            }

        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }
    }

    private fun getGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.java, BooleanAsIntDeserializer())
        .create()

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
