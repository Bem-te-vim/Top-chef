package com.sam.topchef.feature_shopping_list.adapter_interface

import android.view.View

interface AdapterChanges {
    fun onCartClick(id: Int)
    fun onCartImageClick(imageUri: String?, view: View)
    fun onCartTools(id: Int)
}