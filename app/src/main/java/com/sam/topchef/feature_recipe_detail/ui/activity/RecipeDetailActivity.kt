package com.sam.topchef.feature_recipe_detail.ui.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.databinding.ActivityRecipeDetailBinding
import com.sam.topchef.core.utils.adapter.ImagesAdapter
import kotlin.concurrent.thread

class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val statusBarHeight = resources.getDimensionPixelSize(
            resources.getIdentifier("status_bar_height", "dimen", "android")
        )
        binding.statusBarOverlay.layoutParams.height = statusBarHeight


        val i = intent
        val recipeId = i.extras?.getInt("id") as Int

        thread {
            val app = application as App
            val dao = app.db.recipeDao()
            val recipe = dao.getRecipe(recipeId)

            runOnUiThread {
                // Agora currentRecipe tem dados
                val imgUriList = recipe.imageUriString
                val title = recipe.title
                val reviews = recipe.reviews
                val type = recipe.type ?: "Tipo não informado."
                val description =
                    recipe.description ?: "Você pode adicionar uma descricao quando quiser :)"
                val difficult = recipe.difficult
                val ingredients = recipe.ingredients
                val preparationMode = recipe.preparationMode
                val preparationTime = recipe.preparationTime
                val cookingTime = recipe.cookingTime


                // Cover image
                Glide.with(this)
                    .load(imgUriList.firstOrNull())
                    .placeholder(R.drawable.placeholder_item)
                    .into(binding.coverImageRecipe)


                binding.txtRecipeType.text = type
                binding.txtRecipeTitle.text = title
                binding.txtRecipeDescription.text = description
                binding.btnEvaluateRecipe.text = reviews.toString()


/**                val totalTimeInMinutes = preparationTime + cookingTime
                binding.txtRecipeCookingTime.text = minuterFormater(totalTimeInMinutes)
**/

                binding.rvImageFromDetail.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.rvImageFromDetail.adapter = ImagesAdapter(imgUriList)


                // RecyclerView ingredientes (aqui você precisa criar um adapter igual ao ImagesAdapter)
                binding.rvIngredients.layoutManager = LinearLayoutManager(this)
                //todo
                // binding.rvIngredients.adapter = IngredientsAdapter(ingredients ?: emptyList())
            }
        }


        val items = listOf("1 Service", "2 Services", "3 Services")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        val autoCompleteService = binding.autoCompleteService
        autoCompleteService.apply {
            setText(items[0])
            setAdapter(adapter)
            setDropDownBackgroundDrawable(
                ContextCompat.getDrawable(
                    this@RecipeDetailActivity,
                    com.sam.topchef.R.drawable.bg_dropdown_dark
                )
            )
        }
        binding.btnBack.setOnClickListener { finish() }
    }


}