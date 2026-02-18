package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.entity.AyahTranslation

@Dao
interface AyahTranslationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ayahTranslation: AyahTranslation)

    @Query("SELECT EXISTS(SELECT 1 FROM ayah_translations WHERE translation_id = :translationId)")
    suspend fun exists(translationId: Int): Boolean

    @Query("SELECT DISTINCT translation_id FROM ayah_translations")
    suspend fun getInstalledTranslationIds(): List<Int>

    @Query("DELETE FROM ayah_translations WHERE translation_id = :translationId")
    suspend fun deleteByTranslationId(translationId: Int)
}
