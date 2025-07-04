package com.thesunnahrevival.sunnahassistant.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flags")

class FlagRepository private constructor(private val applicationContext: Context) {
    suspend fun setFlag(key: String, value: Long) {
        val flagKey = longPreferencesKey(key)
        applicationContext.dataStore.edit { flags ->
            flags[flagKey] = value
        }
    }

    suspend fun getLongFlag(key: String): Long? {
        val flagKey = longPreferencesKey(key)
        return applicationContext.dataStore.data
            .map { flag -> flag[flagKey] }
            .first()
    }

    companion object {
        @Volatile
        private var INSTANCE: FlagRepository? = null

        @JvmStatic
        fun getInstance(context: Context): FlagRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildRepository(context).also { INSTANCE = it }
            }

        private fun buildRepository(context: Context) =
            FlagRepository(context.applicationContext)
    }
}