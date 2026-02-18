package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.data.local.AdhkaarChapterDao
import com.thesunnahrevival.sunnahassistant.data.local.PinnedAdhkaarChapterDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.model.entity.PinnedAdhkaarChapter
import kotlinx.coroutines.flow.map

class AdhkaarChapterRepository private constructor(
    private val applicationContext: Context
) {

    companion object {
        @Volatile
        private var instance: AdhkaarChapterRepository? = null

        fun getInstance(context: Context): AdhkaarChapterRepository {
            return instance ?: synchronized(this) {
                instance ?: AdhkaarChapterRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val adhkaarChapterDao: AdhkaarChapterDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).adhkaarChapterDao()

    private val pinnedAdhkaarChapterDao: PinnedAdhkaarChapterDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).pinnedAdhkaarChapterDao()

    fun getAllChapters(language: String) = adhkaarChapterDao.getAllChaptersPagingSource(language)

    fun searchChapters(language: String, query: String) = adhkaarChapterDao.getChaptersByQuery(language, query)

    fun getFirstThreeChapters(language: String) = adhkaarChapterDao.getFirstThreeChapters(language).map { adhkaarChapterWithPins ->
        adhkaarChapterWithPins.map { it.toAdhkaarChapter() }
    }

    suspend fun toggleChapterPin(chapterId: Int): PinResult {
        return if (pinnedAdhkaarChapterDao.isChapterPinned(chapterId)) {
            pinnedAdhkaarChapterDao.deletePinnedChapterById(chapterId)
            PinResult.Unpinned
        } else {
            val pinnedCount = pinnedAdhkaarChapterDao.getPinnedChaptersCount()
            if (pinnedCount>= 5) {
                PinResult.LimitReached
            } else {
                val nextPinOrder = pinnedAdhkaarChapterDao.getMaxPinOrder() + 1
                pinnedAdhkaarChapterDao.insertPinnedChapter(PinnedAdhkaarChapter(chapterId = chapterId, pinOrder = nextPinOrder))
                PinResult.Pinned
            }
        }
    }

    enum class PinResult {
        Pinned, Unpinned, LimitReached
    }
}