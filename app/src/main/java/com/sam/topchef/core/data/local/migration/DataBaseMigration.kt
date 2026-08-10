package com.sam.topchef.core.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DataBaseMigration {

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
}