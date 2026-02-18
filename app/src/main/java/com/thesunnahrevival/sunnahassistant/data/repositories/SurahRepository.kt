package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import androidx.paging.PagingSource
import com.thesunnahrevival.sunnahassistant.data.local.PinnedSurahDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.local.SurahDao
import com.thesunnahrevival.sunnahassistant.data.model.entity.PinnedSurah
import kotlinx.coroutines.flow.map

class SurahRepository private constructor(
    private val applicationContext: Context
) {
    private var activePagingSources = mutableSetOf<PagingSource<*, *>>()

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
    
    private val pinnedSurahDao: PinnedSurahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).pinnedSurahDao()

    fun getAllSurahs() = surahDao.getAllSurahsWithPin()

    fun searchSurahs(query: String)= surahDao.searchSurahsWithPin(query)

    fun getFirst3Surahs() = surahDao.getFirst3SurahsWithPin().map { surahs -> surahs.map { it.toSurah() } }

    suspend fun getSurahByPage(page: Int) = surahDao.getSurahByPage(page)

    suspend fun toggleSurahPin(surahId: Int): PinResult {
        val existingPin = pinnedSurahDao.getPinnedSurahById(surahId)
        
        val result = if (existingPin != null) {
            pinnedSurahDao.deleteBysurahId(surahId)
            PinResult.Unpinned
        } else {
            val pinnedCount = pinnedSurahDao.getPinnedCount()
            if (pinnedCount >= 5) {
                PinResult.LimitReached
            } else {
                val nextPinOrder = (pinnedSurahDao.getMaxPinOrder() ?: 0) + 1
                pinnedSurahDao.insert(PinnedSurah(surahId = surahId, pinOrder = nextPinOrder))
                PinResult.Pinned
            }
        }
        return result
    }

    enum class PinResult {
        Pinned, Unpinned, LimitReached
    }
}