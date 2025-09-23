package com.sam.topchef.core.data.local.appDataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sam.topchef.core.data.local.dao.RecipeDao
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.utils.ArrayConverter
import com.sam.topchef.core.utils.DateConverter

@Database(entities = [Recipe::class], version = 2)
@TypeConverters(DateConverter::class, ArrayConverter::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        private var INSTANCE: AppDataBase? = null

        fun getDataBase(context: Context): AppDataBase {
            return if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDataBase::class.java,
                        "top_chef"
                    ).build()
                }
                INSTANCE as AppDataBase
            } else {
                INSTANCE as AppDataBase
            }
        }
    }
}