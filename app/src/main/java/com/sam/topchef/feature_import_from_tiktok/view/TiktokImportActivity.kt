package com.sam.topchef.feature_import_from_tiktok.view

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.google.android.material.snackbar.Snackbar
import com.sam.topchef.R
import com.sam.topchef.core.data.local.appDataBase.AppDataBase
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.core.utils.MediaDownloader
import com.sam.topchef.core.utils.Utils.clickAnimation
import com.sam.topchef.core.utils.Utils.hide
import com.sam.topchef.core.utils.Utils.setClicksListener
import com.sam.topchef.core.utils.Utils.shareText
import com.sam.topchef.core.utils.Utils.show
import com.sam.topchef.core.utils.Utils.toShareText
import com.sam.topchef.databinding.ActivityTiktokImportBinding
import com.sam.topchef.feature_feed_main.ui.activity.MainActivity
import com.sam.topchef.feature_import_from_tiktok.ia.RecipeInfoByIA
import com.sam.topchef.feature_import_from_tiktok.model.IgramMediaResponse
import com.sam.topchef.feature_import_from_tiktok.model.TikTokData
import com.sam.topchef.feature_import_from_tiktok.model.TikTokModel
import com.sam.topchef.feature_import_from_tiktok.offline.TiktokVideoDownloader
import com.sam.topchef.feature_import_from_tiktok.player.PlayerListener
import com.sam.topchef.feature_import_from_tiktok.presenter.IGramImportPresenter
import com.sam.topchef.feature_import_from_tiktok.presenter.IGramUICallBack
import com.sam.topchef.feature_import_from_tiktok.presenter.TikTokImportPresenter
import com.sam.topchef.feature_import_from_tiktok.presenter.TikTokUICallBack
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
class TiktokImportActivity : AppCompatActivity(), TikTokUICallBack, IGramUICallBack {
    private lateinit var binding: ActivityTiktokImportBinding
    private lateinit var tikwmPresenter: TikTokImportPresenter
    private lateinit var igramPresenter: IGramImportPresenter
    private lateinit var player: ExoPlayer

    private lateinit var message: TextView
    private val recipeInfoByIA by lazy { RecipeInfoByIA() }
    private var pulseAnimator: ObjectAnimator? = null
    private var currentOriginUrl: String? = null
    private var isRefreshing = false
    private var currentRecipeId: Int? = null

    private var currentTikTokModel: TikTokModel? = null

    private var isImportMode = false
    private lateinit var playerListener: PlayerListener
    private var isUserSeeking = false

    private val videoDownloader by lazy { TiktokVideoDownloader(this) }

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

        setupOnBackPressed()
        setupWindowInsets()
        setupPlayer()
        setupListeners()
        setupPresenter()
        handleIntent()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent()
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isImportMode) {
                    val intent = Intent(this@TiktokImportActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra(MainActivity.EXTRA_RELOAD, true)
                    startActivity(intent)
                    finish()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.containerBottomInfo.updateLayoutParams<MarginLayoutParams> {
                val baseMargin = (12 * resources.displayMetrics.density).toInt()
                bottomMargin = systemBars.bottom + baseMargin
            }

            binding.containerSidePanel.updateLayoutParams<MarginLayoutParams> {
                val baseMargin = (12 * resources.displayMetrics.density).toInt()
                bottomMargin = systemBars.bottom + baseMargin
            }

            binding.videoProgress.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }

            insets
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer() {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36")
            .setDefaultRequestProperties(mapOf("Referer" to "https://www.tiktok.com/"))
            .setAllowCrossProtocolRedirects(true)

        val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

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
            onDoubleClick = { _, x, y ->
                showHeartAnimation(x, y)
                binding.btnFavorite.clickAnimation(startAnimationScale = 0.90f)
                // Only favorites (curtir). Unlike (descurtir) is only via btn_favorite.
                saveFavoriteToDatabase(true)
            },
            onHold = {
                player.setPlaybackSpeed(2f)
                binding.message.show()
                binding.message.text = getString(R.string._2x_speed)
                if (!player.isPlaying) {
                    player.play()
                }
            },
            onRelease = {
                player.setPlaybackSpeed(1f)
                binding.message.hide()
            }
        )

        player.addListener(playerListener)

        playerListener.onPlayerError {
            val cause = it.cause
            val localPath = currentTikTokModel?.localVideoPath
            val localFile = if (!localPath.isNullOrEmpty()) File(localPath) else null

            if (localFile != null && localFile.exists() && localFile.length() > 0) {
                Log.d("TiktokImport", "Player error on remote stream, falling back to local file")
                val mediaItem = MediaItem.fromUri(Uri.fromFile(localFile))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            } else if (cause is HttpDataSource.InvalidResponseCodeException && cause.responseCode == 403) {
                handle403Error()
            }
        }

        playerListener.isPlaying {
            showPlayerIc(it)
            if (it) startProgressUpdate() else stopProgressUpdate()
        }

        setupProgressBar()
    }

    private fun setupProgressBar() {
        binding.videoProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val duration = player.duration
                    if (duration > 0) {
                        val newPosition = (progress * duration) / 1000
                        player.seekTo(newPosition)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
            }
        })
    }

    private fun setupListeners() {
        /** video interactions **/
        binding.btnFavorite.setOnClickListener {
            it.clickAnimation(startAnimationScale = 0.90f)
            toggleFavorite()
        }

        binding.btnCart.setOnClickListener {
            it.clickAnimation(startAnimationScale = 0.90f)
            dialogMoveToCart()
        }

        binding.btnShare.setOnClickListener {
            it.clickAnimation(startAnimationScale = 0.90f)
            share()
        }

        binding.btnReopenDialog.setOnClickListener {
            it.clickAnimation(startAnimationScale = 0.90f)
            currentTikTokModel?.let { recipe ->
                showRecipeDialog(recipe)
            } ?: run {
                Toast.makeText(this, "Nenhuma receita carregada ainda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPresenter() {
        tikwmPresenter = TikTokImportPresenter(this)
        igramPresenter = IGramImportPresenter(this)
        message = binding.message
    }

    private fun handleIntent() {
        val sharedText = intent.getStringExtra("urlPath")
        val tiktokId = intent.getIntExtra("tiktokId", -1)

        if (tiktokId != -1) {
            isImportMode = false
            loadSavedTiktokRecipe(tiktokId)
        } else if (sharedText != null) {
            isImportMode = true
            val url = extractUrlFromSharedText(sharedText)
            if (url != null) {
                currentOriginUrl = url
                lifecycleScope.launch {
                    val db = AppDataBase.getDataBase(this@TiktokImportActivity)
                    val existing = withContext(Dispatchers.IO) {
                        db.tiktokDao().getByUrl(url)
                    }
                    if (existing != null) {
                        currentRecipeId = existing.id
                        loadSavedTiktokRecipe(existing.id)
                    } else {
                        if (url.contains("instagram.com")) {
                            igramPresenter.getIGramData(url)
                        } else {
                            tikwmPresenter.getTikTokData(url)
                        }
                    }
                }
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
    fun share() {
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
    fun dialogMoveToCart() {
        AlertDialog.Builder(this)
            .setTitle("Mover Items Para o Carrinho?")
            .setPositiveButton("Mover") { p0, _ ->
                moveToCart()
                p0.dismiss()
            }
            .setNegativeButton("Cancelar") { p0, _ ->
                p0.dismiss()
            }.show()
    }

    fun moveToCart() {
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
                    Toast.makeText(
                        this@TiktokImportActivity,
                        "Ingredientes movidos para o carrinho!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@TiktokImportActivity, ShoppingListActivity::class.java)
                    intent.putExtra("id", cartId)
                    startActivity(intent)
                }
            }
        } ?: run {
            Toast.makeText(this, "Aguarde o carregamento da receita", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPlayerIc(isPlaying: Boolean) {
        if (isPlaying) {
            binding.playIc.hide()
        } else {
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
     * Updates the video player's source URL and metadata for a specific recipe in the database.
     * @param id The ID of the TikTok recipe.
     * @param data The fresh data from TikTok API.
     */
    private fun updateVideoMetadataInDb(id: Int, data: com.sam.topchef.feature_import_from_tiktok.model.Data) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDataBase.getDataBase(this@TiktokImportActivity)
            val recipe = db.tiktokDao().getById(id)
            if (recipe != null) {
                val updated = recipe.copy(
                    videoUrl = data.videoUrl,
                    authorName = data.author.name,
                    authorAvatar = data.author.avatar,
                    likesCount = data.likesCount
                )
                db.tiktokDao().update(updated)
                withContext(Dispatchers.Main) {
                    if (currentRecipeId == id) {
                        currentTikTokModel = updated
                        updateVideoInfoUI(updated)
                        lifecycleScope.launch {
                            videoDownloader.downloadAndSaveVideo(updated) { model ->
                                currentTikTokModel = model
                            }
                        }
                    }
                }
                Log.d("TiktokImport", "Updated video metadata in DB for recipe $id")
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
                val audioFile = recipeInfoByIA.downloadAudio(response.data.videoAudioFile, tempFile)

                val recipe = recipeInfoByIA.importRecipe(listOf(response.data.title), audioFile)

                val savedRecipe = withContext(Dispatchers.IO) {
                    val db = AppDataBase.getDataBase(this@TiktokImportActivity)
                    val existing = currentOriginUrl?.let { db.tiktokDao().getByUrl(it) }
                    val toInsert = if (existing != null) {
                        recipe.copy(
                            id = existing.id,
                            thumbnail = response.data.thumbnail,
                            videoUrl = response.data.videoUrl,
                            originUrl = currentOriginUrl,
                            authorName = response.data.author.name,
                            authorAvatar = response.data.author.avatar,
                            likesCount = response.data.likesCount,
                            localVideoPath = existing.localVideoPath
                        )
                    } else {
                        recipe.copy(
                            thumbnail = response.data.thumbnail,
                            videoUrl = response.data.videoUrl,
                            originUrl = currentOriginUrl,
                            authorName = response.data.author.name,
                            authorAvatar = response.data.author.avatar,
                            likesCount = response.data.likesCount
                        )
                    }

                    toInsert.type?.let { typeName ->
                        if (!db.typeDao().getAllTypeNames().contains(typeName)) {
                            db.typeDao().insert(com.sam.topchef.core.data.model.Type(type = typeName))
                        }
                    }

                    if (existing != null) {
                        db.tiktokDao().update(toInsert)
                        toInsert
                    } else {
                        db.tiktokDao().insert(toInsert)
                        db.tiktokDao().getByUrl(currentOriginUrl ?: "") ?: toInsert
                    }
                }

                currentTikTokModel = savedRecipe
                currentRecipeId = savedRecipe.id
                message.visibility = View.GONE
                messageLoadAnimation(false)

                updateFavoriteUI()
                updateVideoInfoUI(savedRecipe)
                showRecipeDialog(savedRecipe)

                lifecycleScope.launch {
                    videoDownloader.downloadAndSaveVideo(savedRecipe) { model ->
                        currentTikTokModel = model
                    }
                }

            } catch (e: Exception) {
                messageLoadAnimation(false)
                Log.e("RecipeIA", "Error importing recipe with IA", e)

                val errorMessage = when {
                    e.message?.contains("high demand", ignoreCase = true) == true ->
                        "O modelo da IA está com alta demanda no momento. Por favor, tente novamente mais tarde.\nDetalhes: ${e.message}"
                    else -> e.message ?: "Erro ao converter receita: ${e.javaClass.simpleName}"
                }

                message.visibility = View.VISIBLE
                message.text = errorMessage

                Snackbar.make(
                    binding.root,
                    errorMessage,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Tentar Novamente") {
                        importRecipeWithIA(response)
                    }.show()
            }
        }
    }

    /**
     * Displays a dialog showing the parsed/imported recipe data for user confirmation.
     * @param recipe The imported TikTok recipe data to show.
     */
    fun showRecipeDialog(recipe: TikTokModel) {
        if (isFinishing || isDestroyed || supportFragmentManager.isStateSaved) return
        try {
            val prev = supportFragmentManager.findFragmentByTag(TiktokRecipeDataDialog.TAG)
            if (prev != null) {
                (prev as? androidx.fragment.app.DialogFragment)?.dismissAllowingStateLoss()
            }
            val dialog = TiktokRecipeDataDialog.newInstance(recipe)
            dialog.show(supportFragmentManager, TiktokRecipeDataDialog.TAG)
        } catch (e: Exception) {
            Log.e("TiktokImportActivity", "Error showing recipe dialog", e)
        }
    }

    fun pausePlayer() {
        if (::player.isInitialized && player.isPlaying) {
            player.pause()
        }
    }

    fun onRecipeUpdated(updatedRecipe: TikTokModel) {
        lifecycleScope.launch {
            val updated = withContext(Dispatchers.IO) {
                val db = AppDataBase.getDataBase(this@TiktokImportActivity)
                updatedRecipe.type?.let { typeName ->
                    if (!db.typeDao().getAllTypeNames().contains(typeName)) {
                        db.typeDao().insert(com.sam.topchef.core.data.model.Type(type = typeName))
                    }
                }
                val existing = if (updatedRecipe.id != 0) {
                    db.tiktokDao().getById(updatedRecipe.id)
                } else {
                    updatedRecipe.originUrl?.let { url -> db.tiktokDao().getByUrl(url) }
                }

                var localThumbPath = existing?.localThumbnailPath
                val thumbnailUri = updatedRecipe.thumbnail
                if (!thumbnailUri.isNullOrEmpty()) {
                    val imagesDir = File(filesDir, "tiktok_images")
                    if (!imagesDir.exists()) imagesDir.mkdirs()
                    val targetThumbFile = File(imagesDir, "tiktok_thumb_${existing?.id ?: System.currentTimeMillis()}.jpg")
                    val downloader = MediaDownloader(this@TiktokImportActivity)
                    val success = downloader.downloadOrCopyImage(thumbnailUri, targetThumbFile)
                    if (success && targetThumbFile.exists() && targetThumbFile.length() > 0) {
                        localThumbPath = targetThumbFile.absolutePath
                    }
                }

                var result = if (existing != null) {
                    updatedRecipe.copy(
                        id = existing.id,
                        localVideoPath = existing.localVideoPath ?: updatedRecipe.localVideoPath,
                        localThumbnailPath = localThumbPath,
                        localAuthorAvatarPath = existing.localAuthorAvatarPath
                    )
                } else updatedRecipe.copy(localThumbnailPath = localThumbPath)

                if (existing != null) {
                    db.tiktokDao().update(result)
                } else {
                    db.tiktokDao().insert(result)
                    result = result.originUrl?.let { db.tiktokDao().getByUrl(it) } ?: result
                }
                result
            }
            currentTikTokModel = updated
            updateFavoriteUI()
            updateVideoInfoUI(updated)
            showRecipeDialog(updated)
            lifecycleScope.launch {
                videoDownloader.downloadAndSaveVideo(updated) { model ->
                    currentTikTokModel = model
                }
            }
        }
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
     * Updates the UI with the fetched TikTok data, specifically setting
     * and playing the video URL in the ExoPlayer.
     *
     * @param response The data object containing TikTok video information.
     */
    override fun showData(response: TikTokData) {
        lifecycleScope.launch {
            val db = AppDataBase.getDataBase(this@TiktokImportActivity)
            val existing = withContext(Dispatchers.IO) {
                currentOriginUrl?.let { db.tiktokDao().getByUrl(it) }
            }

            if (existing != null) {
                currentTikTokModel = existing
                currentRecipeId = existing.id
                playVideoForRecipe(existing)
            } else {
                val mediaItem = MediaItem.fromUri(response.data.videoUrl)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            }

            updateVideoInfoUI(
                authorName = response.data.author.name,
                authorAvatar = response.data.author.avatar,
                likesCount = response.data.likesCount,
                name = response.data.title,
                description = null
            )

            Log.i("TTKResponse", response.toString())

            if (isRefreshing && currentRecipeId != null) {
                updateVideoMetadataInDb(currentRecipeId!!, response.data)
            } else if (!isRefreshing && existing == null) {
                importRecipeWithIA(response)
            }
            isRefreshing = false
        }
    }

    // =========================================================================
    // PRESENTER CALLBACKS (TikTok & Instagram)
    // =========================================================================

    private fun getLowResVideoUrl(response: IgramMediaResponse): String? {
        val formatUrl = response.formats
            ?.filter { !it.id.isNullOrEmpty() && it.id.startsWith("mp4") && !it.url.isNullOrEmpty() }
            ?.minByOrNull { format ->
                format.id?.filter { it.isDigit() }?.toIntOrNull() ?: Int.MAX_VALUE
            }?.url
        return formatUrl ?: response.videoUrl
    }

    private fun getDashAudioUrl(response: IgramMediaResponse): String? {
        val dashFormat = response.formats
            ?.filter { !it.id.isNullOrEmpty() && it.id.contains("dash") && (!it.audioUrl.isNullOrEmpty() || !it.url.isNullOrEmpty()) }
            ?.minByOrNull { format ->
                format.id?.filter { it.isDigit() }?.toIntOrNull() ?: Int.MAX_VALUE
            }
        return dashFormat?.audioUrl ?: dashFormat?.url ?: response.videoUrl
    }

    override fun showData(response: IgramMediaResponse) {
        lifecycleScope.launch {
            val db = AppDataBase.getDataBase(this@TiktokImportActivity)
            val existing = withContext(Dispatchers.IO) {
                currentOriginUrl?.let { db.tiktokDao().getByUrl(it) }
            }

            val videoToPlay = getLowResVideoUrl(response)
            if (existing != null) {
                currentTikTokModel = existing
                currentRecipeId = existing.id
                playVideoForRecipe(existing)
            } else if (!videoToPlay.isNullOrEmpty()) {
                val mediaItem = MediaItem.fromUri(videoToPlay)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            }

            updateVideoInfoUI(
                authorName = response.author?.username,
                authorAvatar = response.author?.profilePicture,
                likesCount = response.likesCount ?: 0,
                name = response.caption ?: "Instagram Recipe",
                description = null
            )

            Log.i("IGramResponse", response.toString())

            if (!isRefreshing && existing == null) {
                importInstagramRecipeWithIA(response)
            }
            isRefreshing = false
        }
    }

    private fun importInstagramRecipeWithIA(response: IgramMediaResponse) {
        lifecycleScope.launch {
            try {
                message.visibility = View.VISIBLE
                messageLoadAnimation(true)
                message.text = getString(R.string.load_info)

                val audioUrlToDownload = getDashAudioUrl(response)
                val audioFile = if (!audioUrlToDownload.isNullOrEmpty()) {
                    val tempFile = File(cacheDir, "temp_audio_igram.mp3")
                    recipeInfoByIA.downloadAudio(audioUrlToDownload, tempFile)
                } else {
                    null
                }

                val recipe = if (audioFile != null) {
                    recipeInfoByIA.importRecipe(listOf(response.caption ?: "Instagram Recipe"), audioFile)
                } else {
                    TikTokModel(
                        name = response.caption ?: "Instagram Recipe",
                        description = "",
                        ingredients = emptyList(),
                        preparationMode = emptyList()
                    )
                }

                val videoToSave = getLowResVideoUrl(response) ?: response.videoUrl
                val savedRecipe = withContext(Dispatchers.IO) {
                    val db = AppDataBase.getDataBase(this@TiktokImportActivity)
                    val existing = currentOriginUrl?.let { db.tiktokDao().getByUrl(it) }
                    val toInsert = if (existing != null) {
                        recipe.copy(
                            id = existing.id,
                            thumbnail = response.thumbnail,
                            videoUrl = videoToSave,
                            originUrl = currentOriginUrl,
                            authorName = response.author?.username,
                            authorAvatar = response.author?.profilePicture,
                            likesCount = response.likesCount ?: 0,
                            localVideoPath = existing.localVideoPath
                        )
                    } else {
                        recipe.copy(
                            thumbnail = response.thumbnail,
                            videoUrl = videoToSave,
                            originUrl = currentOriginUrl,
                            authorName = response.author?.username,
                            authorAvatar = response.author?.profilePicture,
                            likesCount = response.likesCount ?: 0
                        )
                    }

                    toInsert.type?.let { typeName ->
                        if (!db.typeDao().getAllTypeNames().contains(typeName)) {
                            db.typeDao().insert(com.sam.topchef.core.data.model.Type(type = typeName))
                        }
                    }

                    if (existing != null) {
                        db.tiktokDao().update(toInsert)
                        toInsert
                    } else {
                        db.tiktokDao().insert(toInsert)
                        db.tiktokDao().getByUrl(currentOriginUrl ?: "") ?: toInsert
                    }
                }

                currentTikTokModel = savedRecipe
                currentRecipeId = savedRecipe.id
                message.visibility = View.GONE
                messageLoadAnimation(false)

                updateFavoriteUI()
                updateVideoInfoUI(savedRecipe)
                showRecipeDialog(savedRecipe)

                lifecycleScope.launch {
                    videoDownloader.downloadAndSaveVideo(savedRecipe) { model ->
                        currentTikTokModel = model
                    }
                }

            } catch (e: Exception) {
                messageLoadAnimation(false)
                Log.e("RecipeIA", "Error importing Instagram recipe with IA", e)

                val errorMessage = e.message ?: "Erro ao converter receita do Instagram"
                message.visibility = View.VISIBLE
                message.text = errorMessage

                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Tentar Novamente") {
                        importInstagramRecipeWithIA(response)
                    }.show()
            }
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

                playVideoForRecipe(recipe)

                updateFavoriteUI()
                updateVideoInfoUI(recipe)
                showRecipeDialog(recipe)
            }
        }
    }

    private fun playVideoForRecipe(recipe: TikTokModel) {
        val localPath = recipe.localVideoPath
        val localFile = if (!localPath.isNullOrEmpty()) File(localPath) else null

        if (localFile != null && localFile.exists() && localFile.length() > 0) {
            Log.d("TiktokImport", "Playing video from local file: ${localFile.absolutePath}")
            val mediaItem = MediaItem.fromUri(Uri.fromFile(localFile))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        } else if (!recipe.videoUrl.isNullOrEmpty()) {
            Log.d("TiktokImport", "Playing video from remote URL: ${recipe.videoUrl}")
            val mediaItem = MediaItem.fromUri(recipe.videoUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            lifecycleScope.launch {
                videoDownloader.downloadAndSaveVideo(recipe) { model ->
                    currentTikTokModel = model
                }
            }
        } else {
            Toast.makeText(this, "Vídeo não disponível offline.", Toast.LENGTH_SHORT).show()
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
        tikwmPresenter.getTikTokData(currentOriginUrl!!)
    }

    private fun showHeartAnimation(x: Float, y: Float) {
        val heart = binding.favoriteIc
        heart.animate().cancel()

        heart.x = x - heart.width / 2
        heart.y = y - heart.height / 2

        heart.scaleX = 0f
        heart.scaleY = 0f
        heart.alpha = 1f
        heart.show()

        heart.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .withEndAction {
                heart.animate()
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction { heart.hide() }
            }
    }

    private fun toggleFavorite() {
        val model = currentTikTokModel ?: return
        val newStatus = !model.isFavorite
        saveFavoriteToDatabase(newStatus)
    }

    private fun saveFavoriteToDatabase(isFavorite: Boolean = true) {
        val recipe = currentTikTokModel ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDataBase.getDataBase(this@TiktokImportActivity)
            val tiktokDao = db.tiktokDao()

            val existing = recipe.originUrl?.let { tiktokDao.getByUrl(it) } ?: tiktokDao.getById(recipe.id)
            val toUpdate = (existing ?: recipe).copy(
                isFavorite = isFavorite,
                localVideoPath = existing?.localVideoPath ?: recipe.localVideoPath
            )

            if (existing != null || recipe.id != 0) {
                tiktokDao.update(toUpdate)
            } else {
                tiktokDao.insert(toUpdate)
            }

            val finalModel = recipe.originUrl?.let { tiktokDao.getByUrl(it) } ?: tiktokDao.getById(toUpdate.id) ?: toUpdate
            withContext(Dispatchers.Main) {
                currentTikTokModel = finalModel
                updateFavoriteUI()
                updateVideoInfoUI(finalModel)
                lifecycleScope.launch {
                    videoDownloader.downloadAndSaveVideo(finalModel) { model ->
                        currentTikTokModel = model
                    }
                }
            }
        }
    }

    private fun updateFavoriteUI() {
        val isFavorite = currentTikTokModel?.isFavorite ?: false
        if (isFavorite) {
            binding.btnFavorite.clearColorFilter()
            binding.btnFavorite.setImageResource(R.drawable.favorite_svgrepo_com)
        } else {
            binding.btnFavorite.setColorFilter(getColor(R.color.WhiteForTxt))
            binding.btnFavorite.setImageResource(R.drawable.favorite_heart_love_svgrepo_com)
        }
    }

    private fun updateVideoInfoUI(recipe: TikTokModel) {
        updateVideoInfoUI(
            authorName = recipe.authorName,
            authorAvatar = recipe.authorAvatar,
            likesCount = recipe.likesCount,
            name = recipe.name,
            description = recipe.description
        )
    }

    private fun updateVideoInfoUI(
        authorName: String?,
        authorAvatar: String?,
        likesCount: Int,
        name: String,
        description: String?
    ) {
        binding.txtUsername.text = authorName?.let { "@${it.replace(" ", "_").lowercase()}" } ?: "@topchef_creator"
        binding.txtDescription.text = if (description.isNullOrEmpty()) name else "$name - $description"

        binding.txtLikesCount.text = formatLikesCount(likesCount)

        authorAvatar?.let {
            LoadImages().loadImagesWithBlur(it, binding.imgProfile)
        }
    }

    private fun formatLikesCount(count: Int): String {
        return when {
            count >= 1000000 -> String.format(java.util.Locale.US, "%.1fM", count / 1000000.0)
            count >= 1000 -> String.format(java.util.Locale.US, "%.1fK", count / 1000.0)
            else -> count.toString()
        }
    }

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (::player.isInitialized && player.isPlaying && !isUserSeeking) {
                val currentPos = player.currentPosition
                val duration = player.duration
                if (duration > 0) {
                    binding.videoProgress.progress = ((currentPos * 1000) / duration).toInt()
                }
            }
            binding.videoProgress.postDelayed(this, 100)
        }
    }

    private fun startProgressUpdate() {
        binding.videoProgress.removeCallbacks(progressRunnable)
        binding.videoProgress.post(progressRunnable)
    }

    private fun stopProgressUpdate() {
        binding.videoProgress.removeCallbacks(progressRunnable)
    }

    /**
     * Releases ExoPlayer resources when the activity is destroyed.
     */
    override fun onDestroy() {
        pulseAnimator?.cancel()
        if (::player.isInitialized) {
            player.release()
        }
        recipeInfoByIA.close()
        super.onDestroy()
    }

    /**
     * Pauses the player when the activity is stopped.
     */
    override fun onStop() {
        if (::player.isInitialized) {
            player.pause()
        }
        super.onStop()
    }

    /**
     * Resumes video playback when the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        if (::player.isInitialized && player.mediaItemCount > 0) {
            player.play()
        }
    }
}