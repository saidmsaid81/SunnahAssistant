package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
}