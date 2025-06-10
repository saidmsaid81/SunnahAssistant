package com.thesunnahrevival.sunnahassistant.utilities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResourceItem(
    val id: Int,
    val title: String,
    val description: String,
    val destination: Int,
    val startPage: Int = 1
) : Parcelable
