package com.thesunnahrevival.sunnahassistant.data.model.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResourceItem(
    val id: Int,
    val titleResourceKey: Int,
    val descriptionResourceKey: Int,
    val destination: Int
) : Parcelable