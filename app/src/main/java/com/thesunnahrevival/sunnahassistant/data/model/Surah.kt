package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "surahs")
data class Surah(
    @PrimaryKey val id: Int,

    @ColumnInfo(name = "arabic_name")
    val arabicName: String,

    @ColumnInfo(name = "transliterated_name")
    val transliteratedName: String,

    @ColumnInfo(name = "is_makki")
    val isMakki: Boolean,

    @ColumnInfo(name = "verse_count")
    val verseCount: Int,

    @ColumnInfo(name = "start_page")
    val startPage: Int
) : Serializable