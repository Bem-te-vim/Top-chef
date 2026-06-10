package com.sam.topchef.feature_import_recipe_from_tiktok.model

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("title")
    val title: String,

    @SerializedName("play")
    val videoUrl: String,

)
