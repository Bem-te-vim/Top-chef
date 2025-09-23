package com.sam.topchef.feature_feed_main.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.databinding.ActivityMainBinding
import com.sam.topchef.feature_add_recipe.ui.activity.AddRecipeActivity
import com.sam.topchef.feature_feed_main.adapter.FeedMainAdapter
import com.sam.topchef.feature_feed_main.data.model.MainFeedItem
import com.sam.topchef.feature_recipe_detail.ui.activity.RecipeDetailActivity
import com.sam.topchef.feature_search.activities.SearchActivity
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var feedMainAdapter: FeedMainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textInputSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        binding.imageProfile.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AddRecipeActivity::class.java
                )
            )
        }

        val rvMain = binding.rvMain
        rvMain.layoutManager = LinearLayoutManager(this)
        val mainFeedItems = listOf<MainFeedItem>(
            MainFeedItem.PopularRecipes,
            MainFeedItem.Categories,
            MainFeedItem.RecipePost(
                13412,
                "Macaronada",
                isFavorite = true,
                reviews = 4.8
            ),
            MainFeedItem.RecipePost(
                1414,
                "Macaronada",
                isFavorite = true,
                reviews = 4.8
            ),
            MainFeedItem.RecipePost(
                15245,
                "Macaronada",
                isFavorite = true,
                reviews = 4.8
            ),
            MainFeedItem.RecipePost(
                41524167,
                "Macaronada",
                isFavorite = true,
                reviews = 4.8
            ),
        )
        feedMainAdapter = FeedMainAdapter(mainFeedItems)
        rvMain.adapter = feedMainAdapter

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
    }
}
