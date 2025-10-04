package com.sam.topchef.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sam.topchef.core.data.model.Type

@Dao
interface TypeDao {
    @Insert
    fun insert(type: Type)

    @Delete
    fun delete(type: Type): Int

    @Query("SELECT * FROM  Type")
    fun getAllTypes(): List<Type>
}