package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ayahs",
    foreignKeys = [ForeignKey(
        entity = Surah::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("surah_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Ayah(
    @PrimaryKey val id: Int,
    val number: Int,
    @ColumnInfo(name = "surah_id") val surahId: Int,
    @ColumnInfo(name = "arabic_text") val arabicText: String
)