package com.sam.topchef.feature_add_recipe.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.data.model.Type
import com.sam.topchef.core.utils.adapter.ImagesAdapter
import com.sam.topchef.core.utils.adapter.TextsAdapter
import com.sam.topchef.databinding.ActivityAddRecipeBinding
import com.sam.topchef.databinding.DialogAddTypeBinding
import com.sam.topchef.feature_add_recipe.adapter.RecipeDifficultAdapter
import com.sam.topchef.feature_feed_main.ui.activity.MainActivity
import kotlin.concurrent.thread

/**
 * Activity responsible for creating new recipes.
 * Handles image selection, difficulty setting, and input of ingredients and preparation steps.
 */
class AddRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddRecipeBinding
    private lateinit var imagesAdapter: ImagesAdapter
    private val difficultAdapter = RecipeDifficultAdapter()

    private val typeItems = mutableListOf<String>()
    private lateinit var typeAdapter: ArrayAdapter<String>


    private val selectedUris = mutableListOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    private val pickImages =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                if (uris.size <= 5) {
                    Glide.with(this).load(uris.first()).into(binding.imgCoverAddRecipe)
                    selectedUris.clear()
                    selectedUris.addAll(uris.map { it.toString() })
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
     * Initializes the activity, sets up adapters for images, difficulty, and lists, and handles save actions.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val imgCover = binding.imgCoverAddRecipe

        val rvImg = binding.rvImagesAddRecipe
        rvImg.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imagesAdapter = ImagesAdapter(selectedUris)
        imagesAdapter.onImgClickListener = { img ->
            Glide.with(this)
                .load(img)
                .placeholder(R.drawable.placeholder_item)
                .into(imgCover)
        }

        imagesAdapter.onImgLongClickListener = { position ->
            AlertDialog.Builder(this)
                .setTitle("Remover imagem?")
                .setNegativeButton("Cancelar") { p0, _ -> p0.dismiss() }
                .setPositiveButton("Remover") { _, _ ->
                    selectedUris.removeAt(position)
                    imagesAdapter.notifyItemRemoved(position)

                    Glide.with(this)
                        .load(selectedUris.firstOrNull())
                        .placeholder(R.drawable.placeholder_item)
                        .into(imgCover)

                }.show()
            true
        }
        rvImg.adapter = imagesAdapter

        binding.btnAddImages.setOnClickListener {
            pickImages.launch("image/*")
        }


        thread {
            val app = application as App
            val typeDao = app.db.typeDao()
            val allTypes = typeDao.getAllTypeNames()

            runOnUiThread {
                typeItems.clear()
                typeItems.addAll(allTypes)
                typeItems.add("Add+")
                
                typeAdapter = ArrayAdapter(this@AddRecipeActivity, android.R.layout.simple_list_item_1, typeItems)
                val autoCompleteType = binding.autoCompleteType
                autoCompleteType.apply {
                    setText(getString(R.string.type))
                    setAdapter(typeAdapter)
                    setDropDownBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@AddRecipeActivity,
                            R.drawable.bg_dropdown_dark
                        )
                    )
                }

                autoCompleteType.setOnItemClickListener { _, _, position, _ ->
                    val selectedItem = typeItems[position]
                    if (selectedItem == "Add+") {
                        showAddTypeDialog()
                    }
                }
            }
        }


        val edtxTitle = binding.edtxRecipeTitle
        val characterCountTitle = binding.characterCountTitle
        setViewCount(edtxTitle, characterCountTitle, 45)


        val edtxDescription = binding.edtxRecipeDescription
        val characterCountDescription = binding.characterCountDescription
        setViewCount(edtxDescription, characterCountDescription, 300)


        val rvDifficult = binding.rvRecipeDifficult
        val layoutManager =
            object : LinearLayoutManager(this, HORIZONTAL, false) {
                override fun canScrollHorizontally(): Boolean {
                    return false
                }
            }
        rvDifficult.layoutManager = layoutManager
        rvDifficult.adapter = difficultAdapter


        val ingredientList = mutableListOf<String>()
        val rvIngredients = binding.rvIngredients
        rvIngredients.layoutManager = LinearLayoutManager(this)

        val ingredientAdapter = TextsAdapter(ingredientList, true)
        rvIngredients.adapter = ingredientAdapter

        val addIngredient = binding.addIngredient
        addIngredient.setOnClickListener {

            val ingredientFiled = binding.edtxIngredient
            val ingredient =
                ingredientFiled.text.toString().trim().ifEmpty { return@setOnClickListener }

            ingredientList.add(ingredient)
            ingredientAdapter.notifyItemInserted(ingredientList.lastIndex)
            ingredientFiled.text.clear()
        }

        val stepsList = mutableListOf<String>()
        val rvPreparation = binding.rvPreparation
        rvPreparation.layoutManager = LinearLayoutManager(this)

        val stepsAdapter = TextsAdapter(stepsList, true)
        rvPreparation.adapter = stepsAdapter

        val addStep = binding.addStep
        addStep.setOnClickListener {
            val stepField = binding.edtxStep
            val step = stepField.text.toString().trim().ifEmpty { return@setOnClickListener }

            stepsList.add(step)
            stepsAdapter.notifyItemInserted(stepsList.lastIndex)
            stepField.text.clear()
        }

        binding.btnSave.setOnClickListener { view ->

            val title = edtxTitle.text.toString().trim().ifEmpty {
                edtxTitle.error = getString(R.string.required_field)

                val nestedScrollView = binding.mainNestedScrollView
                nestedScrollView.post {
                    nestedScrollView.smoothScrollTo(0, edtxTitle.top)
                }
                return@setOnClickListener
            }

            val description = edtxDescription.text.toString().trim().takeIf { it.isNotEmpty() }
            val difficult = difficultAdapter.getDifficultyLevel()
            val imageUriString = selectedUris

            val cookingTimeHour =
                binding.edtxCokingTimeHour.text.toString().trim().toIntOrNull() ?: 0
            val cookingTimeMinute =
                binding.edtxCokingTimeMinute.text.toString().trim().toIntOrNull() ?: 0
            val cookingTime = sumHourMinutes(cookingTimeHour, cookingTimeMinute)


            val preparationTimeHour =
                binding.edtxPreparationTimeHour.text.toString().trim().toIntOrNull() ?: 0
            val preparationTimeMinute =
                binding.edtxPreparationTimeMinute.text.toString().trim().toIntOrNull() ?: 0
            val preparationTime = sumHourMinutes(preparationTimeHour, preparationTimeMinute)

            val recipeType = binding.autoCompleteType.text.toString().trim().takeIf { 
                it.isNotEmpty() && it != getString(R.string.type)
            }

            val recipe = Recipe(
                title = title,
                description = description,
                difficult = difficult,
                imageUriString = imageUriString,
                ingredients = ingredientList,
                preparationMode = stepsList,
                cookingTime = cookingTime,
                preparationTime = preparationTime,
                type = recipeType
            )
            thread {
                val app = application as App
                val dao = app.db.recipeDao()
                
                recipeType?.let { typeName ->
                    val typeDao = app.db.typeDao()
                    if (!typeDao.getAllTypeNames().contains(typeName)) {
                        typeDao.insert(Type(type = typeName))
                    }
                }
                
                dao.insert(recipe)

                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.your_recipe_will_saved),
                        Toast.LENGTH_SHORT
                    ).show()

                    intent.putExtra(MainActivity.EXTRA_RELOAD, true)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }


    }

    /**
     * take the length of a editText and set the value in
     * one textview
     **/
    /**
     * Monitors character count in an EditText and updates a corresponding TextView.
     * @param editText The input field to monitor.
     * @param textView The display for the current count.
     * @param maxValueCont The maximum allowed characters.
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
     * Converts hours and minutes into total minutes.
     * @param hour The number of hours.
     * @param minutes The number of minutes.
     * @return Total time in minutes.
     */
    private fun sumHourMinutes(hour: Int, minutes: Int): Int {
        val totalInMinutes = (hour * 60) + minutes
        return totalInMinutes
    }

    /**
     * Shows a dialog to manually add a new recipe type/category.
     */
    private fun showAddTypeDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogAddTypeBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.btnAddType.setOnClickListener {
            val newType = dialogBinding.edtxNewType.text.toString().trim()
            if (newType.isNotEmpty()) {
                if (!typeItems.contains(newType)) {
                    typeItems.add(typeItems.size - 1, newType)
                    typeAdapter.notifyDataSetChanged()
                }
                binding.autoCompleteType.setText(newType, false)
                dialog.dismiss()
            } else {
                dialogBinding.edtxNewType.error = "Campo obrigatório"
            }
        }

        dialog.show()
    }


}
