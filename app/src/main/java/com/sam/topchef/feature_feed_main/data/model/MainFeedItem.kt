package com.sam.topchef.feature_feed_main.data.model

sealed class MainFeedItem {
    data class PopularSection(val items: List<PopularRecipe>) : MainFeedItem()
    data class CategorySection(val items: List<RecipeCategory>) : MainFeedItem()
    data class PostItem(val post: RecipePost) : MainFeedItem()
}
