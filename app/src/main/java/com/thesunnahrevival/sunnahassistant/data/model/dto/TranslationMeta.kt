package com.thesunnahrevival.sunnahassistant.data.model.dto

import com.google.gson.annotations.SerializedName

data class TranslationMeta(
    val key: String,
    val version: Long,
    @SerializedName("updated_at") val updatedAt: String
)
