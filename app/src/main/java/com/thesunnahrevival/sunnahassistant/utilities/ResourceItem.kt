package com.thesunnahrevival.sunnahassistant.utilities

data class ResourceItem(
    val id: Int,
    val title: String,
    val description: String,
    val destination: Int,
    val pageNumbers: List<Int> = listOf()
)