package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class SurahWithPin(
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