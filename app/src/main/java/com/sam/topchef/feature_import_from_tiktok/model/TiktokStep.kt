package com.sam.topchef.feature_import_from_tiktok.model

import com.google.gson.annotations.SerializedName

data class TiktokStep(
    @SerializedName("step_name")
    val stepName: String,
    @SerializedName("step_desc")
    val stepDesc: String
)
