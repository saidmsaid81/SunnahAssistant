package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FlagRepository private constructor(private val applicationContext: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flags")

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

    suspend fun clearLongFlag(key: String) {
        val flagKey = longPreferencesKey(key)
        applicationContext.dataStore.edit { flags ->
            flags.remove(flagKey)
        }
    }

    suspend fun setFlag(key: String, value: Int) {
        val flagKey = intPreferencesKey(key)
        applicationContext.dataStore.edit { flags ->
            flags[flagKey] = value
        }
    }

    suspend fun getIntFlag(key: String): Int? {
        val flagKey = intPreferencesKey(key)
        return applicationContext.dataStore.data
            .map { flag -> flag[flagKey] }
            .first()
    }

    suspend fun clearIntFlag(key: String) {
        val flagKey = intPreferencesKey(key)
        applicationContext.dataStore.edit { flags ->
            flags.remove(flagKey)
        }
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