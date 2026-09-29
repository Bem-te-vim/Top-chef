package com.sam.topchef.feature_profile.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sam.topchef.databinding.FragmentWebRecipesBinding
import com.sam.topchef.feature_import_from_tudogostoso.activities.TudoGostosoImportActivity
import com.sam.topchef.feature_import_from_tudogostoso.importer.TudoGostosoImporter
import com.sam.topchef.feature_import_from_tudogostoso.model.WebRecipeModel
import com.sam.topchef.feature_profile.adaper.WebRecipeAdapter
import kotlinx.coroutines.launch


class WebRecipesFragment : Fragment() {
    private var _binding: FragmentWebRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var webRecipeAdapter: WebRecipeAdapter

    //Views
    private lateinit var progressBar: ProgressBar
    private lateinit var searchWebRecipe: EditText

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val recipes = mutableListOf<WebRecipeModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebRecipesBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerView()
        setupListeners()
        loadFeed()
    }

    private fun setupViews() {
        progressBar = binding.progressBar
        searchWebRecipe = binding.textInputSearch
        swipeRefreshLayout = binding.swipeRefreshLayout
    }

    private fun setupRecyclerView() {
        val rvMain = binding.rvWebRecipes
        rvMain.layoutManager = LinearLayoutManager(requireContext())
        webRecipeAdapter = WebRecipeAdapter(recipes)
        rvMain.adapter = webRecipeAdapter
    }

    private fun setupListeners() {
        webRecipeAdapter.onClick = { recipeLinkPath ->
            val i = Intent(requireContext(), TudoGostosoImportActivity::class.java)
            i.putExtra("urlPath", recipeLinkPath)
            startActivity(i)
        }

        webRecipeAdapter.onLongClick = { recipe ->
            com.sam.topchef.core.utils.RecipeToolsDialog.show(requireContext(), recipe)
        }

        searchWebRecipe.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val textSearch = view.text.toString()
                searchWebRecipe(textSearch)

                val inputMethodManager =
                    ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)

                true
            } else {
                false
            }
        }

        swipeRefreshLayout.setColorSchemeResources(com.sam.topchef.R.color.default_color_app)
        swipeRefreshLayout.setOnRefreshListener {
            loadFeed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchWebRecipe(search: String) {
        lifecycleScope.launch {
            if (!swipeRefreshLayout.isRefreshing){
                showProgress()
            }

            val result = TudoGostosoImporter.searchRecipe(search)

            if (result.isNotEmpty()) {
                recipes.clear()
                recipes.addAll(result)
                webRecipeAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(requireContext(), "Nenhuma receita encontrada", Toast.LENGTH_SHORT)
                    .show()
            }
            hideProgress()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun loadFeed() {
        lifecycleScope.launch {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                showProgress()
            }
            hideErrorMessage()
            val result = TudoGostosoImporter.getFeed()
            recipes.clear()
            recipes.addAll(result)
            webRecipeAdapter.notifyDataSetChanged()

            if (result.isEmpty()) {
                showErrorMessage()
            }
            hideProgress()
            binding.swipeRefreshLayout.isRefreshing = false
            searchWebRecipe.text.clear()
            searchWebRecipe.clearFocus()
        }
    }

    private fun showErrorMessage() {
        binding.layoutNotFound.visibility = View.VISIBLE
    }

    private fun hideErrorMessage() {
        binding.layoutNotFound.visibility = View.GONE
    }


    private fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }


}