package com.thesunnahrevival.sunnahassistant.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "lines",
    foreignKeys = [ForeignKey(
        entity = Ayah::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("ayah_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Line(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "page_number") val pageNumber: Int,
    @ColumnInfo(name = "line_number") val lineNumber: Int,
    @ColumnInfo(name = "min_x") val minX: Int,
    @ColumnInfo(name = "min_y") val minY: Int,
    @ColumnInfo(name = "max_x") val maxX: Int,
    @ColumnInfo(name = "max_y") val maxY: Int,
    @ColumnInfo(name = "ayah_id") val ayahId: Int
)