package com.thesunnahrevival.sunnahassistant.data.model.entity

import androidx.room.*

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
    @ColumnInfo(name = "arabic_text") val arabicText: String,
    @Ignore var bookmarked: Boolean = false
) {
    constructor(
        id: Int,
        number: Int,
        surahId: Int,
        arabicText: String
    ) : this(id, number, surahId, arabicText, false)
}