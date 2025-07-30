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

    fun getFirst5Surahs() = surahDao.getFirst5Surahs()

    suspend fun getSurahByPage(page: Int) = surahDao.getSurahByPage(page)

    suspend fun toggleSurahPin(surahId: Int): PinResult {
        val surah = surahDao.getSurahById(surahId)
        
        return if (surah.pinOrder != null) {
            surahDao.updatePinOrder(surahId, null)
            PinResult.Unpinned
        } else {
            val pinnedCount = surahDao.getPinnedSurahCount()
            if (pinnedCount >= 5) {
                PinResult.LimitReached
            } else {
                val nextPinOrder = (surahDao.getMaxPinOrder() ?: 0) + 1
                surahDao.updatePinOrder(surahId, nextPinOrder)
                PinResult.Pinned
            }
        }
    }

    enum class PinResult {
        Pinned, Unpinned, LimitReached
    }
}