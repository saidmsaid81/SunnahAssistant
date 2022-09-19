package com.thesunnahrevival.common.data.model

data class ReminderDate(
    val day: Int,
    val month: Int,
    val year: Int,
    var dayOfWeek: String? = ""
)