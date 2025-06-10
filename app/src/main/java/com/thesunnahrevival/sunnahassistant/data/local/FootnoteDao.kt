package com.thesunnahrevival.sunnahassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.thesunnahrevival.sunnahassistant.data.model.Footnote

@Dao
interface FootnoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(footnote: Footnote)
}