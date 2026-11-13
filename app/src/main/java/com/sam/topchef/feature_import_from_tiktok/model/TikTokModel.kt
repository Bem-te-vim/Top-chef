package com.sam.topchef.feature_import_from_tiktok.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.File


@Entity(tableName = "Tiktok")
data class TikTokModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo("is_favorite")
    var isFavorite: Boolean = false,

    @ColumnInfo("thumbnail")
    val thumbnail: String? = null,

    @ColumnInfo("local_thumbnail_path")
    val localThumbnailPath: String? = null,

    @ColumnInfo("video_url")
    val videoUrl: String? = null,

    @ColumnInfo("local_video_path")
    val localVideoPath: String? = null,

    @ColumnInfo("origin_url")
    val originUrl: String? = null,

    @SerializedName("name")
    @ColumnInfo(name = "name")
    val name: String,

    @SerializedName("description")
    @ColumnInfo(name = "description")
    val description: String,

    @SerializedName(value = "category", alternate = ["type"])
    @ColumnInfo(name = "type")
    val type: String? = null,

    @SerializedName("ingredients_section")
    @ColumnInfo(name = "ingredients")
    val ingredients: List<TiktokSection>,

    @SerializedName("preparation_mode_section")
    @ColumnInfo(name = "preparation_mode")
    val preparationMode: List<TiktokStep>,

    @ColumnInfo("author_name")
    val authorName: String? = null,

    @ColumnInfo("author_avatar")
    val authorAvatar: String? = null,

    @ColumnInfo("local_author_avatar_path")
    val localAuthorAvatarPath: String? = null,

    @ColumnInfo(name = "likes_count", defaultValue = "0")
    val likesCount: Int = 0
) {
    fun getEffectiveThumbnailPath(): String? {
        if (!localThumbnailPath.isNullOrEmpty()) {
            val file = File(localThumbnailPath)
            if (file.exists() && file.length() > 0) {
                return file.absolutePath
            }
        }
        return thumbnail
    }

    fun getEffectiveAvatarPath(): String? {
        if (!localAuthorAvatarPath.isNullOrEmpty()) {
            val file = File(localAuthorAvatarPath)
            if (file.exists() && file.length() > 0) {
                return file.absolutePath
            }
        }
        return authorAvatar
    }
}
