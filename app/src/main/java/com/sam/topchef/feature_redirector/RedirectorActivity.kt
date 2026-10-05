package com.sam.topchef.feature_redirector

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sam.topchef.R
import com.sam.topchef.databinding.ActivityRedirectorBinding
import com.sam.topchef.feature_feed_main.ui.activity.MainActivity
import com.sam.topchef.feature_import_from_tiktok.view.TiktokImportActivity
import com.sam.topchef.feature_import_from_tudogostoso.activities.TudoGostosoImportActivity

/**
 * Entry point for shared content from other apps.
 * Analyzes the incoming URL (e.g., from TikTok or browser) and redirects the user
 * to the appropriate import activity in a single application task.
 */
class RedirectorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRedirectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRedirectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        handleShareIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        val sharedUrl = intent?.getStringExtra(Intent.EXTRA_TEXT)
        redirectUrl(sharedUrl)
    }

    private fun redirectUrl(url: String?) {
        if (url.isNullOrBlank()) {
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(mainIntent)
            finish()
            return
        }

        val targetIntent = when {
            url.contains("tiktok.com") -> {
                Intent(this, TiktokImportActivity::class.java)
            }
            url.contains("tudogostoso.com") -> {
                Intent(this, TudoGostosoImportActivity::class.java)
            }
            else -> {
                null
            }
        }

        if (targetIntent != null) {
            targetIntent.putExtra("urlPath", url)
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(targetIntent)
        } else {
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(mainIntent)
        }
        finish()
    }
}
