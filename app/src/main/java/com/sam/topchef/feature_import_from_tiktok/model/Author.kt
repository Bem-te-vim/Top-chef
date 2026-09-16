package com.sam.topchef.feature_import_from_tiktok.model

import com.google.gson.annotations.SerializedName

data class Author(
    @SerializedName("nickname")
    val name: String,

    @SerializedName("avatar")
    val avatar: String
)