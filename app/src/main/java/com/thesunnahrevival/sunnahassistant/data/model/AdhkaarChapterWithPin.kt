package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class AdhkaarChapterWithPin(
    @Embedded
    val adhkaarChapter: AdhkaarChapter,

    @Relation(
        parentColumn = "chapter_id",
        entityColumn = "chapter_id"
    )
    val pinnedAdhkaarChapter: PinnedAdhkaarChapter?
) {
    fun toAdhkaarChapter(): AdhkaarChapter {
        return adhkaarChapter.apply {
            pinOrder = pinnedAdhkaarChapter?.pinOrder
        }
    }
}
