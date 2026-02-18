package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesunnahrevival.sunnahassistant.data.model.entity.Line

@Dao
interface LineDao {
    @Query("SELECT count(*) FROM lines")
    suspend fun countLines(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(line: Line)

    @Query("SELECT * FROM lines WHERE page_number = :pageNumber ")
    suspend fun getLineByPageNumber(pageNumber: Int): List<Line>

    @Query("SELECT * FROM lines WHERE ayah_id = :ayahId ")
    suspend fun getLineByAyahId(ayahId: Int): List<Line>
}
