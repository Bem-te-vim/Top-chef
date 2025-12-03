package com.sam.topchef.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sam.topchef.core.data.model.Cart

@Dao
interface CartDao {
    @Insert
    fun insert(cart: Cart): Long

    @Delete
    fun delete(cart: Cart): Int

    @Query("SELECT * FROM  Cart")
    fun getAllCarts(): List<Cart>

    @Query("SELECT * FROM Cart WHERE id = :id")
    fun getCart(id: Int): Cart

    @Update
    fun update(cart: Cart)
}