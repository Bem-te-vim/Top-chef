package com.sam.topchef.fature_import_recipe.model

data class RecipeModel(
    val title: String,
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val preparationMode: List<String> = emptyList(),
    val totalTime: String = ""
)
