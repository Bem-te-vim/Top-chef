package com.sam.topchef.feature_profile.model


data class VideoThumbnail(
    val videoId: Int,
    val thumbnailPath: String? = null,
    val title: String,
    val description: String = ""
)
