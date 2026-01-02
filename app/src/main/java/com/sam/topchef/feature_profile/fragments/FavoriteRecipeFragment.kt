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
                (requireContext().applicationContext as App)
                    .recipeDao
                    .getAllRecipes().filter { it.isFavorite }
            }


            allForProfileAdapter.submitList(recipes)
        }

        allForProfileAdapter.itemClick = { id ->
            val i = Intent(requireContext(), RecipeDetailActivity::class.java)
            i.putExtra("id", id)
            startActivity(i)
        }

        allForProfileAdapter.itemLongClick = { id ->
            //TODO: tools
        }
    }

}
