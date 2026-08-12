package com.sam.topchef.feature_fullscreen_image

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.sam.topchef.databinding.ActivityFullscreenImageBinding

/**
 * Activity for displaying an image in full screen.
 * Used for detailed viewing of recipe photos.
 */
class FullscreenImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullscreenImageBinding
    /**
     * Initializes the fullscreen view and loads the specified image URI using Glide.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        

        val imageView = binding.fullImage
        val imageUri = intent.getStringExtra("imageUri")

        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(imageView)
        }
    }

}