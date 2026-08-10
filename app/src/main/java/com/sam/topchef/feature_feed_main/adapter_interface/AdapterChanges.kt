package com.sam.topchef.feature_feed_main.adapter_interface

interface AdapterChanges {
    fun onRecipeLiked(id: Int, isFavorite: Boolean, isTikTok: Boolean = false)
    fun onRecipeClicked(id: Int)
    fun onTikTokRecipeClicked(id: Int)
    fun onRecipeTools(id: Int, isTikTok: Boolean = false)
}