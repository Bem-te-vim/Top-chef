package com.sam.topchef.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sam.topchef.core.data.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM User WHERE id = 1 LIMIT 1")
    fun getUser(): User?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUser(user: User)

}