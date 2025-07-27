package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class AyahWithSurah(
    @Embedded val ayah: Ayah,
    @Relation(
        parentColumn = "surah_id",
        entityColumn = "id"
    )
    val surah: Surah
)