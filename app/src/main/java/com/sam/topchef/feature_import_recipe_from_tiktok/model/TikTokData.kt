package com.sam.topchef.feature_import_recipe_from_tiktok.model

import com.google.gson.annotations.SerializedName

data class TikTokData(
    @SerializedName("msg")
    val msg: String,

    @SerializedName("data")
    val data: Data
    )
