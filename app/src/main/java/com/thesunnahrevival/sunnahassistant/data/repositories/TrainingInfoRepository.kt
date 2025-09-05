package com.thesunnahrevival.sunnahassistant.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thesunnahrevival.sunnahassistant.data.model.dto.TrainingSection
import com.thesunnahrevival.sunnahassistant.data.model.dto.TrainingStep
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TrainingInfoRepository private constructor(private val applicationContext: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "training_info")

    suspend fun getCurrentTrainingIndex(section: TrainingSection): Int {
        val key = intPreferencesKey(section.key)
        return applicationContext.dataStore.data
            .map { preferences -> preferences[key] ?: 0 }
            .first()
    }

    suspend fun completeCurrentTrainingStep(section: TrainingSection) {
        val key = intPreferencesKey(section.key)
        applicationContext.dataStore.edit { preferences ->
            val currentIndex = preferences[key] ?: 0
            preferences[key] = currentIndex + 1
        }
    }

    suspend fun getNextTrainingStep(section: TrainingSection): TrainingStep? {
        val currentIndex = getCurrentTrainingIndex(section)
        return TrainingStep.entries
            .filter { it.section == section }
            .find { it.index == currentIndex }
    }

    suspend fun resetTrainingForSection(section: TrainingSection) {
        val key = intPreferencesKey(section.key)
        applicationContext.dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    suspend fun resetAllTraining() {
        applicationContext.dataStore.edit { preferences ->
            TrainingSection.entries.forEach { section ->
                val key = intPreferencesKey(section.key)
                preferences.remove(key)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TrainingInfoRepository? = null

        @JvmStatic
        fun getInstance(context: Context): TrainingInfoRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildRepository(context).also { INSTANCE = it }
            }

        private fun buildRepository(context: Context) =
            TrainingInfoRepository(context.applicationContext)
    }
}