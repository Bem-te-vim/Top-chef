package com.sam.topchef.feature_feed_main.data.model

sealed class MainFeedItem {

    object Categories : MainFeedItem()

    object PopularRecipes : MainFeedItem()

    data class RecipePost(
        val id: Int,
        val title: String,
        val imageUrl: String? = null,
        val isFavorite: Boolean,
        val reviews: Double
    ): MainFeedItem()
}