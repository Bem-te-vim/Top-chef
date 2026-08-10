package com.sam.topchef.feature_import_from_tiktok.model

import com.google.gson.annotations.SerializedName
import com.sam.topchef.feature_profile.model.VideoThumbnail

data class Data(
    @SerializedName("title")
    val title: String,

    @SerializedName("play")
    val videoUrl: String,

    @SerializedName("cover")
    val thumbnail: String

)
