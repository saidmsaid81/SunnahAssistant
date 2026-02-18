package com.thesunnahrevival.sunnahassistant.data.model.dto

data class NotificationSettings(
    val notificationTone: String,
    val shouldVibrate: Boolean,
    val notificationImportance: String
)