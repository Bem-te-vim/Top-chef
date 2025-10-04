package com.sam.topchef.feature_feed_main.data.model

sealed class MainFeedItem {

    data class Categories(
        val categories: List<RecipeCategory>
    ) : MainFeedItem()

    data class PopularRecipes(
        val popularRecipes: List<PopularRecipe>
    ) : MainFeedItem()

    data class RecipePost(
        val id: Int,
        val title: String,
        val coverUrl: String? = null,
        var isFavorite: Boolean,
        val reviews: Double
    ): MainFeedItem()
}