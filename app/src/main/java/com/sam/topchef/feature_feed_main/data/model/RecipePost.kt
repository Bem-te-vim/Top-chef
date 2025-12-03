package com.sam.topchef.feature_feed_main.data.model

data class RecipePost(
    val id: Int,
    val title: String,
    val coverUrl: String? = null,
    var isFavorite: Boolean,
    val reviews: Double
)
