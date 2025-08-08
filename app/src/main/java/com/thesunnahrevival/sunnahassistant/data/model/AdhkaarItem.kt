package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    tableName = "adhkaar_items",
    foreignKeys = [ForeignKey(
        entity = AdhkaarChapter::class,
        parentColumns = ["chapter_id"],
        childColumns = ["chapter_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AdhkaarItem(
    @PrimaryKey
    @SerializedName("_id")
    val id: Int,

    @ColumnInfo(name = "item_id")
    @SerializedName("item_id")
    val itemId: Int,

    @ColumnInfo(name = "language")
    @SerializedName("language")
    val language: String,

    @ColumnInfo(name = "item_translation")
    @SerializedName("item_translation")
    val itemTranslation: String,

    @ColumnInfo(name = "chapter_id")
    @SerializedName("chapter_id")
    val chapterId: Int,

    @ColumnInfo(name = "reference")
    @SerializedName("reference")
    val reference: String?
) : Serializable