package com.sam.topchef.feature_recipe_detail.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.utils.adapter.ImagesAdapter
import com.sam.topchef.core.utils.adapter.TextsAdapter
import com.sam.topchef.databinding.ActivityRecipeDetailBinding
import com.sam.topchef.feature_recipe_detail.adapter.StepsAdapter
import com.sam.topchef.feature_recipe_detail.model.Step
import kotlin.concurrent.thread

class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailBinding
    private var recipeCookingTimerInSeconds: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val statusBarHeight = resources.getDimensionPixelSize(
            resources.getIdentifier("status_bar_height", "dimen", "android")
        )
        binding.statusBarOverlay.layoutParams.height = statusBarHeight

        val i = intent
        val recipeId = i.extras?.getInt("id") as Int
        loadData(recipeId)


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


        binding.goTimer.setOnClickListener {

            recipeCookingTimerInSeconds?.let {

            }
        }
    }
    

    override fun onStop() {
        super.onStop()
        // Importante: Pare o timer para evitar vazamentos de memória se não estiver em um serviço em background
        // countDownTimer.cancel()
    }

    private fun loadData(recipeId: Int) {
        thread {
            val app = application as App
            val dao = app.db.recipeDao()
            val recipe = dao.getRecipe(recipeId)

            runOnUiThread {

                if (recipe == null) {
                    Toast.makeText(applicationContext, "Receita não encontrada", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                    return@runOnUiThread
                }

                val imgUriList = recipe.imageUriString
                val title = recipe.title
                val reviews = recipe.reviews
                val type = recipe.type ?: "Tipo não informado."
                val description = recipe.description ?: "Adicione uma descricao quando quiser."
                val difficult = recipe.difficult
                val ingredients = recipe.ingredients
                val cookingTime = recipe.cookingTime

                val preparationMode = recipe.preparationMode
                val preparationTime = recipe.preparationTime


                recipeCookingTimerInSeconds = cookingTime * 60


                fun setImage(load: String?, img: ShapeableImageView) {
                    Glide.with(this)
                        .load(load)
                        .placeholder(R.drawable.placeholder_item)
                        .into(img)
                }


                val imgCover = binding.coverImageRecipe
                setImage(imgUriList.firstOrNull(), imgCover)

                binding.txtRecipeType.text = type
                binding.txtRecipeTitle.text = title
                binding.txtRecipeDescription.text = description
                binding.btnEvaluateRecipe.text = reviews.toString()
                binding.txtDifficult.text = difficultFormater(difficult)
                binding.txtRecipeCookingTime.text = timeFormater(cookingTime)
                binding.txtRecipePreparationTime.text = timeFormater(preparationTime)


                binding.rvImageFromDetail.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                val imagesAdapter = ImagesAdapter(imgUriList)
                imagesAdapter.onImgClickListener = { imageSrc ->
                    setImage(imageSrc, imgCover)
                }
                binding.rvImageFromDetail.adapter = imagesAdapter


                binding.rvIngredients.layoutManager = LinearLayoutManager(this)
                val rvIngredient = binding.rvIngredients
                rvIngredient.layoutManager = LinearLayoutManager(this)
                rvIngredient.adapter = TextsAdapter(ingredients)


                binding.rvIngredients.layoutManager = LinearLayoutManager(this)
                val rvSteps = binding.rvSteps
                val steps = preparationMode.map { Step(it) }
                rvSteps.layoutManager = LinearLayoutManager(this)
                rvSteps.adapter = StepsAdapter(steps)

            }

        }
    }


    private fun difficultFormater(difficult: Int): String {

        return when (difficult) {
            1 -> getString(R.string.very_easy)
            2 -> getString(R.string.easy)
            3 -> getString(R.string.average)
            4 -> getString(R.string.hard)
            5 -> getString(R.string.very_hard)
            else -> "empty"
        }
    }

    @SuppressLint("DefaultLocale")
    private fun timeFormater(totalMinutes: Int): String {
        val h = totalMinutes / 60
        val min = totalMinutes % 60

        return String.format("%dh:%02dmin", h, min)
    }

}