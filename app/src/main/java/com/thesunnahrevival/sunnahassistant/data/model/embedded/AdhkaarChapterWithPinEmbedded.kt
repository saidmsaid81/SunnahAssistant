package com.thesunnahrevival.sunnahassistant.data.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.thesunnahrevival.sunnahassistant.data.model.entity.AdhkaarChapter
import com.thesunnahrevival.sunnahassistant.data.model.entity.PinnedAdhkaarChapter

data class AdhkaarChapterWithPinEmbedded(
    @Embedded
    val adhkaarChapter: AdhkaarChapter,

    @Relation(
        parentColumn = "chapter_id",
        entityColumn = "adhkaar_chapter_id"
    )
    val pinnedAdhkaarChapter: PinnedAdhkaarChapter?
) {
    fun toAdhkaarChapter(): AdhkaarChapter {
        return adhkaarChapter.apply {
            pinOrder = pinnedAdhkaarChapter?.pinOrder
        }
    }
}