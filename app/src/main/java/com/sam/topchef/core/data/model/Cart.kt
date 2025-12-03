package com.sam.topchef.core.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sam.topchef.feature_shopping_list.data.model.CartItem

@Entity
data class Cart(
    @PrimaryKey(autoGenerate = true)

    @ColumnInfo(name = "id")
    var id: Int  = 0,

    @ColumnInfo("cart_image")
    val cartImage: String? = null,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "cart_items")
    val cartItems: List<CartItem> = emptyList()
)
