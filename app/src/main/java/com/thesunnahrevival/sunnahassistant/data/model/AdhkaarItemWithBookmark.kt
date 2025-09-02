package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class AdhkaarItemWithBookmark(
    @Embedded val item: AdhkaarItem,
    @ColumnInfo(name = "bookmarked") val bookmarked: Boolean
)
