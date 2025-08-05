package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "adhkaar_chapters")
data class AdhkaarChapter(
    @PrimaryKey 
    @SerializedName("_id")
    val id: Int,

    @ColumnInfo(name = "chapter_id")
    @SerializedName("chapter_id")
    val chapterId: Int,

    @ColumnInfo(name = "language")
    @SerializedName("language")
    val language: String,

    @ColumnInfo(name = "chapter_name")
    @SerializedName("chapter_name")
    val chapterName: String
) : Serializable