package com.sam.topchef.feature_profile.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.feature_feed_main.data.model.RecipePost
import com.sam.topchef.feature_import_from_tiktok.view.TiktokImportActivity
import com.sam.topchef.feature_profile.adaper.AllForProfileAdapter
import com.sam.topchef.feature_recipe_detail.ui.activity.RecipeDetailActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FavoriteRecipeFragment : Fragment() {
    private lateinit var allForProfileAdapter: AllForProfileAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvAllRecipes = view.findViewById<RecyclerView>(R.id.rv_favorites_recipes)
        rvAllRecipes.layoutManager = GridLayoutManager(requireContext(), 3)
        allForProfileAdapter = AllForProfileAdapter()//You can inflate a row layout (but check out the class)
        rvAllRecipes.adapter = allForProfileAdapter

        lifecycleScope.launch {

            val recipes = withContext(Dispatchers.IO) {
                val app = requireContext().applicationContext as App
                val normalFavorites = app.db.recipeDao().getAllFavorites().map {
                    RecipePost(
                        id = it.id,
                        title = it.title,
                        coverUrl = it.imageUriString.firstOrNull(),
                        isFavorite = it.isFavorite,
                        reviews = it.reviews,
                        isTikTok = false
                    )
                }
                val tiktokFavorites = app.db.tiktokDao().getAllFavorites().map {
                    RecipePost(
                        id = it.id,
                        title = it.name,
                        coverUrl = it.thumbnail,
                        isFavorite = it.isFavorite,
                        reviews = 0.0,
                        isTikTok = true
                    )
                }
                (normalFavorites + tiktokFavorites).shuffled()
            }


            allForProfileAdapter.submitList(recipes)
        }

        allForProfileAdapter.itemClick = { id, isTikTok ->
            val i = if (isTikTok) {
                Intent(requireContext(), TiktokImportActivity::class.java).apply {
                    putExtra("tiktokId", id)
                }
            } else {
                Intent(requireContext(), RecipeDetailActivity::class.java).apply {
                    putExtra("id", id)
                }
            }
            startActivity(i)
        }

        allForProfileAdapter.itemLongClick = { id, isTikTok ->
            //TODO: tools
        }
    }

}
