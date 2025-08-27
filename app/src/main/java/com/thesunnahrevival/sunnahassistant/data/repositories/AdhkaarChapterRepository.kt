package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.thesunnahrevival.sunnahassistant.data.local.AdhkaarChapterDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase

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

    fun getAllChapters(language: String) = adhkaarChapterDao.getAllChaptersPagingSource(language)

    fun searchChapters(language: String, query: String) = adhkaarChapterDao.getChaptersByQuery(language, query)

    fun getFirstThreeChapters(language: String) = adhkaarChapterDao.getFirstThreeChapters(language)

    suspend fun toggleChapterPin(chapterId: Int): PinResult {
        val chapter = adhkaarChapterDao.getChapterById(chapterId)

        return if (chapter.pinOrder != null) {
            adhkaarChapterDao.updatePinOrder(chapterId, null)
            PinResult.Unpinned
        } else {
            val pinnedCount = adhkaarChapterDao.getPinnedChapterCount()
            if (pinnedCount.size >= 5) {
                PinResult.LimitReached
            } else {
                val nextPinOrder = (adhkaarChapterDao.getMaxPinOrder() ?: 0) + 1
                adhkaarChapterDao.updatePinOrder(chapterId, nextPinOrder)
                PinResult.Pinned
            }
        }
    }

    enum class PinResult {
        Pinned, Unpinned, LimitReached
    }
}