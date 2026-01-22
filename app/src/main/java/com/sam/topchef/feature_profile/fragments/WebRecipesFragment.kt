package com.sam.topchef.feature_profile.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.core.retrofit.model.WebRecipe
import com.sam.topchef.core.retrofit.model.WebRecipePage
import com.sam.topchef.core.retrofit.presentation.WebRecipePresenter
import com.sam.topchef.feature_profile.adaper.WebRecipeAdapter


class WebRecipesFragment : Fragment(), WebRecipePresenter.WebRecipeCallBack {

    private lateinit var webRecipePresenter: WebRecipePresenter
    private lateinit var webRecipeAdapter: WebRecipeAdapter
    private lateinit var progressBar: ProgressBar

    private val recipes = mutableListOf<WebRecipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webRecipePresenter = WebRecipePresenter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_web_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progress_bar)

        val rvMain: RecyclerView = view.findViewById(R.id.rv_web_recipes)
        rvMain.layoutManager = LinearLayoutManager(requireContext())
        webRecipeAdapter = WebRecipeAdapter(recipes)
        rvMain.adapter = webRecipeAdapter



        webRecipePresenter.getWebRecipePage()

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun showWebRecipePage(response: WebRecipePage) {
        recipes.clear()
        recipes.addAll(response.data)
        webRecipeAdapter.notifyDataSetChanged()
    }

    override fun showFailure(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }


}