package com.thesunnahrevival.sunnahassistant.data.model.embedded

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarItem

data class AdhkaarItemWithBookmarkEmbedded(
    @Embedded val item: AdhkaarItem,
    @ColumnInfo(name = "bookmarked") val bookmarked: Boolean
)