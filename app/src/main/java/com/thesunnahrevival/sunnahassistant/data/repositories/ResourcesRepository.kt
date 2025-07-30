package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.local.AyahDao
import com.thesunnahrevival.sunnahassistant.data.local.LanguageDao
import com.thesunnahrevival.sunnahassistant.data.local.LineDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.local.SurahDao
import com.thesunnahrevival.sunnahassistant.data.local.TranslationDao
import com.thesunnahrevival.sunnahassistant.data.model.Ayah
import com.thesunnahrevival.sunnahassistant.data.model.Language
import com.thesunnahrevival.sunnahassistant.data.model.Line
import com.thesunnahrevival.sunnahassistant.data.model.ResourceItem
import com.thesunnahrevival.sunnahassistant.data.model.Surah
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import com.thesunnahrevival.sunnahassistant.data.typeconverters.BooleanAsIntDeserializer

class ResourcesRepository private constructor(
    private val applicationContext: Context
) {

    companion object {
        @Volatile
        private var instance: ResourcesRepository? = null

        fun getInstance(context: Context): ResourcesRepository {
            return instance ?: synchronized(this) {
                instance ?: ResourcesRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val surahDao: SurahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).surahDao()

    private val translationDao: TranslationDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).translationDao()

    private val ayahDao: AyahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahDao()

    private val lineDao: LineDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).lineDao()

    private val languageDao: LanguageDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).languageDao()

    fun resourceItems(): List<ResourceItem> {
        return listOf(
            ResourceItem(
                1,
                R.string.daily_hadith,
                R.string.from_the_sunnah_revival_blog,
                R.id.dailyHadithFragment
            )
        )
    }
    
    suspend fun prepopulateQuranData() {
        if (surahDao.countSurah() == 0) {
            prepopulateSurahData()
            prepopulateAyahData()
            prepopulateLineData()
            prepopulateLanguageData()
            prepopulateTranslationData()
        }
    }


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


        } catch (exception: Exception) {
            exception.printStackTrace()
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

        } catch (exception: Exception) {
            exception.printStackTrace()
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

        } catch (exception: Exception) {
            exception.printStackTrace()
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

        } catch (exception: Exception) {
            exception.printStackTrace()
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

        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun getGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.java, BooleanAsIntDeserializer())
        .create()
}