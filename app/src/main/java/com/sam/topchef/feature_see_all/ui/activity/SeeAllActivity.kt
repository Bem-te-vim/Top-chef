package com.sam.topchef.feature_see_all.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
        const val CATEGORY_FILTER = "CategoryFilter"
        const val EXTRA_CATEGORY_NAME = "extra_category_name"
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
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME)
        
        val rvSeeAll = binding.rvSeeAll
        
        when (i) {
            ALL_POPULAR_RECIPES -> {
                rvSeeAll.layoutManager = GridLayoutManager(this, 3)
                seeAllAdapter = SeeAllAdapter(R.layout.row_images)
                loadPopularRecipe()
            }
            ALL_CATEGORIES -> {
                rvSeeAll.layoutManager = GridLayoutManager(this, 4)
                seeAllAdapter = SeeAllAdapter(R.layout.row_categories_recipe_item)
                loadCategories()
            }
            CATEGORY_FILTER -> {
                rvSeeAll.layoutManager = LinearLayoutManager(this)
                seeAllAdapter = SeeAllAdapter(R.layout.row_popular_recipe_item)
                loadRecipesByCategory(categoryName ?: "")
            }
        }

        rvSeeAll.adapter = seeAllAdapter

        seeAllAdapter.itemClick = { id ->
            val i = Intent(this, RecipeDetailActivity::class.java)
            i.putExtra("id", id)
            startActivity(i)
        }
        seeAllAdapter.likeClick = { id, isFavorite ->
            updateRecipeFavoriteStatus(id, isFavorite)
        }
        seeAllAdapter.categoryClick = { category ->
            val i = Intent(this, SeeAllActivity::class.java).apply {
                putExtra("show", CATEGORY_FILTER)
                putExtra(EXTRA_CATEGORY_NAME, category)
            }
            startActivity(i)
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun updateRecipeFavoriteStatus(id: Int, isFavorite: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            val recipe = app.recipeDao.getRecipe(id)
            recipe?.let {
                it.isFavorite = isFavorite
                app.recipeDao.update(it)
            }
        }
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
                (application as App).recipeDao.getAllRecipes()
                    .filter { !it.type.isNullOrBlank() }
                    .distinctBy { it.type }
            }

            showProgressBar(allCategories.size)
            binding.customToolbarTitle.text = getString(R.string.categories)
            seeAllAdapter.submitList(allCategories)
        }
    }

    /**
     * Loads and displays recipes filtered by a specific category name.
     */
    private fun loadRecipesByCategory(categoryName: String) {
        lifecycleScope.launch {
            val recipes = withContext(Dispatchers.IO) {
                (application as App).recipeDao.getAllRecipes().filter { it.type == categoryName }
            }

            showProgressBar(recipes.size)
            binding.customToolbarTitle.text = categoryName
            seeAllAdapter.submitList(recipes)
        }
    }

    /**
     * Toggles the visibility of the progress bar or empty state message.
     * @param result The number of items loaded.
     */
    private fun showProgressBar(result: Int) {
        binding.progressBar.visibility = View.GONE
        if (result > 0) {
            binding.txtMessage.visibility = View.GONE
        } else {
            binding.txtMessage.visibility = View.VISIBLE
        }
    }

}