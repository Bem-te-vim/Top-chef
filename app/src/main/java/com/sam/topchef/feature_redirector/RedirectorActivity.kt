package com.sam.topchef.feature_redirector

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.sam.topchef.R
import com.sam.topchef.databinding.ActivityRedirectorBinding

import com.sam.topchef.feature_import_from_tudogostoso.activities.TudoGostosoImportActivity
import com.sam.topchef.feature_import_from_tiktok.view.TiktokImportActivity

class RedirectorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRedirectorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRedirectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        redirectUrl(sharedUrl)
    }

    private fun redirectUrl(url: String?) {
        if (url == null) return

        val intent = when {
            url.contains("tiktok.com") -> {
                Intent(this, TiktokImportActivity::class.java)
            }
            url.contains("tudogostoso.com") -> {
                Intent(this, TudoGostosoImportActivity::class.java)
            }
            else -> null
        }

        intent?.let {
            it.putExtra("urlPath", url)
            startActivity(it)
            finish()
        }
    }
}