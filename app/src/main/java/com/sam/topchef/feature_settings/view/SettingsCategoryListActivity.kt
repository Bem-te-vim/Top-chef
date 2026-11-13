package com.sam.topchef.feature_settings.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.databinding.ActivitySettingsCategoryListBinding
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity
import com.sam.topchef.feature_settings.adapter.SettingsCategoryAdapter
import com.sam.topchef.feature_settings.model.SettingsCategoryUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsCategoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsCategoryListBinding
    private lateinit var adapter: SettingsCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = SettingsCategoryAdapter(
            onItemClick = { category ->
                val intent = Intent(this, SeeAllActivity::class.java).apply {
                    putExtra("show", SeeAllActivity.CATEGORY_FILTER)
                    putExtra(SeeAllActivity.EXTRA_CATEGORY_NAME, category.name)
                }
                startActivity(intent)
            },
            onDeleteClick = { category ->
                confirmDeleteCategory(category)
            }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun confirmDeleteCategory(category: SettingsCategoryUiModel) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_category_title))
            .setMessage(getString(R.string.delete_category_confirm, category.name))
            .setNegativeButton(R.string.back) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete_recipe) { dialog, _ ->
                dialog.dismiss()
                deleteCategory(category)
            }
            .show()
    }

    private fun deleteCategory(category: SettingsCategoryUiModel) {
        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            val typeDao = app.typeDao
            val allTypes = typeDao.getAllTypes()

            val matchingTypes = allTypes.filter {
                it.type.trim().equals(category.name.trim(), ignoreCase = true)
            }

            matchingTypes.forEach { type ->
                typeDao.delete(type)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@SettingsCategoryListActivity,
                    getString(R.string.category_deleted_success),
                    Toast.LENGTH_SHORT
                ).show()
                loadData()
            }
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            val typeDao = app.typeDao
            val recipeDao = app.recipeDao
            val tiktokDao = app.tiktokDao

            val typesInDb = typeDao.getAllTypes()
            val categoryNamesFromRecipes = recipeDao.getAllCategoryNames()
            val categoryNamesFromTiktoks = tiktokDao.getAllCategoryNames()

            val allCategoryNames = (typesInDb.map { it.type } +
                    categoryNamesFromRecipes +
                    categoryNamesFromTiktoks)
                .filter { it.isNotBlank() }
                .map { it.trim() }
                .distinctBy { it.lowercase(java.util.Locale.getDefault()) }

            val allRecipes = recipeDao.getAllRecipes()
            val allTiktoks = tiktokDao.getAll()

            val uiModels = allCategoryNames.map { catName ->
                val recipesCount = allRecipes.count {
                    it.type?.trim().equals(catName, ignoreCase = true)
                }
                val tiktoksCount = allTiktoks.count {
                    it.type?.trim().equals(catName, ignoreCase = true)
                }
                val totalCount = recipesCount + tiktoksCount

                val matchedType = typesInDb.firstOrNull {
                    it.type.trim().equals(catName, ignoreCase = true)
                }

                SettingsCategoryUiModel(
                    id = matchedType?.id,
                    name = matchedType?.type ?: catName,
                    recipeCount = totalCount,
                    canDelete = (totalCount == 0)
                )
            }.sortedBy { it.name.lowercase(java.util.Locale.getDefault()) }

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (uiModels.isEmpty()) {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                }
                adapter.submitList(uiModels)
            }
        }
    }
}
