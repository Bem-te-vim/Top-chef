package com.sam.topchef.feature_profile.adaper

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sam.topchef.feature_profile.fragments.AllRecipesFragment
import com.sam.topchef.feature_profile.fragments.FavoriteRecipeFragment
import com.sam.topchef.feature_profile.fragments.WebRecipesFragment

class ProfilePageAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
      return when(position){
          0 -> AllRecipesFragment()
          1 -> FavoriteRecipeFragment()
          2 -> WebRecipesFragment()
          else -> throw IllegalStateException()
      }
    }

    override fun getItemCount(): Int = 3
}