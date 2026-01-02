package com.sam.topchef.feature_profile.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.databinding.ActivityProfileBinding
import com.sam.topchef.feature_profile.adaper.ProfilePageAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val include = binding.includeHeader
        include.btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            val recipesCount = withContext(Dispatchers.IO){
                (application as App).recipeDao.getAllRecipes()
            }

            include.recipesCount.text = recipesCount.size.toString()
            include.favoritesCount.text = recipesCount.filter { it.isFavorite }.size.toString()
        }


        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = ProfilePageAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(this, R.drawable.grid_on_24dp)
                1 -> ContextCompat.getDrawable(this, R.drawable.round_favorite_border_24)
                else -> throw IllegalStateException()
            }
        }.attach()

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


}