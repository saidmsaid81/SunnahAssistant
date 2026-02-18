package com.thesunnahrevival.sunnahassistant.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "adhkaar_item_bookmarks"
)
data class AdhkaarItemBookmark(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "adhkaar_item_id") val adhkaarItemId: Int
)