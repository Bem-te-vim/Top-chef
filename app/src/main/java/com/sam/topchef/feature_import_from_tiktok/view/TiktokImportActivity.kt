package com.sam.topchef.feature_import_from_tiktok.view

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.sam.topchef.R
import com.sam.topchef.core.utils.Utils.hide
import com.sam.topchef.core.utils.Utils.show
import com.sam.topchef.databinding.ActivityTiktokImportBinding
import com.sam.topchef.feature_feed_main.ui.activity.MainActivity
import com.sam.topchef.feature_import_from_tiktok.ia.RecipeInfoByIA
import com.sam.topchef.feature_import_from_tiktok.model.TikTokData
import com.sam.topchef.feature_import_from_tiktok.presenter.TikTokImportPresenter
import com.sam.topchef.feature_import_from_tiktok.presenter.TikTokUICallBack
import com.sam.topchef.feature_import_from_tudogostoso.importer.TudoGostosoImporter.searchRecipe
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

    /**
     * Initializes the activity, sets up edge-to-edge display, binding,
     * presenter, and ExoPlayer. Initiates data fetching for a sample TikTok URL.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [onSaveInstanceState].
     */
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
                presenter.getTikTokData(url)
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
    }

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
        importRecipeWithIA(response)
    }

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
                    videoUrl = response.data.videoUrl
                ).also { updatedRecipe ->
                    Log.d("RecipeIA", "Recipe imported successfully: $updatedRecipe")
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

    private fun showRecipeDialog(recipe: com.sam.topchef.feature_import_from_tiktok.model.TikTokModel) {
        val dialog = TiktokRecipeDataDialog.newInstance(recipe)
        dialog.show(supportFragmentManager, TiktokRecipeDataDialog.TAG)
    }

    private fun messageLoadAnimation(start: Boolean) {
        if (start) {
            if (pulseAnimator == null) {
                val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.02f)
                val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.02f)
                pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(message, scaleX, scaleY).apply {
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

    private fun loadSavedTiktokRecipe(id: Int) {
        lifecycleScope.launch {
            val db = com.sam.topchef.core.data.local.appDataBase.AppDataBase.getDataBase(this@TiktokImportActivity)
            val recipe = withContext(Dispatchers.IO) {
                db.tiktokDao().getById(id)
            }
            if (recipe != null) {
                recipe.videoUrl?.let { url ->
                    val mediaItem = MediaItem.fromUri(url)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                }
                showRecipeDialog(recipe)
            }
        }
    }

    /**
     * Releases ExoPlayer resources when the activity is destroyed.
     */
    override fun onDestroy() {
        pulseAnimator?.cancel()
        player.release()
        super.onDestroy()
    }

    override fun onStop() {
        player.pause()
        super.onStop()
    }

    override fun onResume() {
        player.play()
        super.onResume()
    }
}