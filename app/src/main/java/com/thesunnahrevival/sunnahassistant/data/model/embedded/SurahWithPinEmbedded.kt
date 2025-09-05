package com.thesunnahrevival.sunnahassistant.data.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.thesunnahrevival.sunnahassistant.data.model.entity.PinnedSurah
import com.thesunnahrevival.sunnahassistant.data.model.entity.Surah

data class SurahWithPinEmbedded(
    @Embedded val surah: Surah,

    @Relation(
        parentColumn = "id",
        entityColumn = "surah_id"
    )
    val pinnedSurah: PinnedSurah?
) {
    fun toSurah(): Surah {
        return surah.apply {
            pinOrder = pinnedSurah?.pinOrder
        }
    }
}