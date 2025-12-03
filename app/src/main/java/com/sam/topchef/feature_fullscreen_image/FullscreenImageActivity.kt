package com.sam.topchef.feature_fullscreen_image

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.sam.topchef.databinding.ActivityFullscreenImageBinding

class FullscreenImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullscreenImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

//        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
//            duration = 300
//            scrimColor = Color.TRANSPARENT
//        }
//
//        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
//            duration = 250
//            scrimColor = Color.TRANSPARENT
//        }

        val imageView = binding.fullImage
        val imageUri = intent.getStringExtra("imageUri")

        if (imageUri != null)
        Glide.with(this)
            .load(imageUri)
            .into(imageView)

    }

}