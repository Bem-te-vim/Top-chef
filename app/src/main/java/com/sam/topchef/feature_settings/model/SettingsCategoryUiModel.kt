package com.sam.topchef.feature_settings.model

data class SettingsCategoryUiModel(
    val id: Int?,
    val name: String,
    val recipeCount: Int,
    val canDelete: Boolean
)
