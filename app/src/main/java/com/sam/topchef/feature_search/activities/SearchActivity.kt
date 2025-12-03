package com.sam.topchef.feature_search.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.databinding.ActivitySearchBinding
import com.sam.topchef.feature_recipe_detail.ui.activity.RecipeDetailActivity
import com.sam.topchef.feature_search.adapter.SearchAdapter
import kotlinx.coroutines.Runnable
import kotlin.concurrent.thread

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchAdapter: SearchAdapter

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

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
                    }
                }

                handler.postDelayed(searchRunnable!!, 300)
            }



        })


        val rvSearch = binding.rvSearch
        searchAdapter = SearchAdapter()
        searchAdapter.onItemClickListener = {id ->
            val i = Intent(this, RecipeDetailActivity::class.java)
            i.putExtra("id", id)
            this.startActivity(i)
        }
        rvSearch.layoutManager = LinearLayoutManager(this)
        rvSearch.adapter = searchAdapter
    }

    private fun search(search: String) {
        thread {
            val app = application as App
            val dao = app.db.recipeDao()
            val results = dao.search(search)

            runOnUiThread {
              if (results.isNotEmpty()){
                  searchAdapter.submitList(results)
              }else{
                  searchAdapter.submitList(emptyList())
                  Toast.makeText(applicationContext, "Not Results Found", Toast.LENGTH_SHORT).show()
              }
            }
        }
    }
}