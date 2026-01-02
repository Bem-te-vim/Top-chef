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

class SeeAllActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeeAllBinding
    private lateinit var seeAllAdapter: SeeAllAdapter

    companion object {
        const val ALL_POPULAR_RECIPES = "AllPopularRecipes"
        const val ALL_CATEGORIES = "AllCategories"
    }

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

    private fun showProgressBar(result: Int) {
        if (result > 1) {
            binding.progressBar.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.txtMessage.visibility = View.VISIBLE
        }
    }

}