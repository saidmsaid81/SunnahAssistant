package com.thesunnahrevival.sunnahassistant.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "page_bookmarks")
data class PageBookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "page_number") val pageNumber: Int,
    @ColumnInfo(name = "created_at") val createdAt: Date = Date(),
    @ColumnInfo(name = "note") val note: String? = null
)