package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "translations",
    foreignKeys = [ForeignKey(
        entity = Language::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("language_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Translation(
    @PrimaryKey val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "language_id") val languageId: Int,
    val key: String,
    var selected: Boolean = false,
    var order: Int? = null
)