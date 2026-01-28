package com.sam.topchef.feature_profile.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.fature_import_recipe.activities.ImportRecipeActivity
import com.sam.topchef.fature_import_recipe.importer.TudoGostosoImporter
import com.sam.topchef.fature_import_recipe.model.WebRecipeModel
import com.sam.topchef.feature_profile.adaper.WebRecipeAdapter
import kotlinx.coroutines.launch


class WebRecipesFragment : Fragment() {

    private lateinit var webRecipeAdapter: WebRecipeAdapter
    private lateinit var progressBar: ProgressBar

    private val recipes = mutableListOf<WebRecipeModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_web_recipes, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progress_bar)

        val rvMain: RecyclerView = view.findViewById(R.id.rv_web_recipes)
        rvMain.layoutManager = LinearLayoutManager(requireContext())
        webRecipeAdapter = WebRecipeAdapter(recipes)
        rvMain.adapter = webRecipeAdapter

        webRecipeAdapter.onClick = { recipeLinkPath ->
            val i = Intent(requireContext(), ImportRecipeActivity::class.java)
            i.putExtra("urlPath", recipeLinkPath)
            startActivity(i)
        }



        lifecycleScope.launch {
            showProgress()
            val result = TudoGostosoImporter.getFeed()
            recipes.clear()
            recipes.addAll(result)
            webRecipeAdapter.notifyDataSetChanged()
            hideProgress()
        }
    }


    fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        progressBar.visibility = View.GONE
    }


}