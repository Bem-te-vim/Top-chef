package com.sam.topchef.core.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DataBaseMigration {

    private fun SupportSQLiteDatabase.addColumnIfNotExists(tableName: String, columnName: String, columnDefinition: String) {
        var exists = false
        query("PRAGMA table_info(`$tableName`)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIndex != -1 && cursor.getString(nameIndex) == columnName) {
                    exists = true
                    break
                }
            }
        }
        if (!exists) {
            execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnDefinition")
        }
    }

    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `User` ")
            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS `User` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `name` TEXT,
                        `image_uri` TEXT
                    )
                    """
            )
        }
    }

    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `Tiktok` ")
            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS `Tiktok` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `is_favorite` INTEGER NOT NULL DEFAULT 0,
                        `thumbnail` TEXT,
                        `video_url` TEXT,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `ingredients` TEXT NOT NULL,
                        `preparation_mode` TEXT NOT NULL
                    )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfNotExists("Tiktok", "origin_url", "TEXT")
        }
    }

    val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfNotExists("Tiktok", "author_name", "TEXT")
            db.addColumnIfNotExists("Tiktok", "author_avatar", "TEXT")
            db.addColumnIfNotExists("Tiktok", "likes_count", "INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfNotExists("Tiktok", "type", "TEXT")
        }
    }

    val MIGRATION_18_19 = object : Migration(18, 19) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfNotExists("Tiktok", "local_video_path", "TEXT")
        }
    }

    val MIGRATION_19_20 = object : Migration(19, 20) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfNotExists("Tiktok", "local_thumbnail_path", "TEXT")
            db.addColumnIfNotExists("Tiktok", "local_author_avatar_path", "TEXT")
        }
    }

    val MIGRATION_20_21 = object : Migration(20, 21) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfNotExists("Recipe", "local_image_paths", "TEXT")
            db.addColumnIfNotExists("User", "local_image_uri", "TEXT")
            db.addColumnIfNotExists("Cart", "local_cart_image", "TEXT")
        }
    }

    val MIGRATION_21_22 = object : Migration(21, 22) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfNotExists("Recipe", "created_date", "INTEGER NOT NULL DEFAULT 0")
            db.addColumnIfNotExists("Cart", "cart_image", "TEXT")
        }
    }
}
