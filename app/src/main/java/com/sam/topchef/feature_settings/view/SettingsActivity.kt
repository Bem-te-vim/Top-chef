package com.sam.topchef.feature_settings.view

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.databinding.ActivitySettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupUi()
        setupListeners()
    }

    private fun setupUi() {
        lifecycleScope.launch {
            val userImage =
                withContext(Dispatchers.IO) { (application as App).userDao.getUser()?.imageUri }
            LoadImages().loadImagesWithBlur(userImage, binding.imgUser)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnMyRecipes.setOnClickListener {
            openRecipeList(
                filterType = SettingsRecipeListActivity.USER_RECIPES,
                title = getString(R.string.my_recipes)
            )
        }

        binding.btnFavoriteRecipes.setOnClickListener {
            openRecipeList(
                filterType = SettingsRecipeListActivity.FILTER_FAVORITES,
                title = getString(R.string.favorite_recipes)
            )
        }

        binding.btnTiktokRecipes.setOnClickListener {
            openRecipeList(
                filterType = SettingsRecipeListActivity.FILTER_TIKTOK,
                title = getString(R.string.tiktok_recipes)
            )
        }

        binding.btnTudoGostosoRecipes.setOnClickListener {
            openRecipeList(
                filterType = SettingsRecipeListActivity.FILTER_TUDO_GOSTOSO,
                title = getString(R.string.tudogostoso_recipes)
            )
        }

        binding.btnAllRecipes.setOnClickListener {
            openRecipeList(
                filterType = SettingsRecipeListActivity.FILTER_ALL,
                title = getString(R.string.all_recipes_option)
            )
        }
    }

    private fun openRecipeList(filterType: String, title: String) {
        val intent = Intent(this, SettingsRecipeListActivity::class.java).apply {
            putExtra(SettingsRecipeListActivity.EXTRA_FILTER_TYPE, filterType)
            putExtra(SettingsRecipeListActivity.EXTRA_SCREEN_TITLE, title)
        }
        startActivity(intent)
    }
}
