package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.thesunnahrevival.sunnahassistant.R
import com.thesunnahrevival.sunnahassistant.data.local.AdhkaarChapterDao
import com.thesunnahrevival.sunnahassistant.data.local.AyahDao
import com.thesunnahrevival.sunnahassistant.data.local.LanguageDao
import com.thesunnahrevival.sunnahassistant.data.local.LineDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.local.SurahDao
import com.thesunnahrevival.sunnahassistant.data.local.TranslationDao
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarChapter
import com.thesunnahrevival.sunnahassistant.data.model.entity.Ayah
import com.thesunnahrevival.sunnahassistant.data.model.entity.Language
import com.thesunnahrevival.sunnahassistant.data.model.entity.Line
import com.thesunnahrevival.sunnahassistant.data.model.entity.ResourceItem
import com.thesunnahrevival.sunnahassistant.data.model.entity.Surah
import com.thesunnahrevival.sunnahassistant.data.model.entity.Translation
import com.thesunnahrevival.sunnahassistant.data.typeconverters.BooleanAsIntDeserializer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ResourcesRepository private constructor(
    private val applicationContext: Context
) {

    companion object {
        const val QURAN_DATA_PREPOPULATED_FLAG = "quran_data_prepopulated"

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

    private val adhkaarChapterDao: AdhkaarChapterDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).adhkaarChapterDao()

    private val flagRepository = FlagRepository.getInstance(applicationContext)

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

    fun isQuranDataPrepopulatedFlow(): Flow<Boolean> {
        return flagRepository.getIntFlagFlow(QURAN_DATA_PREPOPULATED_FLAG)
            .map { value -> value == 1 }
            .distinctUntilChanged()
    }

    suspend fun prepopulateResourcesData() = coroutineScope {
        updateQuranDataPrepopulatedFlag()

        val surahJob = launch {
            prepopulateFromAssets<Surah>("Surahs.json", { surahDao.countSurah() }) { surahDao.insert(it) }
            prepopulateFromAssets<Ayah>("Ayahs.json", { ayahDao.countAyahs() }) { ayahDao.insert(it) }
            prepopulateFromAssets<Line>("Lines.json", { lineDao.countLines() }) { lineDao.insert(it) }
            prepopulateFromAssets<Language>("Languages.json", { languageDao.countLanguages() }) { languageDao.insert(it) }
            prepopulateFromAssets<Translation>("Translations.json", { translationDao.countTranslations() }) { translationDao.insert(it) }
        }

        val adhkaarJob = launch {
            prepopulateFromAssets<AdhkaarChapter>("adhkaar_chapters.json", { adhkaarChapterDao.countAdhkaarChapters() }) { adhkaarChapterDao.insert(it) }
        }

        surahJob.join()
        adhkaarJob.join()
        updateQuranDataPrepopulatedFlag()
    }

    private suspend fun updateQuranDataPrepopulatedFlag() {
        val daosCounts = listOf(
            surahDao.countSurah(),
            ayahDao.countAyahs(),
            lineDao.countLines(),
            languageDao.countLanguages(),
            translationDao.countTranslations()
        )
        val isQuranDataReady = daosCounts.all { it > 0 }

        flagRepository.setFlag(
            QURAN_DATA_PREPOPULATED_FLAG,
            if (isQuranDataReady) 1 else 0
        )
    }

    private suspend inline fun <reified T> prepopulateFromAssets(
        fileName: String,
        crossinline countAction: suspend () -> Int,
        crossinline insertAction: suspend (T) -> Unit
    ) {
        if (countAction() > 0) return
        try {
            val jsonString = applicationContext.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }

            val listType = TypeToken.getParameterized(List::class.java, T::class.java).type
            val gson = getGson()
            val items: List<T> = gson.fromJson(jsonString, listType)
            items.forEach { insertAction(it) }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun getGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.java, BooleanAsIntDeserializer())
        .create()
}
