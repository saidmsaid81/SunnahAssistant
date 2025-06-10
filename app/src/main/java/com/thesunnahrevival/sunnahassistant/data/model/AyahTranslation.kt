package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ayah_translations",
    foreignKeys = [
        ForeignKey(
            entity = Translation::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("translation_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ayah::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("ayah_id")
        )
    ]
)
data class AyahTranslation(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "translation_id") val translationId: Int,
    val text: String,
    @ColumnInfo(name = "ayah_id") val ayahId: Int
)