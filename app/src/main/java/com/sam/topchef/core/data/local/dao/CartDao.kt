package com.sam.topchef.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sam.topchef.core.data.model.Cart

@Dao
interface CartDao {
    @Insert
    fun insert(cart: Cart): Long

    @Query("DELETE FROM Cart WHERE id = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM  Cart")
    fun getAllCarts(): List<Cart>

    @Query("SELECT * FROM Cart WHERE id = :id")
    fun getCart(id: Int): Cart

    @Update
    fun update(cart: Cart)
}