package com.sam.topchef.feature_import_from_tiktok.model

import com.google.gson.annotations.SerializedName

data class TiktokSection(
    @SerializedName("sectionName")
    val sectionName: String,
    @SerializedName("sectionItems")
    val sectionItems: List<String>
)
