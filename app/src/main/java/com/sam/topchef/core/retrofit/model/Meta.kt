package com.sam.topchef.core.retrofit.model

import com.google.gson.annotations.SerializedName

data class Meta(
    @SerializedName("itemCount")
    val  itemCount: Int,

    @SerializedName("totalItems")
    val totalItems: Int,

    @SerializedName("itemsPerPage")
    val itemsPerPage: Int,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("currentPage")
    val currentPage: Int
)