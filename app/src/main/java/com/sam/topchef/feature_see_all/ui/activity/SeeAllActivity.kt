package com.sam.topchef.feature_see_all.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.sam.topchef.R

class SeeAllActivity : AppCompatActivity() {

    companion object {
        const val ALL_POPULAR_RECIPES = "AllPopularRecipes"
        const val ALL_CATEGORIES = "AllCategories"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_all)
        enableEdgeToEdge()
    }
}