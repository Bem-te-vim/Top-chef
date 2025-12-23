package com.sam.topchef.feature_feed_main.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.databinding.ActivityMainBinding
import com.sam.topchef.feature_add_recipe.ui.activity.AddRecipeActivity
import com.sam.topchef.feature_edit_recipe.EditRecipeActivity
import com.sam.topchef.feature_feed_main.adapter.CategoryRecipeAdapter
import com.sam.topchef.feature_feed_main.adapter.PopularRecipesAdapter
import com.sam.topchef.feature_feed_main.adapter.RecipePostAdapter
import com.sam.topchef.feature_feed_main.adapter_interface.AdapterChanges
import com.sam.topchef.feature_feed_main.data.model.PopularRecipe
import com.sam.topchef.feature_feed_main.data.model.RecipeCategory
import com.sam.topchef.feature_feed_main.data.model.RecipePost
import com.sam.topchef.feature_profile.activities.ProfileActivity
import com.sam.topchef.feature_recipe_detail.ui.activity.RecipeDetailActivity
import com.sam.topchef.feature_search.activities.SearchActivity
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity.Companion.ALL_CATEGORIES
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity.Companion.ALL_POPULAR_RECIPES
import com.sam.topchef.feature_shopping_list.activities.ShoppingListActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), AdapterChanges {
    private lateinit var result: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityMainBinding
    private lateinit var popularRecipesAdapter: PopularRecipesAdapter
    private lateinit var categoryRecipeAdapter: CategoryRecipeAdapter
    private lateinit var recipePostAdapter: RecipePostAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        result =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    loadData()
                }
            }


        // splash to next activity for show all popular recipes
        val includePopularRecipe = binding.includePopularRecipes
        includePopularRecipe.btnSeeAllPopularRecipes.setOnClickListener {
            val i = Intent(this, SeeAllActivity::class.java)
            i.putExtra("show", ALL_POPULAR_RECIPES)
            startActivity(i)
        }


        val rvPopularRecipe = includePopularRecipe.rvPopularRecipes
        rvPopularRecipe.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val pagerSnapHelper = LinearSnapHelper()
        pagerSnapHelper.attachToRecyclerView(rvPopularRecipe)

        popularRecipesAdapter = PopularRecipesAdapter(this)
        rvPopularRecipe.adapter = popularRecipesAdapter


        // splash to next activity for show all categories
        val includeCategory = binding.includeCategories
        includeCategory.btnSeeAllCategories.setOnClickListener {
            val i = Intent(this, SeeAllActivity::class.java)
            i.putExtra("show", ALL_CATEGORIES)
            startActivity(i)
        }

        val rvCategories = includeCategory.rvCategories
        rvCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryRecipeAdapter = CategoryRecipeAdapter()
        rvCategories.adapter = categoryRecipeAdapter


        val rvPostItem = binding.includeMain.rvPostItem
        rvPostItem.layoutManager = LinearLayoutManager(this)
        recipePostAdapter = RecipePostAdapter(this)
        rvPostItem.adapter = recipePostAdapter



        binding.imageProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }

        binding.textInputSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.btnAddRecipe.setOnClickListener {
            val i = Intent(this, AddRecipeActivity::class.java)
            result.launch(i)
        }

        loadData()

    }


    private fun startActivityEditor(id: Int) {
        val i = Intent(this, EditRecipeActivity::class.java)
        i.putExtra("recipe_for_editing_id", id)
        startActivity(i)
    }

    private fun moveIngredientsToCart(id: Int) {
        val recipe = getRecipe(id)
        val ingredientsList = recipe?.ingredients

        val i = Intent(this, ShoppingListActivity::class.java)
        i.putExtra("ingredients", ingredientsList?.toTypedArray())
        startActivity(i)
    }

    private fun getRecipe(id: Int): Recipe? {
        var recipe: Recipe? = null
        thread {
            val app = application as App
            val dao = app.db.recipeDao()
            val recipeGet = dao.getRecipe(id)

            if (recipeGet == null) return@thread

            runOnUiThread {
                recipe = recipeGet
            }
        }
        return recipe
    }

    private fun showDeleteRecipeDialog(id: Int) {
        AlertDialog.Builder(this)
            // todo: create custom view to this AlertDialog
            .setTitle("Deletar essa recaita?")

            .setNegativeButton("Cancelar") { p0, p1 -> p0.dismiss() }

            .setPositiveButton("Deletar") { p0, _ ->
                deleteRecipe(id)
                p0.dismiss()
            }
            .show()
    }

    private fun deleteRecipe(id: Int) {
        thread {
            val app = application as App
            val dao = app.db.recipeDao()
            dao.delete(id)
            runOnUiThread {
                popularRecipesAdapter.onDeleteNotify(id)
                categoryRecipeAdapter.onDeleteNotify(id)
                recipePostAdapter.onDeleteNotify(id)

                Toast.makeText(this, "Receita deletada", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showRecipesNotFoundError(
        show: Boolean = false,
        title: String = "Oops...",
        message: String = "Parece que algo deu errado",
        buttonText: String = "Volar"
    ) {
        val include = binding.includeRecipeListEmpty

        val errorLayoutRoot = include.errorLayoutRoot
        errorLayoutRoot.visibility = if (show) View.VISIBLE else View.GONE
        binding.includePopularRecipes.root.visibility = if (!show) View.VISIBLE else View.GONE
        binding.includeCategories.root.visibility = if (!show) View.VISIBLE else View.GONE


        val errorTitle = include.errorTitle
        val errorMessage = include.errorMessage
        val btnGoTOHome = include.btnGoToHome

        errorTitle.text = title
        errorMessage.text = message
        btnGoTOHome.text = buttonText

        btnGoTOHome.setOnClickListener {
            val i = Intent(this, AddRecipeActivity::class.java)
            result.launch(i)
        }
    }

    private fun saveLikeUpdate(id: Int, isFavorite: Boolean) {
        Thread {
            val app = application as App
            val recipeDao = app.db.recipeDao()
            val recipe = recipeDao.getRecipe(id)

            if (recipe == null) {
                runOnUiThread {

                    Toast.makeText(
                        applicationContext,
                        "Essa receita foi excluida recentemente, ela desapareceara em breve.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                return@Thread
            }

            recipeDao.update(recipe.copy(isFavorite = isFavorite))
        }.start()
    }

    override fun onRecipeLiked(id: Int, isFavorite: Boolean) {
        saveLikeUpdate(id, isFavorite)
        popularRecipesAdapter.onLikeNotify(id, isFavorite)
        recipePostAdapter.onLikeNotify(id, isFavorite)
    }

    override fun onRecipeReview(id: Int, review: Double) {
        TODO("Not yet implemented")
    }

    override fun onRecipeClicked(id: Int) {
        val i = Intent(this, RecipeDetailActivity::class.java)
        i.putExtra("id", id)
        startActivity(i)
    }

    @SuppressLint("InflateParams")
    private fun showBottomSheetsDialog(id: Int) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_tools_bottom_sheet, null)
        dialog.setContentView(view)

        dialog.show()

        val delete: LinearLayout = view.findViewById(R.id.tools_delete)
        val edit: LinearLayout = view.findViewById(R.id.tools_edit)
        val share: LinearLayout = view.findViewById(R.id.tools_share)
        val moveToCart: LinearLayout = view.findViewById(R.id.tools_move_to_cart)

        delete.setOnClickListener {
            showDeleteRecipeDialog(id)
            dialog.dismiss()
        }

        edit.setOnClickListener {
            startActivityEditor(id)
            dialog.dismiss()
        }

        share.setOnClickListener {

            dialog.dismiss()
        }

        moveToCart.setOnClickListener {
            moveIngredientsToCart(id)
            dialog.dismiss()
        }


        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
    }

    override fun onRecipeTools(id: Int) {
        showBottomSheetsDialog(id)
    }

    private fun loadData() {
        thread {
            val app = application as App
            val dao = app.db.recipeDao()
            val allRecipes = dao.getAllRecipes()


            val popularRecipes = allRecipes.shuffled().take(10).map { recipe ->
                PopularRecipe(
                    recipe.id,
                    recipe.chef ?: "Default",
                    recipe.difficult,
                    recipe.preparationTime,
                    recipe.title,
                    recipe.imageUriString.takeIf { it.isNotEmpty() }?.first(),
                    recipe.reviews,
                    recipe.isFavorite
                )
            }.sortedBy { it.reviews }

            val categories = allRecipes.take(10).map { recipe ->
                RecipeCategory(
                    recipe.id,
                    recipe.type ?: "Default",
                    recipe.imageUriString.takeIf { it.isNotEmpty() }?.first()
                )
            }


            val mainPosts = allRecipes.map { recipe ->
                RecipePost(
                    recipe.id,
                    recipe.title,
                    recipe.imageUriString.takeIf { it.isNotEmpty() }?.first(),
                    recipe.isFavorite,
                    recipe.reviews
                )
            }



            runOnUiThread {
                validateList(allRecipes.isEmpty())
                popularRecipesAdapter.setItems(popularRecipes)
                categoryRecipeAdapter.setItems(categories)
                recipePostAdapter.setItems(mainPosts)
            }
        }
    }

    private fun validateList(isEmpty: Boolean) {
        if (isEmpty) {
            showRecipesNotFoundError(
                show = true,
                title = "Olá!",
                message = "Parece que você ainda não tem nem uma receita salva.",
                buttonText = "Criar Receita"
            )
        } else {
            showRecipesNotFoundError(false)
        }
    }
}