package com.sam.topchef.fature_import_recipe.importer

import com.sam.topchef.core.data.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class TudoGostosoImporter {
    suspend fun import(url: String): Recipe? =
        withContext(Dispatchers.IO) {

            try {
                val doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Android)")
                    .get()

                val title = doc
                    .selectFirst("span.u-title-page")
                    ?.text()
                    ?: return@withContext null

                val ingredients = doc
                    .select("span.recipe-ingredients-item-label")
                    .map { it.text() }

                val preparation = doc
                    .select("div.recipe-steps-text p")
                    .map { it.text() }


                val description = doc
                    .select("div.is-wysiwyg p")
                    .joinToString {
                        it.text().replace("\u00A0", " ").trim()
                    }

                val recipeImageUrl = doc
                    .select("div.recipe-cover source")
                    .firstOrNull()
                    ?.attr("srcset")
                    ?: doc.select("div.recipe-cover img")
                        .firstOrNull()
                        ?.attr("src") ?: "Error"

                val times = doc.select("div.recipe-steps-info-item time")

                val prepMinutes = parseIsoDurationToMinutes(times.getOrNull(0)?.attr("datetime"))
                val cookMinutes = parseIsoDurationToMinutes(times.getOrNull(1)?.attr("datetime"))

                val recipeType = doc
                    .select("ul.breadcrumb li.breadcrumb-item a")
                    .firstOrNull { it.attr("href").contains("/categorias/") }
                    ?.text()
                    ?.trim()
                    ?.replace("Receitas", "")
                    ?: "Outros"




                Recipe(
                    chef = "Web",
                    title = title,
                    ingredients = ingredients,
                    preparationMode = preparation,
                    description = description,
                    difficult = 1,
                    imageUriString = listOf(recipeImageUrl),
                    cookingTime = cookMinutes,
                    preparationTime = prepMinutes,
                    type = recipeType
                )

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


    fun parseIsoDurationToMinutes(iso: String?): Int {
        if (iso.isNullOrBlank()) return 0

        val hours = Regex("(\\d+)H").find(iso)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minutes = Regex("(\\d+)M").find(iso)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        return hours * 60 + minutes
    }


}