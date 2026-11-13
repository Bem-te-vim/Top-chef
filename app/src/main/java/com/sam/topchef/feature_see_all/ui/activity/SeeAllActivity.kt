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
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.databinding.ActivitySeeAllBinding
import com.sam.topchef.feature_import_from_tiktok.view.TiktokImportActivity
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

        setupRecyclerView()
        setupListeners()
        loadData()
    }

    private fun setupRecyclerView() {
        val showType = intent.getStringExtra("show") ?: throw NullPointerException()
        val rvSeeAll = binding.rvSeeAll
        
        when (showType) {
            ALL_POPULAR_RECIPES -> {
                rvSeeAll.layoutManager = GridLayoutManager(this, 3)
                seeAllAdapter = SeeAllAdapter(R.layout.row_images)
            }
            ALL_CATEGORIES -> {
                rvSeeAll.layoutManager = GridLayoutManager(this, 4)
                seeAllAdapter = SeeAllAdapter(R.layout.row_categories_recipe_item)
            }
            CATEGORY_FILTER -> {
                rvSeeAll.layoutManager = LinearLayoutManager(this)
                seeAllAdapter = SeeAllAdapter(R.layout.row_popular_recipe_item)
            }
        }
        rvSeeAll.adapter = seeAllAdapter
    }

    private fun setupListeners() {
        seeAllAdapter.itemClick = { id, isTikTok ->
            if (isTikTok) {
                val i = Intent(this@SeeAllActivity, TiktokImportActivity::class.java).apply {
                    putExtra("tiktokId", id)
                }
                startActivity(i)
            } else {
                val i = Intent(this@SeeAllActivity, RecipeDetailActivity::class.java).apply {
                    putExtra("id", id)
                }
                startActivity(i)
            }
        }
        seeAllAdapter.itemLongClick = { id, isTikTok ->
            com.sam.topchef.core.utils.RecipeToolsDialog.show(
                this@SeeAllActivity,
                id = id,
                isTikTok = isTikTok,
                onDeleted = { loadData() }
            )
        }
        seeAllAdapter.likeClick = { id, isFavorite, isTikTok ->
            updateRecipeFavoriteStatus(id, isFavorite, isTikTok)
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

    private fun loadData() {
        val showType = intent.getStringExtra("show") ?: throw NullPointerException()
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME)

        when (showType) {
            ALL_POPULAR_RECIPES -> loadPopularRecipe()
            ALL_CATEGORIES -> loadCategories()
            CATEGORY_FILTER -> loadRecipesByCategory(categoryName ?: "")
        }
    }

    private fun updateRecipeFavoriteStatus(id: Int, isFavorite: Boolean, isTikTok: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            if (isTikTok) {
                val tiktok = app.tiktokDao.getById(id)
                tiktok?.let {
                    it.isFavorite = isFavorite
                    app.tiktokDao.update(it)
                }
            } else {
                val recipe = app.recipeDao.getRecipe(id)
                recipe?.let {
                    it.isFavorite = isFavorite
                    app.recipeDao.update(it)
                }
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
                val app = application as App
                val regular = app.recipeDao.getAllRecipes().filter { !it.type.isNullOrBlank() }
                val tiktok = app.tiktokDao.getAll().filter { !it.type.isNullOrBlank() }.map { t ->
                    Recipe(
                        id = t.id,
                        title = t.name,
                        imageUriString = listOfNotNull(t.getEffectiveThumbnailPath()),
                        type = t.type,
                        description = t.description,
                        chef = t.authorName,
                        isFavorite = t.isFavorite,
                        difficult = 1
                    ).apply { this.isTikTok = true }
                }
                (regular + tiktok).distinctBy { it.type?.trim()?.lowercase(java.util.Locale.getDefault()) }
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
                val app = application as App
                val regularRecipes = app.recipeDao.getAllRecipes().filter {
                    it.type != null && it.type.trim().equals(categoryName.trim(), ignoreCase = true)
                }
                val tiktokRecipes = app.tiktokDao.getAll().filter {
                    it.type != null && it.type.trim().equals(categoryName.trim(), ignoreCase = true)
                }.map { t ->
                    Recipe(
                        id = t.id,
                        title = t.name,
                        imageUriString = listOfNotNull(t.getEffectiveThumbnailPath()),
                        type = t.type,
                        description = t.description,
                        chef = t.authorName,
                        isFavorite = t.isFavorite,
                        difficult = 1
                    ).apply { this.isTikTok = true }
                }
                regularRecipes + tiktokRecipes
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
