package com.sam.topchef.feature_settings.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.databinding.ActivitySettingsRecipeListBinding
import com.sam.topchef.feature_import_from_tiktok.view.TiktokImportActivity
import com.sam.topchef.feature_recipe_detail.ui.activity.RecipeDetailActivity
import com.sam.topchef.feature_settings.adapter.SettingsRecipeAdapter
import com.sam.topchef.feature_settings.model.SettingsRecipeUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsRecipeListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsRecipeListBinding
    private lateinit var adapter: SettingsRecipeAdapter

    private var filterType: String = FILTER_ALL
    private var screenTitle: String = ""

    companion object {
        const val EXTRA_FILTER_TYPE = "extra_filter_type"
        const val EXTRA_SCREEN_TITLE = "extra_screen_title"

        const val FILTER_FAVORITES = "favorites"
        const val FILTER_TIKTOK = "tiktok"
        const val FILTER_TUDO_GOSTOSO = "tudogostoso"
        const val FILTER_ALL = "all"

        const val USER_RECIPES = "user_recipes"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsRecipeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        filterType = intent.getStringExtra(EXTRA_FILTER_TYPE) ?: FILTER_ALL
        screenTitle = intent.getStringExtra(EXTRA_SCREEN_TITLE) ?: getString(R.string.all_recipes)
        binding.tvTitle.text = screenTitle

        setupRecyclerView()
        setupListeners()
        setupOnBackPressed()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = SettingsRecipeAdapter(
            onItemClick = { item ->
                openRecipeDetail(item)
            },
            onItemLongClick = {
                updateSelectionHeader()
            },
            onSelectionChanged = {
                updateSelectionHeader()
            }
        )

        binding.rvRecipes.layoutManager = LinearLayoutManager(this)
        binding.rvRecipes.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            if (adapter.isSelectionMode) {
                exitSelectionMode()
            } else {
                finish()
            }
        }

        binding.btnDeleteSelected.setOnClickListener {
            confirmDeleteSelected()
        }
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (adapter.isSelectionMode) {
                    exitSelectionMode()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun exitSelectionMode() {
        adapter.isSelectionMode = false
        updateSelectionHeader()
    }

    private fun updateSelectionHeader() {
        if (adapter.isSelectionMode) {
            val count = adapter.getSelectedItems().size
            binding.tvTitle.text = getString(R.string.items_selected, count)
            binding.btnDeleteSelected.visibility = View.VISIBLE
            binding.btnBack.setImageResource(R.drawable.outline_close_small_24)
        } else {
            binding.tvTitle.text = screenTitle
            binding.btnDeleteSelected.visibility = View.GONE
            binding.btnBack.setImageResource(R.drawable.round_arrow_back_24)
        }
    }

    private fun openRecipeDetail(item: SettingsRecipeUiModel) {
        if (item.isTikTok) {
            val intent = Intent(this, TiktokImportActivity::class.java).apply {
                putExtra("tiktokId", item.id)
            }
            startActivity(intent)
        } else {
            val intent = Intent(this, RecipeDetailActivity::class.java).apply {
                putExtra("id", item.id)
            }
            startActivity(intent)
        }
    }

    private fun confirmDeleteSelected() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_recipes_selected), Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_recipes_title))
            .setMessage(getString(R.string.delete_recipes_confirm, selected.size))
            .setNegativeButton(R.string.back) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete_recipe) { dialog, _ ->
                dialog.dismiss()
                deleteSelectedRecipes(selected)
            }
            .show()
    }

    private fun deleteSelectedRecipes(selectedList: List<SettingsRecipeUiModel>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            selectedList.forEach { item ->
                if (item.isTikTok) {
                    app.tiktokDao.delete(item.id)
                } else {
                    app.recipeDao.delete(item.id)
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@SettingsRecipeListActivity,
                    getString(R.string.recipes_deleted_success),
                    Toast.LENGTH_SHORT
                ).show()

                adapter.isSelectionMode = false
                updateSelectionHeader()
                loadData()
            }
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            val list = mutableListOf<SettingsRecipeUiModel>()

            val userName = app.userDao.getUser()?.name

            when (filterType) {
                FILTER_FAVORITES -> {
                    val favAppRecipes = app.recipeDao.getAllFavorites().map { r ->
                        SettingsRecipeUiModel(
                            id = r.id,
                            title = r.title,
                            subtitle = "${r.chef ?: "App"} • ${r.type ?: "Receita"}",
                            imageUrl = r.imageUriString.firstOrNull(),
                            isTikTok = false,
                            isFavorite = true
                        )
                    }

                    val favTikTokRecipes = app.tiktokDao.getAllFavorites().map { t ->
                        SettingsRecipeUiModel(
                            id = t.id,
                            title = t.name,
                            subtitle = "${t.authorName ?: "TikTok"} • ${t.type ?: "Vídeo"}",
                            imageUrl = t.thumbnail,
                            isTikTok = true,
                            isFavorite = true
                        )
                    }

                    list.addAll(favAppRecipes)
                    list.addAll(favTikTokRecipes)
                }

                FILTER_TIKTOK -> {
                    val tiktokRecipes = app.tiktokDao.getAll().map { t ->
                        SettingsRecipeUiModel(
                            id = t.id,
                            title = t.name,
                            subtitle = "${t.authorName ?: "TikTok"} • ${t.type ?: "Vídeo"}",
                            imageUrl = t.thumbnail,
                            isTikTok = true,
                            isFavorite = t.isFavorite
                        )
                    }

                    list.addAll(tiktokRecipes)
                }

                FILTER_TUDO_GOSTOSO -> {
                    val tudogostosoRecipes = app.recipeDao.getAllRecipes().filter { r ->
                        r.chef.equals("Web", ignoreCase = true) || r.chef?.contains(
                            "TudoGostoso",
                            ignoreCase = true
                        ) == true
                    }.map { r ->
                        SettingsRecipeUiModel(
                            id = r.id,
                            title = r.title,
                            subtitle = "TudoGostoso • ${r.type ?: "Receita"}",
                            imageUrl = r.imageUriString.firstOrNull(),
                            isTikTok = false,
                            isFavorite = r.isFavorite
                        )
                    }

                    list.addAll(tudogostosoRecipes)
                }

                FILTER_ALL -> {
                    val appRecipes = app.recipeDao.getAllRecipes().map { r ->
                        val origin =
                            if (r.chef.equals("Web", ignoreCase = true)) "TudoGostoso" else (r.chef
                                ?: "Sua Receita")
                        SettingsRecipeUiModel(
                            id = r.id,
                            title = r.title,
                            subtitle = "$origin • ${r.type ?: "Receita"}",
                            imageUrl = r.imageUriString.firstOrNull(),
                            isTikTok = false,
                            isFavorite = r.isFavorite
                        )
                    }

                    val tiktokRecipes = app.tiktokDao.getAll().map { t ->
                        SettingsRecipeUiModel(
                            id = t.id,
                            title = t.name,
                            subtitle = "${t.authorName ?: "TikTok"} • ${t.type ?: "Vídeo"}",
                            imageUrl = t.thumbnail,
                            isTikTok = true,
                            isFavorite = t.isFavorite
                        )
                    }

                    list.addAll(appRecipes)
                    list.addAll(tiktokRecipes)
                }

                USER_RECIPES -> {
                    val userRecipes = app.recipeDao.getAllRecipes().filter { r ->
                        r.chef == null
                    }.map { r ->

                        SettingsRecipeUiModel(
                            id = r.id,
                            title = r.title,
                            subtitle = "${userName ?: "Sua receita" } • ${r.type ?: "Receita"}",
                            imageUrl = r.imageUriString.firstOrNull(),
                            isTikTok = false,
                            isFavorite = r.isFavorite
                        )
                    }

                    list.addAll(userRecipes)
                }
            }

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (list.isEmpty()) {
                    val emptyMessageRes = when (filterType) {
                        USER_RECIPES -> R.string.empty_user_recipes
                        FILTER_FAVORITES -> R.string.empty_favorites
                        FILTER_TIKTOK -> R.string.empty_tiktok_recipes
                        FILTER_TUDO_GOSTOSO -> R.string.empty_tudogostoso_recipes
                        else -> R.string.empty_all_recipes
                    }
                    binding.tvEmptyMessage.setText(emptyMessageRes)
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    Toast.makeText(
                        this@SettingsRecipeListActivity,
                        R.string.empty_list_warning,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                }
                adapter.submitList(list)
            }
        }
    }
}
