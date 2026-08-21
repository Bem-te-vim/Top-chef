package com.sam.topchef.feature_search.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.databinding.ActivitySearchBinding
import com.sam.topchef.feature_feed_main.data.model.RecipePost
import com.sam.topchef.feature_import_from_tiktok.view.TiktokImportActivity
import com.sam.topchef.feature_recipe_detail.ui.activity.RecipeDetailActivity
import com.sam.topchef.feature_search.adapter.SearchAdapter
import kotlinx.coroutines.Runnable
import kotlin.concurrent.thread

/**
 * Activity providing global search functionality for recipes.
 * Allows users to find recipes by name or ingredients.
 */
class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchAdapter: SearchAdapter

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    /**
     * Initializes the search UI and sets up a debounced TextWatcher for real-time searching.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()


        val statusBarHeight = resources.getDimensionPixelSize(
            resources.getIdentifier("status_bar_height", "dimen", "android")
        )
        binding.statusBarOverlay.layoutParams.height = statusBarHeight

        val txtSearch = binding.textInputSearch
        txtSearch.requestFocus()
        txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}


            override fun afterTextChanged(p0: Editable?) {
                searchRunnable?.let { handler.removeCallbacks (it) }

                searchRunnable = Runnable {
                    val searchText = p0.toString().trim()

                    if (searchText.isNotEmpty()) {
                        search(searchText)
                    } else {
                        searchAdapter.submitList(emptyList())
                        binding.layoutNotFound.visibility = View.GONE
                    }
                }

                handler.postDelayed(searchRunnable!!, 300)
            }



        })


        val rvSearch = binding.rvSearch
        searchAdapter = SearchAdapter()
        searchAdapter.onItemClickListener = { id, isTikTok ->
            val i = if (isTikTok) {
                Intent(this, TiktokImportActivity::class.java).apply {
                    putExtra("tiktokId", id)
                }
            } else {
                Intent(this, RecipeDetailActivity::class.java).apply {
                    putExtra("id", id)
                }
            }
            this.startActivity(i)
        }
        rvSearch.layoutManager = LinearLayoutManager(this)
        rvSearch.adapter = searchAdapter
    }

    /**
     * Performs a database search for recipes matching the query string and updates the results adapter.
     * @param search The search query.
     */
    private fun search(search: String) {
        thread {
            val app = application as App
            val recipeResults = app.db.recipeDao().search(search).map {
                RecipePost(
                    id = it.id,
                    title = it.title,
                    coverUrl = it.imageUriString.firstOrNull(),
                    isFavorite = it.isFavorite,
                    reviews = it.reviews,
                    isTikTok = false
                )
            }
            val tiktokResults = app.db.tiktokDao().search(search).map {
                RecipePost(
                    id = it.id,
                    title = it.name,
                    coverUrl = it.thumbnail,
                    isFavorite = it.isFavorite,
                    reviews = 0.0,
                    isTikTok = true
                )
            }

            val combinedResults = recipeResults + tiktokResults

            runOnUiThread {
                if (combinedResults.isNotEmpty()) {
                    searchAdapter.submitList(combinedResults)
                    binding.layoutNotFound.visibility = View.GONE
                } else {
                    searchAdapter.submitList(emptyList())
                    binding.layoutNotFound.visibility = View.VISIBLE
                }
            }
        }
    }
}
