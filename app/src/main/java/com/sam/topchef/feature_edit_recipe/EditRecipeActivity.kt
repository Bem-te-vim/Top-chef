package com.sam.topchef.feature_edit_recipe

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.sam.topchef.R
import com.sam.topchef.databinding.ActivityAddRecipeBinding

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)
        enableEdgeToEdge()
    }
}