package com.sam.topchef.feature_edit_recipe

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.core.utils.adapter.ImagesAdapter
import com.sam.topchef.core.utils.adapter.TextsAdapter
import com.sam.topchef.databinding.ActivityAddRecipeBinding
import com.sam.topchef.feature_add_recipe.adapter.RecipeDifficultAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddRecipeBinding
    private lateinit var imagesAdapter: ImagesAdapter
    private lateinit var difficultAdapter: RecipeDifficultAdapter

    private lateinit var ingredientsAdapter: TextsAdapter
    private lateinit var preparationAdapter: TextsAdapter

    private val imageUris = mutableListOf<String>()
    private val ingredients = mutableListOf<String>()
    private val preparations = mutableListOf<String>()


    private var currentRecipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val id = intent.extras?.getInt("id") ?: throw NullPointerException("Invalid id")


        ingredientsAdapter = TextsAdapter(ingredients, true)
        preparationAdapter = TextsAdapter(preparations, true)
        imagesAdapter = ImagesAdapter(imageUris)
        difficultAdapter = RecipeDifficultAdapter()


        ingredientsAdapter.onDeleteItemClickListener = { position ->
            ingredients.removeAt(position)
            ingredientsAdapter.notifyItemRemoved(position)
        }

        preparationAdapter.onDeleteItemClickListener = { position ->
            preparations.removeAt(position)
            preparationAdapter.notifyItemRemoved(position)
        }

        binding.btnSave.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Alerta")
                .setMessage("Deseja salvar as alterações?")
                .setPositiveButton("Sim") { p0, p1 ->

                }
                .setNegativeButton("Não") { p0, p1 ->
                    p0.dismiss()
                }
                .show()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

                AlertDialog.Builder(this@EditRecipeActivity)
                    .setTitle("Alerta")
                    .setMessage("Deseja continuar com a edição?")
                    .setPositiveButton("Sim") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setNegativeButton("Não") { dialog, _ ->
                        dialog.dismiss()
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                    .show()
            }

        })





        getRecipe(id) {
            currentRecipe = it
            setData(it)
        }
    }


    private fun getRecipe(id: Int, recipe: (Recipe) -> Unit) {
        lifecycleScope.launch {
            val recipe = withContext(Dispatchers.IO) {
                (application as App).recipeDao.getRecipe(id)
            }

            if (recipe != null) recipe(recipe)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData(recipe: Recipe) {
        LoadImages().loadImagesWithBlur(
            recipe.imageUriString.firstOrNull(), binding.imgCoverAddRecipe
        )
        val rvImg = binding.rvImagesAddRecipe
        rvImg.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageUris.clear()
        imageUris.addAll(recipe.imageUriString)
        imagesAdapter.notifyDataSetChanged()
        rvImg.adapter = imagesAdapter

        binding.autoCompleteType.setText(recipe.type)
        binding.edtxRecipeTitle.setText(recipe.title)
        binding.edtxRecipeDescription.setText(recipe.description)


        difficultAdapter.setDifficultyLevel(recipe.difficult)
        val rvDifficult = binding.rvRecipeDifficult
        val layoutManager = object : LinearLayoutManager(this, HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean {
                return false
            }
        }
        rvDifficult.layoutManager = layoutManager
        rvDifficult.adapter = difficultAdapter

        binding.edtxCokingTimeHour.setText(
            timeFormater(recipe.cookingTime).getValue("hour").toString()
        )
        binding.edtxCokingTimeMinute.setText(
            timeFormater(recipe.cookingTime).getValue("min").toString()
        )

        binding.edtxPreparationTimeHour.setText(
            timeFormater(recipe.preparationTime).getValue("hour").toString()
        )
        binding.edtxPreparationTimeMinute.setText(
            timeFormater(recipe.preparationTime).getValue("min").toString()
        )

        val rvIngredients = binding.rvIngredients
        rvIngredients.layoutManager = LinearLayoutManager(this)
        ingredients.clear()
        ingredients.addAll(recipe.ingredients)
        ingredientsAdapter.notifyDataSetChanged()
        rvIngredients.adapter = ingredientsAdapter


        val rvPreparation = binding.rvPreparation
        rvPreparation.layoutManager = LinearLayoutManager(this)
        preparations.clear()
        preparations.addAll(recipe.preparationMode)
        preparationAdapter.notifyDataSetChanged()
        rvPreparation.adapter = preparationAdapter

    }

    private fun updateRecipe(newRecipe: Recipe) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                (application as App).recipeDao.update(newRecipe)
            }
        }
    }

    private fun getNewRecipeData(): Recipe? {
        val title = binding.edtxRecipeTitle.text.toString().trim().ifEmpty {
            binding.edtxRecipeTitle.error = getString(R.string.required_field)

            val nestedScrollView = binding.mainNestedScrollView
            nestedScrollView.post {
                nestedScrollView.smoothScrollTo(0, binding.edtxRecipeTitle.top)
            }
            return null
        }

        val description =
            binding.edtxRecipeDescription.text.toString().trim().takeIf { it.isNotEmpty() }
        val difficult = difficultAdapter.getDifficultyLevel()
        val imageUriString = imageUris

        val cookingTimeHour = binding.edtxCokingTimeHour.text.toString().trim().toIntOrNull() ?: 0
        val cookingTimeMinute =
            binding.edtxCokingTimeMinute.text.toString().trim().toIntOrNull() ?: 0
        val cookingTime = sumHourMinutes(cookingTimeHour, cookingTimeMinute)


        val preparationTimeHour =
            binding.edtxPreparationTimeHour.text.toString().trim().toIntOrNull() ?: 0
        val preparationTimeMinute =
            binding.edtxPreparationTimeMinute.text.toString().trim().toIntOrNull() ?: 0
        val preparationTime = sumHourMinutes(preparationTimeHour, preparationTimeMinute)

        val recipe = Recipe(
            title = title,
            description = description,
            difficult = difficult,
            imageUriString = imageUriString,
            ingredients = ingredients,
            preparationMode = preparations,
            cookingTime = cookingTime,
            preparationTime = preparationTime

        )
        return recipe
    }

    private fun timeFormater(totalMinutes: Int): Map<String, Int> {
        val h = totalMinutes / 60
        val min = totalMinutes % 60

        return mapOf(
            "hour" to h, "min" to min
        )
    }

    private fun sumHourMinutes(hour: Int, minutes: Int): Int {
        val totalInMinutes = (hour * 60) + minutes
        return totalInMinutes
    }
}