package com.sam.topchef.core.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sam.topchef.feature_shopping_list.data.model.CartItem

object ArrayCartItemConverter {
    @TypeConverter
    fun fromCartItemList(value: List<CartItem>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCartItemList(value: String): List<CartItem> {
        val listType = object : TypeToken<List<CartItem>>() {}.type
        return Gson().fromJson(value, listType)
    }
}