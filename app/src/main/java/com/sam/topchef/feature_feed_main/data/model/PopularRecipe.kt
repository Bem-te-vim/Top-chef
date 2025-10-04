package com.sam.topchef.feature_feed_main.data.model

data class PopularRecipe(
    val id: Int,
    val chef: String,
    val difficult: Int,
    val time: Int,
    val title: String,
    val coverUrl: String? = null,
    val reviews: Double,
    var isFavorite: Boolean = false,
)
