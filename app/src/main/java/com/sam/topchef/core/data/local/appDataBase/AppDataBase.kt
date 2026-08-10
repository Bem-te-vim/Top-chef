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
import com.sam.topchef.core.data.local.dao.TiktokDao
import com.sam.topchef.core.data.local.dao.TypeDao
import com.sam.topchef.core.data.local.dao.UserDao
import com.sam.topchef.core.data.local.migration.DataBaseMigration.MIGRATION_13_14
import com.sam.topchef.core.data.local.migration.DataBaseMigration.MIGRATION_14_15
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.data.model.Type
import com.sam.topchef.core.data.model.User
import com.sam.topchef.feature_import_from_tiktok.model.TikTokModel
import com.sam.topchef.core.utils.ArrayCartItemConverter
import com.sam.topchef.core.utils.ArrayConverter
import com.sam.topchef.core.utils.DateConverter
import com.sam.topchef.core.utils.TiktokConverter

@Database(
    entities = [
        Recipe::class,
        Type::class,
        Cart::class,
        User::class,
        TikTokModel::class
    ], version = 15
)
@TypeConverters(
    DateConverter::class,
    ArrayConverter::class,
    ArrayCartItemConverter::class,
    TiktokConverter::class
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun typeDao(): TypeDao
    abstract fun cartDao(): CartDao
    abstract fun userDao(): UserDao

    abstract fun tiktokDao(): TiktokDao

    companion object {

        private var INSTANCE: AppDataBase? = null


        fun getDataBase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "top_chef"
                )
                    .addMigrations(MIGRATION_13_14, MIGRATION_14_15)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}