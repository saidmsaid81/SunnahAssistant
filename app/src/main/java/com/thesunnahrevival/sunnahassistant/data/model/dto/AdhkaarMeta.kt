package com.thesunnahrevival.sunnahassistant.data.model.dto

import com.google.gson.annotations.SerializedName

data class AdhkaarMeta(
    val version: Long,
    @SerializedName("updated_at")
    val updatedAt: String
)
