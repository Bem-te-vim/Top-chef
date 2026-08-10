package com.sam.topchef.feature_import_from_tiktok.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.sam.topchef.feature_profile.model.VideoThumbnail

@Entity(tableName = "Tiktok")
data class TikTokModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo("is_favorite")
    var isFavorite: Boolean = false,

    @ColumnInfo("thumbnail")
    val thumbnail: String? = null,

    @ColumnInfo("video_url")
    val videoUrl: String? = null,

    @SerializedName("name")
    @ColumnInfo(name = "name")
    val name: String,

    @SerializedName("description")
    @ColumnInfo(name = "description")
    val description: String,

    @SerializedName("ingredients_section")
    @ColumnInfo(name = "ingredients")
    val ingredients: List<TiktokSection>,

    @SerializedName("preparation_mode_section")
    @ColumnInfo(name = "preparation_mode")
    val preparationMode: List<TiktokStep>
)
