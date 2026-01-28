package com.sam.topchef.fature_import_recipe.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.data.model.Type
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.core.utils.adapter.ImagesAdapter
import com.sam.topchef.core.utils.adapter.TextsAdapter
import com.sam.topchef.databinding.ActivityAddRecipeBinding
import com.sam.topchef.fature_import_recipe.importer.TudoGostosoImporter
import com.sam.topchef.feature_add_recipe.adapter.RecipeDifficultAdapter
import com.sam.topchef.feature_feed_main.ui.activity.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportRecipeActivity : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val urlIntent = intent.extras?.getString("urlPath")


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

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val url = sharedText?.let { extractUrlFromSharedText(it) }
        if (url != null) { startImport(url) } else if (urlIntent != null) { startImport(urlIntent) }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@ImportRecipeActivity)
                    .setTitle("Alerta")
                    .setMessage("Deseja continuar com a edição?")
                    .setPositiveButton("Sim") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setNegativeButton("Não") { dialog, _ ->
                        dialog.dismiss()
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        goToMain()
                    }
                    .show()

            }
        })

        binding.btnAddImages.setOnClickListener {
            pickImages.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            val edtxTitle = binding.edtxRecipeTitle
            val title = edtxTitle.text.toString().trim().ifEmpty {
                edtxTitle.error = getString(R.string.required_field)

                val nestedScrollView = binding.mainNestedScrollView
                nestedScrollView.post {
                    nestedScrollView.smoothScrollTo(0, edtxTitle.top)
                }
                return@setOnClickListener
            }


            val description =
                binding.edtxRecipeDescription.text.toString().trim().takeIf { it.isNotEmpty() }
            val difficult = difficultAdapter.getDifficultyLevel()
            val imageUriString = imageUris

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


            currentRecipe = currentRecipe?.copy(
                title = title,
                description = description,
                difficult = difficult,
                imageUriString = imageUriString,
                ingredients = ingredients,
                preparationMode = preparations,
                cookingTime = cookingTime,
                preparationTime = preparationTime
            )
            saveType(currentRecipe?.type!!)
            saveRecipe(currentRecipe!!)
            goToMain()
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData(recipe: Recipe) {
        LoadImages().loadImagesWithBlur(
            recipe.imageUriString.firstOrNull(), binding.imgCoverAddRecipe
        )
        val rvImg = binding.rvImagesAddRecipe
        rvImg.layoutManager = LinearLayoutManager(
            this@ImportRecipeActivity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        imageUris.clear()
        imageUris.addAll(recipe.imageUriString)
        imagesAdapter.notifyDataSetChanged()
        rvImg.adapter = imagesAdapter

        binding.autoCompleteType.setText(recipe.type)
        binding.edtxRecipeTitle.setText(recipe.title)
        binding.edtxRecipeDescription.setText(recipe.description)


        difficultAdapter.setDifficultyLevel(recipe.difficult)
        val rvDifficult = binding.rvRecipeDifficult
        val layoutManager =
            object : LinearLayoutManager(this@ImportRecipeActivity, HORIZONTAL, false) {
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
        rvIngredients.layoutManager = LinearLayoutManager(this@ImportRecipeActivity)
        ingredients.clear()
        ingredients.addAll(recipe.ingredients)
        ingredientsAdapter.notifyDataSetChanged()
        rvIngredients.adapter = ingredientsAdapter


        val rvPreparation = binding.rvPreparation
        rvPreparation.layoutManager = LinearLayoutManager(this@ImportRecipeActivity)
        preparations.clear()
        preparations.addAll(recipe.preparationMode)
        preparationAdapter.notifyDataSetChanged()
        rvPreparation.adapter = preparationAdapter
    }

    private fun saveType(newType: String) {
        lifecycleScope.launch() {
            withContext(Dispatchers.IO) {
                val type = Type(type = newType)
                val dao = (application as App).typeDao

                if (!dao.getAllTypes().contains(type)) {
                    dao.insert(type)
                    Log.i("test", "New type inserted $type")
                }


            }
        }
    }

    private fun saveRecipe(recipe: Recipe) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                (application as App).recipeDao.insert(recipe)
            }

            Toast.makeText(this@ImportRecipeActivity, "Sua receita foi salva", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun extractUrlFromSharedText(text: String): String? {

        // remove espaços estranhos tipo "https://www .site.com"
        val normalized = text.replace(" ", "")

        val regex = Regex("""https?://\S+""")

        return regex.find(normalized)?.value
    }


    private fun timeFormater(totalMinutes: Int): Map<String, Int> {
        val h = totalMinutes / 60
        val min = totalMinutes % 60

        return mapOf(
            "hour" to h, "min" to min
        )
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }


    private fun sumHourMinutes(hour: Int, minutes: Int): Int {
        val totalInMinutes = (hour * 60) + minutes
        return totalInMinutes
    }

    private fun startImport(url: String) {
        lifecycleScope.launch {

            binding.progressBar.visibility = View.VISIBLE

            val recipe = TudoGostosoImporter.import(url)
            Log.i("test", url)

            if (recipe == null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@ImportRecipeActivity, "Não foi possível carregar a receita.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }
            binding.progressBar.visibility = View.GONE
            currentRecipe = recipe
            setData(recipe)

        }
    }

}


