package com.sam.topchef.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sam.topchef.core.data.model.Recipe

@Dao
interface RecipeDao {

    @Insert
    fun insert(recipe: Recipe)

    @Delete
    fun delete(recipe: Recipe): Int

    @Update
    fun update(recipe: Recipe)

    @Query("SELECT * FROM Recipe WHERE id = :id")
    fun getRecipe(id: Int): Recipe

    @Query("SELECT * FROM Recipe")
    fun getAllRecipes(): List<Recipe>

}