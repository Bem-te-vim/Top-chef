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
    private var originalIngredients: List<String> = emptyList()
    private var currentPortionFactor: Double = 1.0

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
                binding.txtChefName.text = user?.name ?: getString(R.string.without_username)
                binding.txtChefWorkerPosition.text = getString(R.string.you_own_recipe)
            }else{
                LoadImages().loadImagesWithBlur(R.drawable.tudo_gostoso, binding.imageChefCover)
                binding.txtChefName.text = getString(R.string.tudo_gostoso)
                binding.txtChefWorkerPosition.text = getString(R.string.recipe_from_web)
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


                originalIngredients = ingredients
                currentPortionFactor = 1.0

                binding.rvIngredients.layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
                binding.rvIngredients.adapter = TextsAdapter(ingredients)

                setupPortionSelector()

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

    private fun setupPortionSelector() {
        val portionOptions = listOf("1x (Padrão)", "2x (Dobro)", "1/2 (Metade)", "Personalizado...")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, portionOptions)

        val autoComplete = binding.autoCompleteService
        autoComplete.apply {
            setAdapter(adapter)
            setText("1x (Padrão)", false)
            setDropDownBackgroundDrawable(
                ContextCompat.getDrawable(this@RecipeDetailActivity, R.drawable.bg_dropdown_dark)
            )
        }

        autoComplete.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> applyPortionFactor(1.0, "1x (Padrão)")
                1 -> applyPortionFactor(2.0, "2x (Dobro)")
                2 -> applyPortionFactor(0.5, "1/2 (Metade)")
                3 -> showCustomPortionDialog()
            }
        }
    }

    private fun showCustomPortionDialog() {
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            hint = "Ex: 3, 1.5, 0.5, 1/3"
            setPadding(48, 32, 48, 32)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Porcionar Receita")
            .setMessage("Digite a quantidade desejada para multiplicar ou dividir a receita:")
            .setView(input)
            .setPositiveButton("Aplicar") { dialog, _ ->
                val text = input.text.toString()
                val factor = parsePortionFactor(text)
                if (factor != null && factor > 0) {
                    val label = formatFactorLabel(factor)
                    applyPortionFactor(factor, label)
                } else {
                    Toast.makeText(this, "Valor inválido. Digite um número ex: 3, 1.5 ou 1/2", Toast.LENGTH_SHORT).show()
                    restorePortionLabel()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                restorePortionLabel()
                dialog.dismiss()
            }
            .setOnCancelListener {
                restorePortionLabel()
            }
            .show()
    }

    private fun restorePortionLabel() {
        val label = formatFactorLabel(currentPortionFactor)
        binding.autoCompleteService.setText(label, false)
    }

    private fun formatFactorLabel(factor: Double): String {
        return when (factor) {
            1.0 -> "1x (Padrão)"
            2.0 -> "2x (Dobro)"
            0.5 -> "1/2 (Metade)"
            else -> {
                val numStr = if (factor % 1.0 == 0.0) {
                    factor.toLong().toString()
                } else {
                    String.format(java.util.Locale.US, "%.2f", factor)
                        .replace(Regex("""\.?0+$"""), "")
                }
                "${numStr}x (Personalizado)"
            }
        }
    }

    private fun applyPortionFactor(factor: Double, label: String) {
        currentPortionFactor = factor
        binding.autoCompleteService.setText(label, false)

        val scaledList = originalIngredients.map { scaleIngredientText(it, factor) }
        binding.rvIngredients.adapter = TextsAdapter(scaledList)
    }

    private fun parsePortionFactor(input: String): Double? {
        val clean = input.trim().lowercase().replace("x", "").trim()
        if (clean.isEmpty()) return null

        if (clean == "dobro") return 2.0
        if (clean == "metade") return 0.5
        if (clean == "triplo") return 3.0

        if (clean.contains("/")) {
            val parts = clean.split("/")
            if (parts.size == 2) {
                val num = parts[0].trim().toDoubleOrNull()
                val den = parts[1].trim().toDoubleOrNull()
                if (num != null && den != null && den != 0.0) {
                    return num / den
                }
            }
        }

        val mixedParts = clean.split("\\s+".toRegex())
        if (mixedParts.size == 2 && mixedParts[1].contains("/")) {
            val whole = mixedParts[0].toDoubleOrNull()
            val fracParts = mixedParts[1].split("/")
            if (whole != null && fracParts.size == 2) {
                val num = fracParts[0].toDoubleOrNull()
                val den = fracParts[1].toDoubleOrNull()
                if (num != null && den != null && den != 0.0) {
                    return whole + (num / den)
                }
            }
        }

        return clean.replace(",", ".").toDoubleOrNull()
    }

    private fun scaleIngredientText(ingredient: String, factor: Double): String {
        if (factor == 1.0) return ingredient

        val wordNumberMap = mapOf(
            "uma" to 1.0,
            "um" to 1.0,
            "duas" to 2.0,
            "dois" to 2.0,
            "três" to 3.0,
            "tres" to 3.0,
            "quatro" to 4.0,
            "cinco" to 5.0,
            "seis" to 6.0,
            "sete" to 7.0,
            "oito" to 8.0,
            "nove" to 9.0,
            "dez" to 10.0,
            "meia" to 0.5,
            "meio" to 0.5
        )

        val unitPluralMap = mapOf(
            "xícara" to "xícaras",
            "xicara" to "xicaras",
            "colher" to "colheres",
            "copo" to "copos",
            "pitada" to "pitadas",
            "dente" to "dentes",
            "fatia" to "fatias",
            "pacote" to "pacotes",
            "lata" to "latas",
            "caixa" to "caixas",
            "grama" to "gramas",
            "quilo" to "quilos",
            "unidade" to "unidades",
            "ramo" to "ramos",
            "folha" to "folhas",
            "porção" to "porções",
            "porcao" to "porcoes",
            "limão" to "limões",
            "limao" to "limoes",
            "ovo" to "ovos",
            "caixinha" to "caixinhas",
            "latinha" to "latinhas",
            "vidro" to "vidros",
            "garrafa" to "garrafas",
            "envelope" to "envelopes",
            "cubo" to "cubos",
            "tablete" to "tabletes",
            "pedaço" to "pedaços",
            "pedaco" to "pedacos",
            "penca" to "pencas",
            "cabeça" to "cabeças",
            "cabeca" to "cabecas"
        )

        val unitSingularMap = unitPluralMap.entries.associateBy({ it.value }, { it.key })

        val mixedRegex = Regex("""\b(\d+)\s+(\d+)/(\d+)\b""")
        val fractionRegex = Regex("""\b(\d+)/(\d+)\b""")
        val numberRegex = Regex("""\b(\d+(?:[.,]\d+)?)\b""")
        val wordRegex = Regex("""\b(?i)(uma|um|duas|dois|três|tres|quatro|cinco|seis|sete|oito|nove|dez|meia|meio)\b""")

        data class Token(val range: IntRange, val rawValue: Double)

        val tokens = mutableListOf<Token>()
        val matchedRanges = mutableListOf<IntRange>()

        fun overlaps(range: IntRange): Boolean {
            return matchedRanges.any { it.first <= range.last && range.first <= it.last }
        }

        mixedRegex.findAll(ingredient).forEach { m ->
            val range = m.range
            if (!overlaps(range)) {
                val whole = m.groupValues[1].toDouble()
                val num = m.groupValues[2].toDouble()
                val den = m.groupValues[3].toDouble()
                if (den != 0.0) {
                    tokens.add(Token(range, whole + (num / den)))
                    matchedRanges.add(range)
                }
            }
        }

        fractionRegex.findAll(ingredient).forEach { m ->
            val range = m.range
            if (!overlaps(range)) {
                val num = m.groupValues[1].toDouble()
                val den = m.groupValues[2].toDouble()
                if (den != 0.0) {
                    tokens.add(Token(range, num / den))
                    matchedRanges.add(range)
                }
            }
        }

        numberRegex.findAll(ingredient).forEach { m ->
            val range = m.range
            if (!overlaps(range)) {
                val num = m.groupValues[1].replace(",", ".").toDoubleOrNull()
                if (num != null) {
                    tokens.add(Token(range, num))
                    matchedRanges.add(range)
                }
            }
        }

        wordRegex.findAll(ingredient).forEach { m ->
            val range = m.range
            if (!overlaps(range)) {
                val word = m.groupValues[1].lowercase()
                val num = wordNumberMap[word]
                if (num != null) {
                    tokens.add(Token(range, num))
                    matchedRanges.add(range)
                }
            }
        }

        if (tokens.isEmpty()) {
            return ingredient
        }

        tokens.sortByDescending { it.range.first }

        var result = ingredient
        for (token in tokens) {
            val scaledVal = token.rawValue * factor
            val formattedVal = formatScaledValue(scaledVal)

            val afterIndex = token.range.last + 1
            val remainder = result.substring(afterIndex)
            val unitMatch = Regex("""^\s*([a-zA-ZáàâãéèêíïóôõöúçñÁÀÂÃÉÈÊÍÏÓÔÕÖÚÇÑ]+)""").find(remainder)

            if (unitMatch != null) {
                val unitWord = unitMatch.groupValues[1]
                val unitLower = unitWord.lowercase()

                if (scaledVal == 1.0) {
                    val singular = unitSingularMap[unitLower]
                    if (singular != null) {
                        val replacementUnit = matchCase(unitWord, singular)
                        val unitRangeInRemainder = unitMatch.groups[1]!!.range
                        val fullRemainderUnitStart = afterIndex + unitRangeInRemainder.first
                        val fullRemainderUnitEnd = afterIndex + unitRangeInRemainder.last + 1

                        result = result.substring(0, fullRemainderUnitStart) + replacementUnit + result.substring(fullRemainderUnitEnd)
                    }
                } else if (scaledVal > 1.0) {
                    val plural = unitPluralMap[unitLower]
                    if (plural != null) {
                        val replacementUnit = matchCase(unitWord, plural)
                        val unitRangeInRemainder = unitMatch.groups[1]!!.range
                        val fullRemainderUnitStart = afterIndex + unitRangeInRemainder.first
                        val fullRemainderUnitEnd = afterIndex + unitRangeInRemainder.last + 1

                        result = result.substring(0, fullRemainderUnitStart) + replacementUnit + result.substring(fullRemainderUnitEnd)
                    }
                }
            }

            result = result.replaceRange(token.range, formattedVal)
        }

        return result
    }

    private fun formatScaledValue(value: Double): String {
        val longVal = kotlin.math.round(value).toLong()
        if (Math.abs(value - longVal) < 0.001) {
            return longVal.toString()
        }

        val wholePart = value.toInt()
        val fracPart = value - wholePart

        val fracStr = when {
            Math.abs(fracPart - 0.5) < 0.05 -> "1/2"
            Math.abs(fracPart - 0.25) < 0.05 -> "1/4"
            Math.abs(fracPart - 0.75) < 0.05 -> "3/4"
            Math.abs(fracPart - 0.333) < 0.05 -> "1/3"
            Math.abs(fracPart - 0.667) < 0.05 -> "2/3"
            else -> null
        }

        return if (fracStr != null) {
            if (wholePart > 0) "$wholePart $fracStr" else fracStr
        } else {
            String.format(java.util.Locale.US, "%.2f", value)
                .replace(Regex("""\.?0+$"""), "")
        }
    }

    private fun matchCase(original: String, target: String): String {
        return if (original.firstOrNull()?.isUpperCase() == true) {
            target.replaceFirstChar { it.uppercase() }
        } else {
            target
        }
    }

}