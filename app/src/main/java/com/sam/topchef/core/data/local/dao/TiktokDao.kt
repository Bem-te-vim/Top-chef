package com.sam.topchef.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sam.topchef.feature_import_from_tiktok.model.TikTokModel

@Dao
interface TiktokDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tiktokModel: TikTokModel)

    @Update
    fun update(tiktokModel: TikTokModel)

    @Query("SELECT * FROM Tiktok")
    fun getAll(): List<TikTokModel>

    @Query("SELECT * FROM Tiktok WHERE is_favorite = 1")
    fun getAllFavorites(): List<TikTokModel>

    @Query("SELECT * FROM Tiktok WHERE LOWER(name) LIKE '%' || LOWER(:search) || '%' OR LOWER(description) LIKE '%' || LOWER(:search) || '%'")
    fun search(search: String): List<TikTokModel>

    @Query("SELECT * FROM Tiktok WHERE id = :id")
    fun getById(id: Int): TikTokModel?

    @Query("DELETE FROM Tiktok WHERE id = :id")
    fun delete(id: Int)

}
