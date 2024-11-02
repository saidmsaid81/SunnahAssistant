package com.thesunnahrevival.sunnahassistant.utilities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResourceItem(
    val id: Int,
    val title: String,
    val description: String,
    val destination: Int,
    val pageNumbers: List<Int> = emptyList()
) : Parcelable
