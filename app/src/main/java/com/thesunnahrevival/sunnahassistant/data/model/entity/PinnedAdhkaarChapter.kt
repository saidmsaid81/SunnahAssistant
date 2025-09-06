package com.thesunnahrevival.sunnahassistant.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_adhkaar_chapters")
data class PinnedAdhkaarChapter(
    @PrimaryKey
    @ColumnInfo(name = "adhkaar_chapter_id")
    val chapterId: Int,

    @ColumnInfo(name = "pin_order")
    val pinOrder: Int
)