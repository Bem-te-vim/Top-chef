package com.sam.topchef.core.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.util.Date

@Entity
data class Recipe(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "reviews")
    val reviews: Double = 0.0,

    @ColumnInfo(name = "chef")
    val chef: String? = null,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "image_uri")
    val imageUriString: List<String> = listOf(),

    @ColumnInfo(name = "local_image_paths")
    val localImagePaths: List<String>? = null,

    @ColumnInfo(name = "type")
    val type: String? = null,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "preparation_time")
    val preparationTime: Int = 0,

    @ColumnInfo(name = "cooking_time")
    val cookingTime: Int = 0,

    @ColumnInfo(name = "difficult")
    val difficult: Int,

    @ColumnInfo(name = "ingredients")
    val ingredients: List<String> = listOf(),

    @ColumnInfo(name = "preparation_mode")
    val preparationMode: List<String> = listOf(),

    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false,

    @ColumnInfo(name = "created_date")
    val createdDate: Date = Date()
) {
    @androidx.room.Ignore
    var isTikTok: Boolean = false

    fun getEffectiveImageUris(): List<String> {
        if (!localImagePaths.isNullOrEmpty()) {
            val validLocal = localImagePaths.filter { path ->
                val f = File(path)
                f.exists() && f.length() > 0
            }
            if (validLocal.isNotEmpty()) {
                return validLocal
            }
        }
        return imageUriString
    }
}
