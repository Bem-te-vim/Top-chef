package com.sam.topchef.core.data.local.appDataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sam.topchef.core.data.local.dao.CartDao
import com.sam.topchef.core.data.local.dao.RecipeDao
import com.sam.topchef.core.data.local.dao.TypeDao
import com.sam.topchef.core.data.local.dao.UserDao
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.data.model.Type
import com.sam.topchef.core.data.model.User
import com.sam.topchef.core.utils.ArrayCartItemConverter
import com.sam.topchef.core.utils.ArrayConverter
import com.sam.topchef.core.utils.DateConverter

@Database(
    entities = [
        Recipe::class,
        Type::class,
        Cart::class,
        User::class
    ], version = 14
)
@TypeConverters(DateConverter::class, ArrayConverter::class, ArrayCartItemConverter::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun typeDao(): TypeDao
    abstract fun cartDao(): CartDao
    abstract fun UserDao(): UserDao

    companion object {

        private var INSTANCE: AppDataBase? = null

        val MIGRATION_1_2 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `User` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT,
                        `image_uri` TEXT
                    )
                    """
                )
            }
        }

        fun getDataBase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "top_chef"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}