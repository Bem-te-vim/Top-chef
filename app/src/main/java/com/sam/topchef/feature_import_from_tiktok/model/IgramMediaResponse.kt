package com.sam.topchef.feature_import_from_tiktok.model

import com.google.gson.annotations.SerializedName

data class IgramMediaResponse(
    @SerializedName("video_url")
    val videoUrl: String?,

    @SerializedName("thumbnail")
    val thumbnail: String?,

    @SerializedName("caption")
    val caption: String?,

    @SerializedName("author")
    val author: InstagramAuthor?,

    @SerializedName("formats")
    val formats: List<InstagramFormat>?,

    @SerializedName("like_count")
    val likesCount: Int?
)
data class InstagramAuthor(
    @SerializedName("username")
    val username: String?,

    @SerializedName("profile_picture_url")
    val profilePicture: String?
)

data class InstagramFormat(
    @SerializedName("id")
    val id: String?,

    @SerializedName("quality")
    val quality: String?,

    @SerializedName("url")
    val url: String?,

    @SerializedName("audio_url")
    val audioUrl: String?
)