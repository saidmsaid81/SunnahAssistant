package com.thesunnahrevival.sunnahassistant.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pinned_surahs",
    foreignKeys = [
        ForeignKey(
            entity = Surah::class,
            parentColumns = ["id"],
            childColumns = ["surah_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PinnedSurah(
    @PrimaryKey
    @ColumnInfo(name = "surah_id")
    val surahId: Int,
    
    @ColumnInfo(name = "pin_order")
    val pinOrder: Int
)