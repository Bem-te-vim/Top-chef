package com.sam.topchef.feature_feed_main.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.databinding.ActivityMainBinding
import com.sam.topchef.feature_add_recipe.ui.activity.AddRecipeActivity
import com.sam.topchef.feature_feed_main.adapter.FeedMainAdapter
import com.sam.topchef.feature_feed_main.data.model.MainFeedItem
import com.sam.topchef.feature_feed_main.data.model.PopularRecipe
import com.sam.topchef.feature_feed_main.data.model.RecipeCategory
import com.sam.topchef.feature_recipe_detail.ui.activity.RecipeDetailActivity
import com.sam.topchef.feature_search.activities.SearchActivity
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity
import com.sam.topchef.feature_shopping_list.activities.ShoppingListActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var result: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityMainBinding
    private lateinit var feedMainAdapter: FeedMainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val rvMain = binding.rvMain
        rvMain.layoutManager = LinearLayoutManager(this)
        feedMainAdapter = FeedMainAdapter()
        rvMain.adapter = feedMainAdapter
        loadData()

        feedMainAdapter.onItemClickListener = { id ->
            val i = Intent(this, RecipeDetailActivity::class.java)
            i.putExtra("id", id)
            this.startActivity(i)
        }
        feedMainAdapter.onWhatShowListener = { whatShow ->
            val i = Intent(this, SeeAllActivity::class.java)
            when (whatShow) {
                "AllPopularRecipes" -> i.putExtra("whatShow", "AllPopularRecipes")
                "AllCategories" -> i.putExtra("whatShow", "AllCategories")
            }
            startActivity(i)
        }

        feedMainAdapter.onLongClickListenerToDelete = { position, id ->

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Deletar essa receita⚠?")
                .setNegativeButton("Cancelar") { p0, _ -> p0.dismiss() }
                .setPositiveButton("Deletar") { _, _ ->
                    feedMainAdapter.removeItem(position)
                    thread {
                        val app = application as App
                        val dao = app.db.recipeDao()
                        dao.delete(id)
                        runOnUiThread {
                            Toast.makeText(this, "Receita deletada :(", Toast.LENGTH_LONG).show()
                        }
                    }
                }.show()

            true
        }

        binding.imageProfile.setOnClickListener {
            //todo: not implemented yet -> open Activity ProfileActivity
        }

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }

        binding.textInputSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.btnAddRecipe.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        thread {
            val app = application as App
            val dao = app.db.recipeDao()
            val allRecipes = dao.getAllRecipes()

            val popularRecipes = allRecipes.shuffled().take(10).map {
                PopularRecipe(
                    it.id,
                    it.chef ?: "Default",
                    it.difficult,
                    it.preparationTime,
                    it.title,
                    if (it.imageUriString.isNotEmpty()) {
                        it.imageUriString.first()
                    } else {
                        null
                    },
                    it.reviews,
                    it.isFavorite
                )
            }//.filter { it.reviews > 0 } not implemented yet

            val categories = allRecipes.shuffled().take(10).map {
                RecipeCategory(
                    it.type ?: "Default",
                    if (it.imageUriString.isNotEmpty()) {
                        it.imageUriString.random()
                    } else {
                        null
                    }
                )
            }


            val mainPosts = allRecipes.map {
                MainFeedItem.RecipePost(
                    it.id,
                    it.title,
                    if (it.imageUriString.isNotEmpty()) {
                        it.imageUriString.first()
                    } else {
                        null
                    },
                    it.isFavorite,
                    it.reviews
                )
            }

            /**
             * the order in which items are added affects the order of the feed
             */
            val items = mutableListOf<MainFeedItem>()
            items.add(MainFeedItem.PopularRecipes(popularRecipes))
            items.add(MainFeedItem.Categories(categories))
            items.addAll(mainPosts)

            runOnUiThread {
                feedMainAdapter.setItems(items)
            }
        }
    }
}