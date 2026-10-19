package com.sam.topchef.feature_settings.model

data class SettingsRecipeUiModel(
    val id: Int,
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
    val isTikTok: Boolean,
    val isFavorite: Boolean,
    var isSelected: Boolean = false
)
