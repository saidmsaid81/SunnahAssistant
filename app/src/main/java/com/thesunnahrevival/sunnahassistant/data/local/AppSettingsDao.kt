package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface AppSettingsDao {
    @Query("UPDATE app_settings SET hideDownloadFilePrompt = :value WHERE id = 1")
    suspend fun updateHideDownloadFilePrompt(value: Boolean)

    @Query("SELECT hideDownloadFilePrompt FROM app_settings WHERE id = 1 ")
    suspend fun isHideDownloadFilePrompt(): Boolean
}