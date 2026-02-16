package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thesunnahrevival.sunnahassistant.BuildConfig
import com.thesunnahrevival.sunnahassistant.data.local.AdhkaarChapterDao
import com.thesunnahrevival.sunnahassistant.data.local.AdhkaarItemBookmarkDao
import com.thesunnahrevival.sunnahassistant.data.local.AdhkaarItemDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.model.dto.AdhkaarMeta
import com.thesunnahrevival.sunnahassistant.data.model.embedded.BookmarkedAdhkaarDataEmbedded
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarItem
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarItemBookmark
import com.thesunnahrevival.sunnahassistant.data.remote.ResourceApiInterface
import com.thesunnahrevival.sunnahassistant.utilities.retrofit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val ADHKAAR_LAST_SYNC_MS_FLAG = "adhkaar_last_sync_ms"
private const val ADHKAAR_VERSION_FLAG = "adhkaar_version"
private const val ADHKAAR_SYNC_INTERVAL_MS = 24 * 60 * 60 * 1000L
private const val ADHKAAR_META_FILE_NAME = "adhkaar_meta.json"

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

    private val adhkaarItemBookmarkDao: AdhkaarItemBookmarkDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).adhkaarItemBookmarkDao()

    private val resourceApiRestApi = retrofit.create(ResourceApiInterface::class.java)
    private val flagRepository = FlagRepository.getInstance(applicationContext)

    private var resourcesLink: String? = null

    suspend fun loadAdhkaarItems() {
        try {
            val adhkaarExists = adhkaarItemDao.doesAdhkaarItemsExist()
            if (!adhkaarExists) {
                fetchAndReplaceAdhkaarItems(getAdhkaarLink())
                return
            }

            if (!shouldCheckMetadataNow()) {
                return
            }

            val adhkaarLink = getAdhkaarLink()
            val metadata = fetchAdhkaarMetadata(adhkaarLink)
            if (metadata != null) {
                val currentVersion = flagRepository.getLongFlag(ADHKAAR_VERSION_FLAG) ?: 0L
                if (metadata.version > currentVersion) {
                    val didUpdate = fetchAndReplaceAdhkaarItems(adhkaarLink)
                    if (didUpdate) {
                        flagRepository.setFlag(ADHKAAR_VERSION_FLAG, metadata.version)
                    }
                }
            }
            flagRepository.setFlag(ADHKAAR_LAST_SYNC_MS_FLAG, System.currentTimeMillis())
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun getAdhkaarItemsByChapterId(chapterId: Int): Flow<List<AdhkaarItem>> {
        return adhkaarItemDao.getAdhkaarItemsByChapterId(chapterId).map { itemsWithBookmark ->
            itemsWithBookmark.map { itemWithBookmark ->
                AdhkaarItem(
                    id = itemWithBookmark.item.id,
                    itemId = itemWithBookmark.item.itemId,
                    language = itemWithBookmark.item.language,
                    itemTranslation = itemWithBookmark.item.itemTranslation,
                    chapterId = itemWithBookmark.item.chapterId,
                    reference = itemWithBookmark.item.reference,
                    itemOrder = itemWithBookmark.item.itemOrder,
                    bookmarked = itemWithBookmark.bookmarked
                )
            }
        }
    }

    suspend fun getChapterNameByChapterId(id: Int, language: String) = adhkaarChapterDao.getChapterNameByChapterId(id, language)

    suspend fun toggleBookmark(itemId: Int) {
        val adhkaarItemBookmark = adhkaarItemBookmarkDao.getAdhkaarItemBookmark(itemId)
        if (adhkaarItemBookmark != null) {
            adhkaarItemBookmarkDao.delete(adhkaarItemBookmark)
        } else {
            adhkaarItemBookmarkDao.insert(AdhkaarItemBookmark(adhkaarItemId = itemId))
        }
    }

    fun getBookmarkedAdhkaarData(language: String): Flow<List<BookmarkedAdhkaarDataEmbedded>> {
        return adhkaarItemBookmarkDao.getBookmarkedAdhkaarData(language)
    }

    fun searchBookmarkedAdhkaarData(language: String, query: String): Flow<List<BookmarkedAdhkaarDataEmbedded>> {
        return adhkaarItemBookmarkDao.searchBookmarkedAdhkaarData(language, query)
    }

    private suspend fun shouldCheckMetadataNow(): Boolean {
        if (BuildConfig.DEBUG) {
            return true
        }

        val lastSyncMillis = flagRepository.getLongFlag(ADHKAAR_LAST_SYNC_MS_FLAG) ?: 0L
        if (lastSyncMillis <= 0L) {
            return true
        }

        val elapsedMillis = System.currentTimeMillis() - lastSyncMillis
        return elapsedMillis >= ADHKAAR_SYNC_INTERVAL_MS
    }

    private suspend fun getAdhkaarLink(): String {
        if (resourcesLink == null) {
            val linksResponse = resourceApiRestApi.getResourceLinks().body()
            resourcesLink = linksResponse?.adhkaarLink
        }

        return resourcesLink ?: throw IllegalStateException("Adhkaar link is not available")
    }

    private suspend fun fetchAdhkaarMetadata(adhkaarLink: String): AdhkaarMeta? {
        val metadataUrl = getAdhkaarMetaUrl(adhkaarLink)
        val metadataResponse = resourceApiRestApi.downloadFile(metadataUrl)
        val metadataJson = metadataResponse.body()?.string() ?: return null
        return Gson().fromJson(metadataJson, AdhkaarMeta::class.java)
    }

    private suspend fun fetchAndReplaceAdhkaarItems(adhkaarLink: String): Boolean {
        val responseBody = resourceApiRestApi.downloadFile(adhkaarLink)
        val jsonString = responseBody.body()?.string() ?: return false
        val listType = object : TypeToken<List<AdhkaarItem>>() {}.type
        val adhkaarItems: List<AdhkaarItem> = Gson().fromJson(jsonString, listType)
        if (adhkaarItems.isEmpty()) {
            return false
        }
        adhkaarItemDao.replaceAllAdhkaarItems(adhkaarItems)
        return true
    }

    private fun getAdhkaarMetaUrl(adhkaarLink: String): String {
        val baseUrl = adhkaarLink.substringBeforeLast("/")
        if (baseUrl == adhkaarLink) {
            throw IllegalStateException("Adhkaar link has an invalid format")
        }
        return "$baseUrl/$ADHKAAR_META_FILE_NAME"
    }
}
