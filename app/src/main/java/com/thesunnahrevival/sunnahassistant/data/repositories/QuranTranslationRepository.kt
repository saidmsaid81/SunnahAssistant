package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.thesunnahrevival.sunnahassistant.data.local.AyahBookmarkDao
import com.thesunnahrevival.sunnahassistant.data.local.AyahDao
import com.thesunnahrevival.sunnahassistant.data.local.AyahTranslationDao
import com.thesunnahrevival.sunnahassistant.data.local.FootnoteDao
import com.thesunnahrevival.sunnahassistant.data.local.SunnahAssistantDatabase
import com.thesunnahrevival.sunnahassistant.data.local.TranslationDao
import com.thesunnahrevival.sunnahassistant.data.model.dto.TranslationMeta
import com.thesunnahrevival.sunnahassistant.data.model.dto.toGroupedFullAyahDetails
import com.thesunnahrevival.sunnahassistant.data.model.entity.AyahBookmark
import com.thesunnahrevival.sunnahassistant.data.model.entity.AyahTranslation
import com.thesunnahrevival.sunnahassistant.data.model.entity.Footnote
import com.thesunnahrevival.sunnahassistant.data.model.entity.Translation
import com.thesunnahrevival.sunnahassistant.data.remote.ResourceApiInterface
import com.thesunnahrevival.sunnahassistant.data.typeconverters.BooleanAsIntDeserializer
import com.thesunnahrevival.sunnahassistant.utilities.retrofit
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

private const val TRANSLATIONS_META_FILE_NAME = "translations_meta.json"
private const val TRANSLATION_VERSION_FLAG_PREFIX = "translation_version_"

class QuranTranslationRepository private constructor(
    private val applicationContext: Context
) {

    companion object {
        @Volatile
        private var instance: QuranTranslationRepository? = null

        fun getInstance(context: Context): QuranTranslationRepository {
            return instance ?: synchronized(this) {
                instance ?: QuranTranslationRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val translationDao: TranslationDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).translationDao()

    private val ayahDao: AyahDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahDao()

    private val footnoteDao: FootnoteDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).footnoteDao()

    private val ayahTranslationDao: AyahTranslationDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahTranslationDao()

    private val ayahBookmarkDao: AyahBookmarkDao
        get() = SunnahAssistantDatabase.getInstance(applicationContext).ayahBookmarkDao()

    private val database: SunnahAssistantDatabase
        get() = SunnahAssistantDatabase.getInstance(applicationContext)

    private val flagRepository = FlagRepository.getInstance(applicationContext)
    private val resourceApiRestApi = retrofit.create(ResourceApiInterface::class.java)

    private var resourcesLink: String? = null

    fun getTranslations() = translationDao.getTranslations()

    suspend fun toggleAyahBookmarkStatus(ayahId: Int) {
        val ayahBookmark = ayahBookmarkDao.getAyahBookmark(ayahId)
        when {
            ayahBookmark != null -> {
                ayahBookmarkDao.delete(ayahBookmark)
            }
            else -> {
                ayahBookmarkDao.insert(AyahBookmark(ayahId = ayahId))
            }
        }
    }

    suspend fun updateTranslation(translation: Translation): Boolean {
        translationDao.updateTranslation(translation)
        if (translation.selected && !ayahTranslationDao.exists(translation.id)) {
            val didUpdate = downloadAndReplaceTranslationData(translation, replaceExisting = false)
            if (didUpdate) {
                updateInstalledTranslationVersion(translation)
            }
            return didUpdate
        }
        return true
    }

    suspend fun getInstalledTranslationUpdates(): Set<Int> {
        return try {
            val installedTranslationIds = ayahTranslationDao.getInstalledTranslationIds().toSet()
            if (installedTranslationIds.isEmpty()) {
                return emptySet()
            }

            val translations = translationDao.getTranslationsList()
                .filter { installedTranslationIds.contains(it.id) }

            val metaByKey = getTranslationsMetadataByKey()
            if (metaByKey.isEmpty()) {
                return emptySet()
            }

            translations.filter { translation ->
                val metadata = metaByKey[translation.key] ?: return@filter false
                val currentVersion =
                    flagRepository.getLongFlag(getTranslationVersionFlagKey(translation.id)) ?: 0L
                metadata.version > currentVersion
            }.map { it.id }.toSet()
        } catch (exception: Exception) {
            exception.printStackTrace()
            emptySet()
        }
    }

    suspend fun updateInstalledTranslations(): TranslationBulkUpdateResult {
        return try {
            val updatesAvailableIds = getInstalledTranslationUpdates()
            if (updatesAvailableIds.isEmpty()) {
                return TranslationBulkUpdateResult()
            }

            val translations = translationDao.getTranslationsList()
                .filter { updatesAvailableIds.contains(it.id) }
            val metaByKey = getTranslationsMetadataByKey()

            val updatedIds = mutableSetOf<Int>()
            val failedIds = mutableSetOf<Int>()

            translations.forEach { translation ->
                val didUpdate = downloadAndReplaceTranslationData(translation, replaceExisting = true)
                if (didUpdate) {
                    val version = metaByKey[translation.key]?.version
                    if (version != null) {
                        flagRepository.setFlag(
                            getTranslationVersionFlagKey(translation.id),
                            version
                        )
                    }
                    updatedIds.add(translation.id)
                } else {
                    failedIds.add(translation.id)
                }
            }

            TranslationBulkUpdateResult(
                updatedTranslationIds = updatedIds,
                failedTranslationIds = failedIds
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            TranslationBulkUpdateResult()
        }
    }

    suspend fun updateInstalledTranslation(translationId: Int): Boolean {
        return try {
            val translation = translationDao.getTranslationsList().firstOrNull { it.id == translationId } ?: return false
            val installedTranslationIds = ayahTranslationDao.getInstalledTranslationIds().toSet()
            if (!installedTranslationIds.contains(translationId)) {
                return false
            }

            val metadata = getTranslationsMetadataByKey()[translation.key] ?: return false
            val currentVersion =
                flagRepository.getLongFlag(getTranslationVersionFlagKey(translation.id)) ?: 0L
            if (metadata.version <= currentVersion) {
                return true
            }

            val didUpdate = downloadAndReplaceTranslationData(translation, replaceExisting = true)
            if (didUpdate) {
                flagRepository.setFlag(getTranslationVersionFlagKey(translation.id), metadata.version)
            }
            didUpdate
        } catch (exception: Exception) {
            exception.printStackTrace()
            false
        }
    }

    private suspend fun updateInstalledTranslationVersion(translation: Translation) {
        try {
            val metadata = getTranslationsMetadataByKey()[translation.key] ?: return
            flagRepository.setFlag(getTranslationVersionFlagKey(translation.id), metadata.version)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private suspend fun downloadAndReplaceTranslationData(
        translation: Translation,
        replaceExisting: Boolean
    ): Boolean {
        if (resourcesLink == null) {
            resourcesLink = resourceApiRestApi.getResourceLinks().body()?.translationLink ?: return false
        }

        val translationZipUrl = withCacheBuster("$resourcesLink/${translation.key}.zip")
        val responseBody = resourceApiRestApi.downloadFile(translationZipUrl)
        if (!responseBody.isSuccessful) {
            return false
        }

        val zipFile = File(applicationContext.cacheDir, "${translation.key}.zip")

        try {
            responseBody.body()?.byteStream().use { inputStream ->
                FileOutputStream(zipFile).use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
            }

            var ayahTranslationsJson: String? = null
            var footnotesJson: String? = null

            ZipInputStream(zipFile.inputStream()).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "AyahTranslations.json" -> {
                            ayahTranslationsJson = zipInputStream.readBytes().toString(Charsets.UTF_8)
                        }
                        "Footnotes.json" -> {
                            footnotesJson = zipInputStream.readBytes().toString(Charsets.UTF_8)
                        }
                    }
                    entry = zipInputStream.nextEntry
                }
            }

            val ayahTranslations = parseAyahTranslations(ayahTranslationsJson ?: return false)
            val footnotes = parseFootnotes(footnotesJson)
            if (ayahTranslations.isEmpty()) {
                return false
            }

            database.withTransaction {
                if (replaceExisting) {
                    footnoteDao.deleteByTranslationId(translation.id)
                    ayahTranslationDao.deleteByTranslationId(translation.id)
                }

                ayahTranslations.forEach { ayahTranslation ->
                    ayahTranslationDao.insert(ayahTranslation)
                }

                footnotes.forEach { footnote ->
                    footnoteDao.insert(footnote)
                }
            }
            return true
        } catch (exception: Exception) {
            exception.printStackTrace()
            return false
        } finally {
            if (zipFile.exists()) {
                zipFile.delete()
            }
        }
    }

    private fun parseAyahTranslations(jsonString: String): List<AyahTranslation> {
        try {
            val listType = object : TypeToken<List<AyahTranslation>>() {}.type
            val gson = getGson()
            return gson.fromJson<List<AyahTranslation>>(jsonString, listType) ?: emptyList()
        } catch (exception: Exception) {
            exception.printStackTrace()
            return emptyList()
        }
    }

    private fun parseFootnotes(jsonString: String?): List<Footnote> {
        if (jsonString == null) {
            return emptyList()
        }

        try {
            val listType = object : TypeToken<List<Footnote>>() {}.type
            val gson = getGson()
            return gson.fromJson<List<Footnote>>(jsonString, listType) ?: emptyList()
        } catch (exception: Exception) {
            exception.printStackTrace()
            return emptyList()
        }
    }

    private suspend fun getTranslationsMetadataByKey(): Map<String, TranslationMeta> {
        if (resourcesLink == null) {
            resourcesLink = resourceApiRestApi.getResourceLinks().body()?.translationLink ?: return emptyMap()
        }

        val metadataUrl = withCacheBuster("$resourcesLink/$TRANSLATIONS_META_FILE_NAME")
        val response = resourceApiRestApi.downloadFile(metadataUrl)
        if (!response.isSuccessful) {
            return emptyMap()
        }

        val metadataJson = response.body()?.string() ?: return emptyMap()
        val listType = object : TypeToken<List<TranslationMeta>>() {}.type
        val metadataList: List<TranslationMeta> = Gson().fromJson<List<TranslationMeta>>(metadataJson, listType) ?: emptyList()
        return metadataList.associateBy { it.key }
    }

    private fun getTranslationVersionFlagKey(translationId: Int): String {
        return "$TRANSLATION_VERSION_FLAG_PREFIX$translationId"
    }

    private fun withCacheBuster(url: String): String {
        val separator = if (url.contains("?")) {
            "&"
        } else {
            "?"
        }
        return "$url${separator}t=${System.currentTimeMillis()}"
    }

    private fun getGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.java, BooleanAsIntDeserializer())
        .create()

    suspend fun getFullAyahDetailsById(ayahId: Int) = ayahDao.getFullAyahDetailsById(ayahId).toGroupedFullAyahDetails().firstOrNull()

    suspend fun getFootnote(ayahTranslationId: Int, number: Int) =
        footnoteDao.getFootnote(ayahTranslationId, number)

    suspend fun getFullAyahDetailsByPageNumber(pageNumber: Int) =
        ayahDao.getFullAyahDetailsByPageNumber(pageNumber).toGroupedFullAyahDetails()

    data class TranslationBulkUpdateResult(
        val updatedTranslationIds: Set<Int> = emptySet(),
        val failedTranslationIds: Set<Int> = emptySet()
    )
}
