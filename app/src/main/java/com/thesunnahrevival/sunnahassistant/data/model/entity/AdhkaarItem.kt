package com.thesunnahrevival.sunnahassistant.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    tableName = "adhkaar_items"
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
    val reference: String?,

    @Ignore
    val bookmarked: Boolean = false
) : Serializable {
    constructor(
            id: Int,
            itemId: Int,
            language: String,
            itemTranslation: String,
            chapterId: Int,
            reference: String?,
    ) : this(id, itemId, language, itemTranslation, chapterId, reference, false)
}