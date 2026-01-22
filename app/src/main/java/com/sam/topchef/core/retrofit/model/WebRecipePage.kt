package com.sam.topchef.core.retrofit.model

import com.google.gson.annotations.SerializedName

data class WebRecipePage(
    @SerializedName("items")
    val data: List<WebRecipe>,

    @SerializedName("meta")
    val meta: Meta,



)
