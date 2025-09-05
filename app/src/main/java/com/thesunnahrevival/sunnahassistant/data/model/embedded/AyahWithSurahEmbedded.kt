package com.thesunnahrevival.sunnahassistant.data.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.thesunnahrevival.sunnahassistant.data.model.entity.Ayah
import com.thesunnahrevival.sunnahassistant.data.model.entity.Surah

data class AyahWithSurahEmbedded(
    @Embedded val ayah: Ayah,
    @Relation(
        parentColumn = "surah_id",
        entityColumn = "id"
    )
    val surah: Surah
)