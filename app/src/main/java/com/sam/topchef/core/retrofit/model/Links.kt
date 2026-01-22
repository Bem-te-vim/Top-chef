package com.sam.topchef.core.retrofit.model

import com.google.gson.annotations.SerializedName

data class Links(

    @SerializedName("first")
    val first: String,

    @SerializedName("previous")
    val previews: String,

    @SerializedName("next")
    val next: String,

    @SerializedName("last")
    val last: String
)
