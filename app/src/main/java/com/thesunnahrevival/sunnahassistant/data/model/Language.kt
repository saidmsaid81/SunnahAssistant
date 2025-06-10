package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "languages")
data class Language(
    @PrimaryKey val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "is_rtl") val isRtl: Boolean
)