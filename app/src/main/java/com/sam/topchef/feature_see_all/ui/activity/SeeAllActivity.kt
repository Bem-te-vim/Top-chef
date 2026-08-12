package com.sam.topchef.feature_see_all.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.databinding.ActivitySeeAllBinding
import com.sam.topchef.feature_recipe_detail.ui.activity.RecipeDetailActivity
import com.sam.topchef.feature_see_all.adapter.SeeAllAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for displaying a scrollable list of recipes from a specific category or collection.
 * Triggered from "See All" buttons in the main feed.
 */
class SeeAllActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeeAllBinding
    private lateinit var seeAllAdapter: SeeAllAdapter

    companion object {
        const val ALL_POPULAR_RECIPES = "AllPopularRecipes"
        const val ALL_CATEGORIES = "AllCategories"
    }

    /**
     * Initializes the activity, determines whether to show popular recipes or categories,
     * and sets up the grid layout and adapter.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeeAllBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val i = intent.getStringExtra("show") ?: throw NullPointerException()
        when (i) {
            ALL_POPULAR_RECIPES -> loadPopularRecipe()
            ALL_CATEGORIES -> loadCategories()
        }

        val rvSeeAll = binding.rvSeeAll
        rvSeeAll.layoutManager = GridLayoutManager(this, 3)
        seeAllAdapter = SeeAllAdapter()
        rvSeeAll.adapter = seeAllAdapter

        seeAllAdapter.itemClick = { id ->
            val i = Intent(this, RecipeDetailActivity::class.java)
            i.putExtra("id", id)
            startActivity(i)
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    /**
     * Loads and displays all recipes sorted by their review scores.
     */
    private fun loadPopularRecipe() {
        lifecycleScope.launch {
            val popularRecipe = withContext(Dispatchers.IO) {
                (application as App).recipeDao.getAllRecipes().sortedBy { it.reviews }
            }

            showProgressBar(popularRecipe.size)
            binding.customToolbarTitle.text = getString(R.string.popular_recipes)
            seeAllAdapter.submitList(popularRecipe)
        }
    }

    /**
     * Loads and displays all recipes filtered by category.
     */
    private fun loadCategories() {
        lifecycleScope.launch {
            val allCategories = withContext(Dispatchers.IO) {
                (application as App).recipeDao.getAllRecipes().filter { it.type != null }
            }

            showProgressBar(allCategories.size)
            binding.customToolbarTitle.text = getString(R.string.categories)
            seeAllAdapter.submitList(allCategories)
        }
    }

    /**
     * Toggles the visibility of the progress bar or empty state message.
     * @param result The number of items loaded.
     */
    private fun showProgressBar(result: Int) {
        if (result > 1) {
            binding.progressBar.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.txtMessage.visibility = View.VISIBLE
        }
    }

}