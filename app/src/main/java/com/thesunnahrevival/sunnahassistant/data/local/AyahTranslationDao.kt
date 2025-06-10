package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.thesunnahrevival.sunnahassistant.data.model.AyahTranslation

@Dao
interface AyahTranslationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ayahTranslation: AyahTranslation)
}