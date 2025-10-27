package com.sam.topchef.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sam.topchef.core.data.model.Recipe

@Dao
interface RecipeDao {

    @Insert
    fun insert(recipe: Recipe)

    @Update
    fun update(recipe: Recipe)

    @Query("SELECT * FROM Recipe WHERE LOWER(title) LIKE '%' || LOWER(:search) || '%' OR LOWER(type) LIKE '%' || LOWER(:search) || '%'")
    fun search(search: String): List<Recipe>

    @Query("DELETE FROM Recipe WHERE id = :id")
    fun delete(id: Int): Int

    @Query("SELECT * FROM Recipe WHERE id = :id")
    fun getRecipe(id: Int): Recipe?

    @Query("SELECT * FROM Recipe")
    fun getAllRecipes(): List<Recipe>

}