package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "daily_hadith")
data class DailyHadith(
    @PrimaryKey var id: Long,
    var title: String,
    var pubDateMilliseconds: Long,
    var content: String
)