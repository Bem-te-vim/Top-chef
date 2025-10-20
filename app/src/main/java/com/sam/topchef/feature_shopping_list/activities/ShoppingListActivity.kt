package com.sam.topchef.feature_shopping_list.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sam.topchef.databinding.ActivityShoppingListBinding

class ShoppingListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShoppingListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}