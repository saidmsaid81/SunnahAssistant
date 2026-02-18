package com.thesunnahrevival.sunnahassistant.data.model.dto

data class ToDoDate(
    val day: Int,
    val month: Int,
    val year: Int,
    var dayOfWeek: String? = ""
)