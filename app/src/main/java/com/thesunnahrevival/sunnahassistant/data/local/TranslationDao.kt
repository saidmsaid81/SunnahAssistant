package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thesunnahrevival.sunnahassistant.data.model.Translation
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(translation: Translation)

    @Query("SELECT * FROM translations ")
    fun getTranslations(): Flow<List<Translation>>

    @Update
    suspend fun updateTranslation(translation: Translation)
}