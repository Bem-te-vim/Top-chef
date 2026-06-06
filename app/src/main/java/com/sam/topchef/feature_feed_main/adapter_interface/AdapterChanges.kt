package com.sam.topchef.feature_feed_main.adapter_interface

interface AdapterChanges {
    fun onRecipeLiked(id: Int, isFavorite: Boolean)
    fun onRecipeClicked(id: Int)
    fun onRecipeTools(id: Int)
}