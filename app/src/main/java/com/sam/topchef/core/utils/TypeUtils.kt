package com.sam.topchef.core.utils

import com.sam.topchef.core.data.local.appDataBase.AppDataBase
import com.sam.topchef.core.data.model.Type
import java.util.Locale

object TypeUtils {

    /**
     * Finds an existing type (case-insensitive & trimmed) or formats and inserts a new one.
     * Returns the canonical type name to be saved on the recipe.
     */
    fun findOrInsertType(db: AppDataBase, rawTypeName: String?): String? {
        if (rawTypeName.isNullOrBlank()) return null

        val trimmed = rawTypeName.trim().trim('"', '\'')
        if (trimmed.isEmpty()) return null

        val typeDao = db.typeDao()
        val recipeDao = db.recipeDao()
        val tiktokDao = db.tiktokDao()

        val allExistingTypes = (typeDao.getAllTypeNames() +
                recipeDao.getAllCategoryNames() +
                tiktokDao.getAllCategoryNames())
            .filter { !it.isNullOrBlank() }
            .map { it.trim() }
            .distinct()

        val matchedType = allExistingTypes.firstOrNull {
            it.equals(trimmed, ignoreCase = true)
        }

        return if (matchedType != null) {
            matchedType
        } else {
            val formattedNewType = trimmed.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            val existingInTypeDao = typeDao.getAllTypeNames().any {
                it.trim().equals(formattedNewType, ignoreCase = true)
            }
            if (!existingInTypeDao) {
                typeDao.insert(Type(type = formattedNewType))
            }
            formattedNewType
        }
    }

    /**
     * Cleans up existing duplicate types in the database by merging case-insensitive duplicates
     * and updating recipes and types tables to use canonical category names.
     */
    fun cleanUpDuplicateTypes(db: AppDataBase) {
        val typeDao = db.typeDao()
        val recipeDao = db.recipeDao()
        val tiktokDao = db.tiktokDao()

        val allTypesInDb = typeDao.getAllTypes()
        val typeGrouped = allTypesInDb.groupBy { it.type.trim().lowercase(Locale.getDefault()) }

        typeGrouped.forEach { (_, types) ->
            if (types.size > 1) {
                val canonical = types.map { it.type.trim() }
                    .maxByOrNull { if (it.firstOrNull()?.isUpperCase() == true) 2 else 1 }
                    ?: types.first().type.trim()

                val toDelete = types.drop(1)
                toDelete.forEach { typeDao.delete(it) }

                val recipes = recipeDao.getAllRecipes()
                recipes.forEach { recipe ->
                    if (recipe.type != null && recipe.type.trim().equals(canonical, ignoreCase = true) && recipe.type != canonical) {
                        recipeDao.update(recipe.copy(type = canonical))
                    }
                }

                val tiktoks = tiktokDao.getAll()
                tiktoks.forEach { tiktok ->
                    if (tiktok.type != null && tiktok.type.trim().equals(canonical, ignoreCase = true) && tiktok.type != canonical) {
                        tiktokDao.update(tiktok.copy(type = canonical))
                    }
                }
            }
        }
    }
}
