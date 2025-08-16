package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thesunnahrevival.sunnahassistant.data.local.AdhkaarChapterDao
import com.thesunnahrevival.sunnahassistant.data.local.AdhkaarItemDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.model.AdhkaarItem
import com.thesunnahrevival.sunnahassistant.data.remote.ResourceApiInterface
import com.thesunnahrevival.sunnahassistant.utilities.retrofit
import kotlinx.coroutines.flow.Flow

class AdhkaarItemRepository private constructor(
    private val applicationContext: Context
) {

    companion object {
        @Volatile
        private var instance: AdhkaarItemRepository? = null

        fun getInstance(context: Context): AdhkaarItemRepository {
            return instance ?: synchronized(this) {
                instance ?: AdhkaarItemRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val adhkaarItemDao: AdhkaarItemDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).adhkaarItemDao()

    private val adhkaarChapterDao: AdhkaarChapterDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).adhkaarChapterDao()

    private val resourceApiRestApi = retrofit.create(ResourceApiInterface::class.java)

    private var resourcesLink: String? = null

    suspend fun loadAdhkaarItems() {
        if (!adhkaarItemDao.doesAdhkaarItemsExist()) {
            try {
                if (resourcesLink == null) {
                    resourcesLink = resourceApiRestApi.getResourceLinks().body()?.adhkaarLink ?: return
                }

                val responseBody = resourceApiRestApi.downloadFile(resourcesLink.toString())
                val jsonString = responseBody.body()?.string()

                jsonString?.let {
                    val listType = object : TypeToken<List<AdhkaarItem>>() {}.type
                    val gson = Gson()
                    val adhkaarItems: List<AdhkaarItem> = gson.fromJson(it, listType)
                    
                    adhkaarItemDao.insertAll(adhkaarItems)
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    fun getAdhkaarItemsByChapterId(chapterId: Int): Flow<List<AdhkaarItem>> {
        return adhkaarItemDao.getAdhkaarItemsByChapterId(chapterId)
    }

    suspend fun getChapterNameByChapterId(id: Int, language: String) = adhkaarChapterDao.getChapterNameByChapterId(id, language)

    suspend fun updateBookmarkStatus(itemId: Int, bookmarked: Boolean) {
        adhkaarItemDao.updateBookmarkStatus(itemId, bookmarked)
    }

    fun getBookmarkedAdhkaarData(language: String): Flow<List<com.thesunnahrevival.sunnahassistant.data.model.BookmarkedAdhkaarData>> {
        return adhkaarItemDao.getBookmarkedAdhkaarData(language)
    }

}