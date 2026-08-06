package com.sam.topchef.feature_import_from_tiktok.model

import com.google.gson.annotations.SerializedName

data class TikTokModel(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("ingredients_section")
    val ingredients: List<TiktokSection>,
    @SerializedName("preparation_mode_section")
    val preparationMode: List<TiktokStep>
)