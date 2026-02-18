package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.entity.Footnote

@Dao
interface FootnoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(footnote: Footnote)

    @Query("SELECT * FROM footnotes WHERE ayah_translation_id = :ayahTranslationId AND number = :number")
    suspend fun getFootnote(ayahTranslationId: Int, number: Int): Footnote?

    @Query(
        "DELETE FROM footnotes WHERE ayah_translation_id IN (" +
            "SELECT id FROM ayah_translations WHERE translation_id = :translationId" +
        ")"
    )
    suspend fun deleteByTranslationId(translationId: Int)
}
