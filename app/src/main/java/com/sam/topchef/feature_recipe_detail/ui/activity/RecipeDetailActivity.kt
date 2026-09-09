package com.sam.topchef.feature_recipe_detail.ui.activity

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.data.model.User
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.core.utils.Utils.clickAnimation
import com.sam.topchef.core.utils.Utils.shareText
import com.sam.topchef.core.utils.Utils.toShareText
import com.sam.topchef.core.utils.adapter.ImagesAdapter
import com.sam.topchef.core.utils.adapter.TextsAdapter
import com.sam.topchef.databinding.ActivityRecipeDetailBinding
import com.sam.topchef.feature_feed_main.ui.activity.MainActivity
import com.sam.topchef.feature_fullscreen_image.FullscreenImageActivity
import com.sam.topchef.feature_recipe_detail.adapter.StepsAdapter
import com.sam.topchef.feature_recipe_detail.model.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for displaying detailed information about a specific recipe.
 * Shows ingredients, preparation steps, images, and provides a cooking timer.
 */
class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailBinding
    private var recipeCookingTimerInSeconds: Int? = null
    private var currentImageUri: String? = null

    private var currentRecipe: Recipe? = null

    /**
     * Initializes the detail view, extracts the recipe ID from intent, and initiates data loading.
     */
    @SuppressLint("InternalInsetResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        setupStatusBarPadding()
        setupListeners()
        loadRecipeData()
    }

    private fun setupStatusBarPadding() {
        val statusBarHeight = resources.getDimensionPixelSize(
            resources.getIdentifier("status_bar_height", "dimen", "android")
        )
        binding.statusBarOverlay.layoutParams.height = statusBarHeight
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnFavorite.setOnClickListener { view ->
            toggleFavorite(view as ImageButton)
        }

        binding.goTimer.setOnClickListener { view ->
            showTimerDialog(view)
        }

        binding.coverImageRecipe.setOnClickListener {
            showFullscreenImage(it)
        }

        binding.btnShare.setOnClickListener {
            it.clickAnimation()
            val recipeId = intent.getIntExtra("id", -1)
            shareRecipe(recipeId)
        }
    }

    private fun toggleFavorite(btnFavorite: ImageButton) {
        btnFavorite.clickAnimation()
        val recipe = currentRecipe ?: return
        val recipeId = intent.getIntExtra("id", -1)

        recipe.isFavorite = !recipe.isFavorite
        setButtonState(recipe.isFavorite, btnFavorite, this)

        val resultIntent = Intent()
        resultIntent.putExtra(MainActivity.EXTRA_RECIPE_ID, recipeId)
        resultIntent.putExtra(MainActivity.EXTRA_IS_FAVORITE, recipe.isFavorite)
        setResult(RESULT_OK, resultIntent)
    }

    private fun showTimerDialog(view: View) {
        view.clickAnimation()
        val minutes = currentRecipe?.cookingTime ?: 0
        val id = currentRecipe?.id ?: -1
        val dialog = TimerDialog.newInstance(minutes, id)
        dialog.show(supportFragmentManager, TimerDialog.TAG)
    }

    private fun showFullscreenImage(view: View) {
        view.clickAnimation()
        val i = Intent(this, FullscreenImageActivity::class.java)
        i.putExtra("imageUri", currentImageUri)

        val options = ActivityOptions
            .makeSceneTransitionAnimation(
                (this),
                view,
                "image_transition"
            )
        startActivity(i, options.toBundle())
    }

    private fun loadRecipeData() {
        val recipeId = intent.getIntExtra("id", -1)
        if (recipeId != -1) {
            loadData(recipeId)
        } else {
            // If ID is missing, try to get it from TimerService if it's running
            if (TimerService.isTimerRunning.value && TimerService.currentRecipeId != -1) {
                loadData(TimerService.currentRecipeId)
            } else {
                Toast.makeText(this, "Erro ao carregar receita", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupRecipeCreator() {
        lifecycleScope.launch {
            var user: User? = null
            withContext(Dispatchers.IO) {
                user = (application as App).userDao.getUser()
            }

            if (currentRecipe?.chef == null){
                LoadImages().loadImagesWithBlur(user?.imageUri, binding.imageChefCover)
                binding.txtChefName.text = user?.name ?: "Sem Nome de Usuario"
                binding.txtChefWorkerPosition.text = "Sua Propia Receita :)"
            }else{
                LoadImages().loadImagesWithBlur(R.drawable.tudo_gostoso, binding.imageChefCover)
                binding.txtChefName.text = "Tudo Gostoso"
                binding.txtChefWorkerPosition.text = "Receita da Web"
            }
        }
    }


    private fun shareRecipe(id: Int, isTikTok: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            val text = if (isTikTok) {
                val tiktok = app.db.tiktokDao().getById(id)
                tiktok?.toShareText()
            } else {
                val recipe = app.db.recipeDao().getRecipe(id)
                recipe?.toShareText()
            }

            text?.let {
                withContext(Dispatchers.Main) {
                    shareText(this@RecipeDetailActivity, it)
                }
            }
        }
    }


    /**
     * Loads recipe data from the database and updates all UI components (title, images, ingredients, etc.).
     * @param recipeId The ID of the recipe to display.
     */
    private fun loadData(recipeId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            val dao = app.db.recipeDao()
            val recipe = dao.getRecipe(recipeId)

            withContext(Dispatchers.Main) {

                if (recipe == null) {
                    Toast.makeText(applicationContext, "Receita não encontrada", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                    return@withContext
                }
                currentRecipe = recipe
                setupRecipeCreator()

                val imgUriList = recipe.imageUriString
                val title = recipe.title
                val reviews = recipe.reviews
                val type = recipe.type ?: "Tipo não informado."
                val description = recipe.description ?: "Adicione uma descricao quando quiser."
                val difficult = recipe.difficult
                val ingredients = recipe.ingredients
                val cookingTime = recipe.cookingTime
                val isFavorite = recipe.isFavorite
                val preparationMode = recipe.preparationMode
                val preparationTime = recipe.preparationTime


                recipeCookingTimerInSeconds = cookingTime * 60


                fun setImage(load: String?, img: ShapeableImageView) {
                    Glide.with(this@RecipeDetailActivity)
                        .load(load)
                        .placeholder(R.drawable.placeholder_item)
                        .into(img)
                }


                val imgCover = binding.coverImageRecipe
                currentImageUri = imgUriList.firstOrNull()
                setImage(imgUriList.firstOrNull(), imgCover)

                setButtonState(isFavorite, binding.btnFavorite, this@RecipeDetailActivity)

                binding.txtRecipeType.text = type
                binding.txtRecipeTitle.text = title
                binding.txtRecipeDescription.text = description
                binding.txtDifficult.text = difficultFormater(difficult)
                binding.txtRecipeCookingTime.text = timeFormater(cookingTime)
                binding.txtRecipePreparationTime.text = timeFormater(preparationTime)


                binding.rvImageFromDetail.layoutManager =
                    LinearLayoutManager(this@RecipeDetailActivity, LinearLayoutManager.HORIZONTAL, false)
                val imagesAdapter = ImagesAdapter(imgUriList)

                imagesAdapter.onImgClickListener = { imageSrc ->
                    setImage(imageSrc, imgCover)
                    currentImageUri = imageSrc
                }
                binding.rvImageFromDetail.adapter = imagesAdapter


                binding.rvIngredients.layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
                val rvIngredient = binding.rvIngredients
                rvIngredient.layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
                rvIngredient.adapter = TextsAdapter(ingredients)


                binding.rvIngredients.layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
                val rvSteps = binding.rvSteps
                val steps = preparationMode.map { Step(it) }
                rvSteps.layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
                rvSteps.adapter = StepsAdapter(steps)

            }

        }
    }


    /**
     * Converts a difficulty level (1-5) into a localized string.
     * @param difficult The difficulty integer.
     * @return A string representation like "Very Easy" or "Hard".
     */
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

    /**
     * Updates the recipe's favorite status or other fields in the database.
     * @param recipe The recipe object to update.
     */
    private fun updateRecipe(recipe: Recipe) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                (application as App).recipeDao.update(recipe)
            }
        }
    }


    /**
     * Formats total minutes into a displayable "Xh:XXmin" string.
     * @param totalMinutes Total time in minutes.
     */
    @SuppressLint("DefaultLocale")
    private fun timeFormater(totalMinutes: Int): String {
        val h = totalMinutes / 60
        val min = totalMinutes % 60

        return String.format("%dh:%02dmin", h, min)
    }

    /**
     * Updates the visual state (tint) of the favorite button based on the recipe's favorite status.
     * @param isFavorite Whether the recipe is marked as favorite.
     * @param btnFavorite The button view to update.
     * @param context The context for retrieving colors.
     */
    private fun setButtonState(isFavorite: Boolean, btnFavorite: ImageButton, context: Context) {
        if (isFavorite) btnFavorite.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.default_color_app)
        ) else btnFavorite.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.myGray)
        )

    }

}