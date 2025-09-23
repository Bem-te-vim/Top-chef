package com.sam.topchef.feature_recipe_detail.data.model

sealed class RecipeDetailItem {

    data class Header(
        val type: String,
        val title: String,
        val chefName: String,
        val chefImage: Int,
        val description: String,
        val cookingTime: String,
        val cuisine: String
    ) : RecipeDetailItem()

    object AutoComplete : RecipeDetailItem()

    data class Ingredient(val name: String) : RecipeDetailItem()

}