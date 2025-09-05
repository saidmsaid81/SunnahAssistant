package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    tableName = "adhkaar_chapters",
    indices = [Index(value = ["chapter_id"])]
)
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
    val chapterName: String,

    @ColumnInfo(name = "category_name")
    @SerializedName("category_name")
    val categoryName: String,

    @Ignore
    var pinOrder: Int? = null
) : Serializable {
    constructor(
            id: Int,
            chapterId: Int,
            language: String,
            chapterName: String,
            categoryName: String
    ) : this(id, chapterId, language, chapterName, categoryName, null)
}