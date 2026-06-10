package com.sam.topchef.feature_import_recipe_from_tiktok.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.sam.topchef.R
import com.sam.topchef.core.utils.Utils.hide
import com.sam.topchef.core.utils.Utils.show
import com.sam.topchef.databinding.ActivityTiktokImporterBinding
import com.sam.topchef.feature_import_recipe_from_tiktok.model.TikTokData
import com.sam.topchef.feature_import_recipe_from_tiktok.presenter.TikTokImportPresenter
import com.sam.topchef.feature_import_recipe_from_tiktok.presenter.TikTokUICallBack

class TiktokImporterActivity : AppCompatActivity(), TikTokUICallBack {
    private lateinit var binding: ActivityTiktokImporterBinding
    private lateinit var presenter: TikTokImportPresenter
    private lateinit var player: ExoPlayer

    var tt: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTiktokImporterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        presenter = TikTokImportPresenter(this)
        presenter.getTikTokData(
            "https://www.tiktok.com/@maiara.silva017/video/7637698533939776775?is_from_webapp=1&sender_device=pc"
        )

        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player


    }

    override fun showData(response: TikTokData) {
        val mediaItem = MediaItem.fromUri(response.data.videoUrl)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    override fun showFailure(message: String) {
        binding.test.text = message
    }

    override fun showProgress() {
        binding.progressBarImport.show()
    }

    override fun hideProgress() {
        binding.progressBarImport.hide()
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}