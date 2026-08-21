package com.sam.topchef.feature_import_from_tiktok.view

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.sam.topchef.R
import com.sam.topchef.core.data.local.appDataBase.AppDataBase
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.utils.Utils.clickAnimation
import com.sam.topchef.core.utils.Utils.hide
import com.sam.topchef.core.utils.Utils.setClicksListener
import com.sam.topchef.core.utils.Utils.shareText
import com.sam.topchef.core.utils.Utils.show
import com.sam.topchef.core.utils.Utils.toShareText
import com.sam.topchef.databinding.ActivityTiktokImportBinding
import com.sam.topchef.feature_feed_main.ui.activity.MainActivity
import com.sam.topchef.feature_import_from_tiktok.ia.RecipeInfoByIA
import com.sam.topchef.feature_import_from_tiktok.model.TikTokData
import com.sam.topchef.feature_import_from_tiktok.model.TikTokModel
import com.sam.topchef.feature_import_from_tiktok.player.PlayerListener
import com.sam.topchef.feature_import_from_tiktok.presenter.TikTokImportPresenter
import com.sam.topchef.feature_import_from_tiktok.presenter.TikTokUICallBack
import com.sam.topchef.feature_shopping_list.activities.CartActivity
import com.sam.topchef.feature_shopping_list.activities.ShoppingListActivity
import com.sam.topchef.feature_shopping_list.data.model.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Activity responsible for importing and displaying TikTok recipe data.
 * It implements the [TikTokUICallBack] to handle UI updates from the presenter.
 */
class TiktokImportActivity : AppCompatActivity(), TikTokUICallBack {
    private lateinit var binding: ActivityTiktokImportBinding
    private lateinit var presenter: TikTokImportPresenter
    private lateinit var player: ExoPlayer

    private lateinit var message: TextView
    private lateinit var recipeInfoByIA: RecipeInfoByIA
    private var pulseAnimator: ObjectAnimator? = null
    private var currentOriginUrl: String? = null
    private var isRefreshing = false
    private var currentRecipeId: Int? = null

    private var currentTikTokModel: TikTokModel? = null

    private lateinit var playerListener: PlayerListener

    /**
     * Initializes the activity, sets up edge-to-edge display, binding,
     * presenter, and ExoPlayer. Initiates data fetching for a sample TikTok URL.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState].
     */
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTiktokImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36")
            .setDefaultRequestProperties(mapOf("Referer" to "https://www.tiktok.com/"))
            .setAllowCrossProtocolRedirects(true)


        playerListener = PlayerListener()

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setDataSourceFactory(
                    dataSourceFactory
                )
            )
            .build()

        player.repeatMode = Player.REPEAT_MODE_ALL

        val playerView = binding.playerView
        playerView.player = player




        /** Pause and play the video Player on single click, show toast on double click **/
        playerView.setClicksListener(
            onSingleClick = {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            },
            onDoubleClick = {
                Toast.makeText(this, "DoubleClick", Toast.LENGTH_SHORT).show()
            },
            onHold = {
                player.setPlaybackSpeed(2f)
                binding.message.show()
                binding.message.text = "2x Speed"
            },
            onRelease = {
                player.setPlaybackSpeed(1f)
                binding.message.hide()
            }
        )

        /** video interactions **/
        binding.btnFavorite.setOnClickListener {
            it.clickAnimation()
        }

        binding.btnCart.setOnClickListener {
            it.clickAnimation()
            moveToCart()
        }

        binding.btnShare.setOnClickListener {
            it.clickAnimation()
            share()
        }

        binding.btnReopenDialog.setOnClickListener {
            it.clickAnimation()
            currentTikTokModel?.let { recipe ->
                showRecipeDialog(recipe)
            } ?: run {
                Toast.makeText(this, "Nenhuma receita carregada ainda", Toast.LENGTH_SHORT).show()
            }
        }


        player.addListener(playerListener)

        playerListener.onPlayerError {   val cause = it.cause
            if (cause is HttpDataSource.InvalidResponseCodeException && cause.responseCode == 403) {
                handle403Error()
            } }

        playerListener.isPlaying { showPlayerIc(it) }

        presenter = TikTokImportPresenter(this)
        recipeInfoByIA = RecipeInfoByIA()
        message = binding.message

        val sharedText = intent.getStringExtra("urlPath")
        val tiktokId = intent.getIntExtra("tiktokId", -1)

        if (tiktokId != -1) {
            loadSavedTiktokRecipe(tiktokId)
        } else if (sharedText != null) {
            val url = extractUrlFromSharedText(sharedText)
            if (url != null) {
                currentOriginUrl = url
                presenter.getTikTokData(url)
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
    /**
     * Shares the current TikTok recipe's information using the system share sheet.
     */
    fun share(){
        currentTikTokModel?.let { recipe ->
            shareText(this, recipe.toShareText())
        } ?: run {
            Toast.makeText(this, "Nenhuma receita carregada ainda", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Converts the current TikTok recipe's ingredients into a shopping cart and redirects the user
     * to the shopping list view.
     */
    fun moveToCart(){
        currentTikTokModel?.let { recipe ->
            val cartItems = recipe.ingredients.flatMap { section ->
                section.sectionItems.map { item -> CartItem(itemName = item) }
            }

            val newCart = Cart(
                title = recipe.name,
                cartImage = recipe.thumbnail,
                cartItems = cartItems
            )

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDataBase.getDataBase(this@TiktokImportActivity)
                val cartId = db.cartDao().insert(newCart).toInt()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TiktokImportActivity, "Ingredientes movidos para o carrinho!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@TiktokImportActivity, ShoppingListActivity::class.java)
                    intent.putExtra("id", cartId)
                    startActivity(intent)
                }
            }
        } ?: run {
            Toast.makeText(this, "Aguarde o carregamento da receita", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showPlayerIc(isPlaying: Boolean){
        if(isPlaying){
            binding.playIc.hide()
        }else{
            binding.playIc.show()
        }
    }


    /**
     * Regex utility to extract a URL from a shared string of text.
     * @param text The raw text containing a potential URL.
     * @return The extracted URL string, or null if not found.
     */
    private fun extractUrlFromSharedText(text: String): String? {
        val normalized = text.replace(" ", "")
        val regex = Regex("""https?://\S+""")
        return regex.find(normalized)?.value
    }

    /**
     * Updates the UI with the fetched TikTok data, specifically setting
     * and playing the video URL in the ExoPlayer.
     *
     * @param response The data object containing TikTok video information.
     */
    override fun showData(response: TikTokData) {
        val mediaItem = MediaItem.fromUri(response.data.videoUrl)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        Log.i("thumbNail", response.data.thumbnail)

        if (isRefreshing && currentRecipeId != null) {
            updateVideoUrlInDb(currentRecipeId!!, response.data.videoUrl)
        } else if (!isRefreshing) {
            importRecipeWithIA(response)
        }
        isRefreshing = false
    }

    /**
     * Updates the video player's source URL for a specific recipe in the database.
     * @param id The ID of the TikTok recipe.
     * @param newVideoUrl The fresh video URL to save.
     */
    private fun updateVideoUrlInDb(id: Int, newVideoUrl: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDataBase.getDataBase(this@TiktokImportActivity)
            val recipe = db.tiktokDao().getById(id)
            if (recipe != null) {
                val updated = recipe.copy(videoUrl = newVideoUrl)
                db.tiktokDao().update(updated)
                withContext(Dispatchers.Main) {
                    if (currentRecipeId == id) {
                        currentTikTokModel = updated
                    }
                }
                Log.d("TiktokImport", "Updated video URL in DB for recipe $id")
            }
        }
    }

    /**
     * Uses AI services to extract recipe details (ingredients, steps) from the TikTok audio/content.
     * @param response The TikTok data containing the video source.
     */
    private fun importRecipeWithIA(response: TikTokData) {
        lifecycleScope.launch {
            try {
                message.visibility = View.VISIBLE
                messageLoadAnimation(true)
                message.text = getString(R.string.load_info)

                val tempFile = File(cacheDir, "temp_audio.mp3")
                val audioFile = recipeInfoByIA.downloadAudio(response.data.videoUrl, tempFile)

                val recipe = recipeInfoByIA.importRecipe(listOf(response.data.title), audioFile)
                recipe.copy(
                    thumbnail = response.data.thumbnail,
                    videoUrl = response.data.videoUrl,
                    originUrl = currentOriginUrl
                ).also { updatedRecipe ->
                    Log.d("RecipeIA", "Recipe imported successfully: $updatedRecipe")
                    currentTikTokModel = updatedRecipe
                    message.visibility = View.GONE
                    messageLoadAnimation(false)

                    showRecipeDialog(updatedRecipe)
                }
            } catch (e: Exception) {
                message.visibility = View.GONE
                messageLoadAnimation(false)
                Log.e("RecipeIA", "Error importing recipe with IA", e)

                Snackbar.make(
                    binding.root,
                    "Erro ao converter receita",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Try Again") {
                        importRecipeWithIA(response)
                    }.show()
            }
        }
    }

    /**
     * Displays a dialog showing the parsed/imported recipe data for user confirmation.
     * @param recipe The imported TikTok recipe data to show.
     */
    private fun showRecipeDialog(recipe: TikTokModel) {
        val dialog = TiktokRecipeDataDialog.newInstance(recipe)
        dialog.show(supportFragmentManager, TiktokRecipeDataDialog.TAG)
    }

    /**
     * Starts or stops a pulsing animation on the status message text.
     * @param start True to start the animation, false to stop it.
     */
    private fun messageLoadAnimation(start: Boolean) {
        if (start) {
            if (pulseAnimator == null) {
                val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.02f)
                val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.02f)
                pulseAnimator =
                    ObjectAnimator.ofPropertyValuesHolder(message, scaleX, scaleY).apply {
                        duration = 1000
                        repeatCount = ObjectAnimator.INFINITE
                        repeatMode = ObjectAnimator.REVERSE
                        start()
                    }
            }
        } else {
            pulseAnimator?.cancel()
            pulseAnimator = null
            message.scaleX = 1f
            message.scaleY = 1f
        }
    }

    /**
     * Displays a failure message to the user.
     *
     * @param message The error message to display.
     */
    override fun showFailure(message: String) {
        this.message.visibility = View.VISIBLE
        this.message.text = message
    }

    /**
     * Shows a progress indicator while data is being fetched.
     */
    override fun showProgress() {
        binding.progressBarImport.show()
    }

    /**
     * Hides the progress indicator once data fetching is complete or failed.
     */
    override fun hideProgress() {
        binding.progressBarImport.hide()
    }

    /**
     * Loads a previously saved TikTok recipe from the database and resumes playback.
     * @param id The recipe ID in the local database.
     */
    private fun loadSavedTiktokRecipe(id: Int) {
        currentRecipeId = id
        lifecycleScope.launch {
            val db = AppDataBase.getDataBase(this@TiktokImportActivity)
            val recipe = withContext(Dispatchers.IO) {
                db.tiktokDao().getById(id)
            }
            if (recipe != null) {
                currentTikTokModel = recipe
                currentOriginUrl = recipe.originUrl
                val mediaItem = MediaItem.fromUri(recipe.videoUrl ?: "")

                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
                showRecipeDialog(recipe)
            }
        }
    }

    /**
     * Handles 403 Forbidden errors by attempting to re-fetch/refresh the TikTok data.
     */
    private fun handle403Error() {
        if (isRefreshing || currentOriginUrl == null) return

        isRefreshing = true
        Log.d(
            "TiktokImport",
            "403 error detected, attempting to refresh URL from: $currentOriginUrl"
        )
        presenter.getTikTokData(currentOriginUrl!!)
    }

    /**
     * Releases ExoPlayer resources when the activity is destroyed.
     */
    override fun onDestroy() {
        pulseAnimator?.cancel()
        player.release()
        super.onDestroy()
    }

    /**
     * Pauses the player when the activity is stopped.
     */
    override fun onStop() {
        player.pause()
        super.onStop()
    }

    /**
     * Resumes video playback when the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        if (player.mediaItemCount > 0) {
            player.play()
        }
    }
}