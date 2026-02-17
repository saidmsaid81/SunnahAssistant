package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.entity.Language

@Dao
interface LanguageDao {
    @Query("SELECT count(*) FROM languages")
    suspend fun countLanguages(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(language: Language)
}
