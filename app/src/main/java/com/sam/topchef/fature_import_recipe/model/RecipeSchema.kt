package com.sam.topchef.fature_import_recipe.model

data class RecipeSchema(
    val name: String?,
    val description: String?,
    val recipeIngredient: List<String>?,
    val recipeInstructions: Any?,
    val totalTime: String?
)
