package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "footnotes",
    foreignKeys = [ForeignKey(
        entity = AyahTranslation::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("ayah_translation_id")
    )]
)
data class Footnote(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "ayah_translation_id") val ayahTranslationId: Int,
    val number: Int,
    val text: String
)