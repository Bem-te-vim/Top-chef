package com.sam.topchef.fature_import_recipe.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sam.topchef.databinding.ActivityAddRecipeBinding
import com.sam.topchef.fature_import_recipe.importer.TudoGostosoImporter
import kotlinx.coroutines.launch

class ImportRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddRecipeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (url != null) {
            lifecycleScope.launch {
                val recipe = TudoGostosoImporter().import(url)
                Log.d("IMPORT", recipe.toString())
                Toast.makeText(this@ImportRecipeActivity, recipe.toString(), Toast.LENGTH_SHORT).show()
            }

        }


    }
}