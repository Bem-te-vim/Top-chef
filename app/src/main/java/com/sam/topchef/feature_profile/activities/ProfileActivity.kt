package com.sam.topchef.feature_profile.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.sam.topchef.R
import com.sam.topchef.databinding.ActivityProfileBinding
import com.sam.topchef.feature_profile.adaper.ProfilePageAdapter

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        binding.btnBack.setOnClickListener { finish() }


        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = ProfilePageAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1

        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            tab.icon =  when (position){
                0 -> ContextCompat.getDrawable(this,R.drawable.grid_on_24dp)
                1 -> ContextCompat.getDrawable(this,R.drawable.round_favorite_border_24)
                else -> throw IllegalStateException()
            }
        }.attach()

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

}