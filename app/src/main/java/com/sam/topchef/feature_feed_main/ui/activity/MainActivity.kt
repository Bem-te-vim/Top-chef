package com.sam.topchef.feature_feed_main.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.databinding.ActivityMainBinding
import com.sam.topchef.feature_add_recipe.ui.activity.AddRecipeActivity
import com.sam.topchef.feature_feed_main.adapter.FeedMainAdapter
import com.sam.topchef.feature_feed_main.data.model.MainFeedItem
import com.sam.topchef.feature_feed_main.data.model.PopularRecipe
import com.sam.topchef.feature_feed_main.data.model.RecipeCategory
import com.sam.topchef.feature_recipe_detail.ui.activity.RecipeDetailActivity
import com.sam.topchef.feature_search.activities.SearchActivity
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity
import com.sam.topchef.feature_shopping_list.activities.ShoppingListActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var result: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityMainBinding
    private lateinit var feedMainAdapter: FeedMainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val rvMain = binding.rvMain
        rvMain.layoutManager = LinearLayoutManager(this)
        feedMainAdapter = FeedMainAdapter()
        rvMain.adapter = feedMainAdapter
        loadData()

        feedMainAdapter.onLikeClickListener = { id, isFavorite ->
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

                runOnUiThread {
                    loadData()
                }

            }.start()
            Log.i("msg", "id:$id -> favorite:$isFavorite")
        }

        feedMainAdapter.onItemClickListener = { id ->
            val i = Intent(this, RecipeDetailActivity::class.java)
            i.putExtra("id", id)
            this.startActivity(i)
        }

        feedMainAdapter.onWhatShowListener = { whatShow ->
            val i = Intent(this, SeeAllActivity::class.java)
            when (whatShow) {
                "AllPopularRecipes" -> i.putExtra("whatShow", "AllPopularRecipes")
                "AllCategories" -> i.putExtra("whatShow", "AllCategories")
            }
            startActivity(i)
        }

        feedMainAdapter.onLongClickListenerToDelete = { position, id ->

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Deletar essa receita⚠?")
                .setNegativeButton("Cancelar") { p0, _ -> p0.dismiss() }
                .setPositiveButton("Deletar") { _, _ ->
                    feedMainAdapter.removeItem(position)
                    thread {
                        val app = application as App
                        val dao = app.db.recipeDao()
                        dao.delete(id)
                        runOnUiThread {
                            Toast.makeText(this, "Receita deletada", Toast.LENGTH_LONG).show()
                        }
                    }
                }.show()

            true
        }

        binding.imageProfile.setOnClickListener {
            //todo: not implemented yet -> open Activity ProfileActivity
        }

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }

        binding.textInputSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.btnAddRecipe.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        //result  todo: switch loadData() from override onResume() to result Launcher if RESULT_OK
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        thread {
            val app = application as App
            val dao = app.db.recipeDao()
            val allRecipes = dao.getAllRecipes()


            val popularRecipes = allRecipes.take(10).map { recipe ->
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
            }

            val categories = allRecipes.take(10).map { recipe ->
                RecipeCategory(
                    recipe.type ?: "Default",
                    recipe.imageUriString.takeIf { it.isNotEmpty() }?.first()
                )
            }


            val mainPosts = allRecipes.map { recipe ->
                MainFeedItem.RecipePost(
                    recipe.id,
                    recipe.title,
                    recipe.imageUriString.takeIf { it.isNotEmpty() }?.first(),
                    recipe.isFavorite,
                    recipe.reviews
                )
            }

            /**
             * the order in which items are added affects the order of the feed
             */
            val items = mutableListOf<MainFeedItem>()
            items.add(MainFeedItem.PopularRecipes(popularRecipes))
            items.add(MainFeedItem.Categories(categories))
            items.addAll(mainPosts)

            runOnUiThread {
                validateList(allRecipes.isEmpty())
                feedMainAdapter.setItems(items)
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

    private fun showRecipesNotFoundError(
        show: Boolean = false,
        title: String = "Oops...",
        message: String = "Parece que algo deu errado",
        buttonText: String = "Volar"
    ) {
        val include = binding.includeRecipeListEmpty

        val errorLayoutRoot = include.errorLayoutRoot
        errorLayoutRoot.visibility = if (show) View.VISIBLE else View.GONE

        val errorTitle = include.errorTitle
        val errorMessage = include.errorMessage
        val btnGoTOHome = include.btnGoToHome

        errorTitle.text = title
        errorMessage.text = message
        btnGoTOHome.text = buttonText

        btnGoTOHome.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AddRecipeActivity::class.java
                )
            )
        }
    }


}