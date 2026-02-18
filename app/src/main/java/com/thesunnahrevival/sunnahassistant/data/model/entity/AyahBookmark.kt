package com.thesunnahrevival.sunnahassistant.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ayah_bookmarks",
    foreignKeys = [ForeignKey(
        entity = Ayah::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("ayah_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class AyahBookmark(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "ayah_id") val ayahId: Int,
)