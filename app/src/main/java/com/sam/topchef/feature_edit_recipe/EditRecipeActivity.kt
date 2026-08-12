package com.sam.topchef.feature_edit_recipe

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.core.utils.adapter.ImagesAdapter
import com.sam.topchef.core.utils.adapter.TextsAdapter
import com.sam.topchef.databinding.ActivityAddRecipeBinding
import com.sam.topchef.feature_add_recipe.adapter.RecipeDifficultAdapter
import com.sam.topchef.feature_feed_main.ui.activity.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity responsible for editing existing recipes.
 * Pre-fills fields with existing recipe data and allows updates to all recipe components.
 */
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

    @SuppressLint("NotifyDataSetChanged")
    private val pickImages =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                if (uris.size <= 5) {
                    Glide.with(this).load(uris.first()).into(binding.imgCoverAddRecipe)
                    imageUris.clear()
                    imageUris.addAll(uris.map { it.toString() })
                    imagesAdapter.notifyDataSetChanged()


                    uris.forEach {
                        contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                } else {
                    Toast.makeText(this, "Selecione no máximo 5 imagens", Toast.LENGTH_SHORT).show()
                }
            }
        }

    /**
     * Initializes the activity, loads existing recipe data, and sets up UI listeners for editing.
     */
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

        imagesAdapter.onImgClickListener = { img ->
            Glide.with(this)
                .load(img)
                .placeholder(R.drawable.placeholder_item)
                .into(binding.imgCoverAddRecipe)
        }

        imagesAdapter.onImgLongClickListener = { position ->
            AlertDialog.Builder(this)
                .setTitle("Remover imagem?")
                .setNegativeButton("Cancelar") { p0, _ -> p0.dismiss() }
                .setPositiveButton("Remover") { _, _ ->
                    imageUris.removeAt(position)
                    imagesAdapter.notifyItemRemoved(position)

                    Glide.with(this)
                        .load(imageUris.firstOrNull())
                        .placeholder(R.drawable.placeholder_item)
                        .into(binding.imgCoverAddRecipe)

                }.show()
            true
        }

        binding.btnAddImages.setOnClickListener {
            pickImages.launch("image/*")
        }

        binding.addIngredient.setOnClickListener {
            val ingredient = binding.edtxIngredient.text.toString().trim()
            if (ingredient.isNotEmpty()) {
                ingredients.add(ingredient)
                ingredientsAdapter.notifyItemInserted(ingredients.size - 1)
                binding.edtxIngredient.text.clear()
            }
        }

        binding.addStep.setOnClickListener {
            val step = binding.edtxStep.text.toString().trim()
            if (step.isNotEmpty()) {
                preparations.add(step)
                preparationAdapter.notifyItemInserted(preparations.size - 1)
                binding.edtxStep.text.clear()
            }
        }

        binding.btnSave.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Alerta")
                .setMessage("Deseja salvar as alterações?")
                .setPositiveButton("Sim") { _, _ ->
                    val updatedRecipe = getNewRecipeData()
                    if (updatedRecipe != null) {
                        updateRecipe(updatedRecipe)
                    }
                }
                .setNegativeButton("Não") { p0, _ ->
                    p0.dismiss()
                }
                .show()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
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

        setViewCount(binding.edtxRecipeTitle, binding.characterCountTitle, 45)
        setViewCount(binding.edtxRecipeDescription, binding.characterCountDescription, 300)

        setupTypeSpinner()
        getRecipe(id) {
            currentRecipe = it
            setData(it)
        }
    }

    /**
     * Sets up a character counter for an EditText.
     * @param editText The EditText to monitor.
     * @param textView The TextView to display the count.
     * @param maxValueCont The character limit.
     */
    private fun setViewCount(editText: EditText, textView: TextView, maxValueCont: Int) {
        textView.text = getString(R.string.value_bar_value, 0, maxValueCont)
        editText.filters = arrayOf(InputFilter.LengthFilter(maxValueCont))

        editText.addTextChangedListener { text ->
            val length = text?.length ?: 0
            textView.text = getString(R.string.value_bar_value, length, maxValueCont)
        }
    }

    /**
     * Populates the category spinner with available recipe types from the database.
     */
    private fun setupTypeSpinner() {
        lifecycleScope.launch {
            val types = withContext(Dispatchers.IO) {
                (application as App).typeDao.getAllTypes().map { it.type }
            }
            val adapter = ArrayAdapter(this@EditRecipeActivity, android.R.layout.simple_dropdown_item_1line, types)
            binding.autoCompleteType.setAdapter(adapter)
        }
    }


    /**
     * Retrieves a single recipe from the database and executes a callback.
     * @param id The recipe ID.
     * @param recipe Callback to handle the retrieved recipe.
     */
    private fun getRecipe(id: Int, recipe: (Recipe) -> Unit) {
        lifecycleScope.launch {
            val recipeResult = withContext(Dispatchers.IO) {
                (application as App).recipeDao.getRecipe(id)
            }

            if (recipeResult != null) recipe(recipeResult)
        }
    }

    /**
     * Populates the activity's fields with data from a [Recipe] object.
     * @param recipe The recipe to display.
     */
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

        binding.autoCompleteType.setText(recipe.type, false)
        binding.edtxRecipeTitle.setText(recipe.title)
        binding.edtxRecipeDescription.setText(recipe.description)


        difficultAdapter.setDifficultyLevel(recipe.difficult)
        val rvDifficult = binding.rvRecipeDifficult
        val layoutManager = object : LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) {
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

    /**
     * Updates an existing recipe in the database.
     * @param newRecipe The updated recipe object.
     */
    private fun updateRecipe(newRecipe: Recipe) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                (application as App).recipeDao.update(newRecipe)
            }

            intent.putExtra(MainActivity.EXTRA_RELOAD, true)
            setResult(RESULT_OK, intent)
            Toast.makeText(this@EditRecipeActivity, "Receita atualizada com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Collects data from all input fields and returns a new [Recipe] object.
     * @return A [Recipe] object with the new data, or null if validation fails.
     */
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

        return currentRecipe?.copy(
            title = title,
            description = description,
            difficult = difficult,
            imageUriString = imageUriString,
            ingredients = ingredients,
            preparationMode = preparations,
            cookingTime = cookingTime,
            preparationTime = preparationTime,
            type = binding.autoCompleteType.text.toString()
        )
    }

    /**
     * Formats total minutes into hours and minutes.
     * @param totalMinutes Total time in minutes.
     * @return A map containing "hour" and "min" keys.
     */
    private fun timeFormater(totalMinutes: Int): Map<String, Int> {
        val h = totalMinutes / 60
        val min = totalMinutes % 60

        return mapOf(
            "hour" to h, "min" to min
        )
    }

    /**
     * Calculates total minutes from hours and minutes.
     * @param hour Hours.
     * @param minutes Minutes.
     * @return Total minutes.
     */
    private fun sumHourMinutes(hour: Int, minutes: Int): Int {
        val totalInMinutes = (hour * 60) + minutes
        return totalInMinutes
    }
}