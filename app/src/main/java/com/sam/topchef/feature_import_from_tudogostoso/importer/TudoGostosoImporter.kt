package com.sam.topchef.feature_import_from_tudogostoso.importer

import android.util.Log
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.feature_import_from_tudogostoso.model.WebRecipeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import kotlin.to
import kotlin.toString

object TudoGostosoImporter {
    private const val BASE_URL = "https://www.tudogostoso.com.br/"
    private const val SEARCH_URL = "https://www.tudogostoso.com.br/busca?q="


    suspend fun searchRecipe(search: String): List<WebRecipeModel> = withContext(Dispatchers.IO) {
        val search = search.replace(" ", "+").trim()
        try {
            val doc = Jsoup.connect(SEARCH_URL + search)
                .userAgent(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/140.0.0.0 Safari/537.36"
                )
                .timeout(15_000)
                .get()


            val recipes = doc.select("div.card.card-recipe")

            val webRecipes = recipes.mapNotNull { card ->

                val title = card
                    .select("strong.card-title a.card-link")
                    .text()
                    .trim()

                val link = card
                    .select("strong.card-title a.card-link")
                    .attr("href")
                    .trim()


                val imageUrl = card.selectFirst("picture.card-media")?.let { picture ->

                val source = picture.selectFirst("source[srcset]")
                val sourceUrl = source?.absUrl("srcset")?.trim()

                if (!sourceUrl.isNullOrEmpty()) {
                    sourceUrl
                } else {
                    picture.selectFirst("img")?.let { img ->
                        img.absUrl("src").trim()
                    }
                }

            } ?: ""

                if (title.isEmpty() || link.isEmpty() || imageUrl.isEmpty()) {
                    null
                } else {
                    WebRecipeModel(
                        title = title,
                        imageUrl = imageUrl,
                        recipeLinksPath = link
                    )
                }

            }
            Log.i("searchRecipe", "Receitas encontradas: ${webRecipes.size}")

            webRecipes.forEach {
                Log.i(
                    "searchRecipe",
                    """
                    Título: ${it.title}
                    Link: ${it.recipeLinksPath}
                    Imagem: ${it.imageUrl}
                    """.trimIndent()
                )
            }

            webRecipes
        } catch (e: Exception) {
            Log.e("searchRecipe", "Erro ao buscar", e)
            emptyList()
        }
    }

    suspend fun getFeed(url: String = BASE_URL): List<WebRecipeModel> =
        withContext(Dispatchers.IO) {
            try {

                val doc = Jsoup.connect(url)
                    .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                "Chrome/140.0.0.0 Safari/537.36"
                    )
                    .referrer("https://www.tudogostoso.com.br/")
                    .timeout(15_000)
                    .get()

                val recipes = doc.select("div.card.card-recipe")

                val webRecipes = recipes.mapNotNull { card ->

                    val title = card
                        .select("strong.card-title a.card-link")
                        .text()
                        .trim()

                    val link = card
                        .select("strong.card-title a.card-link")
                        .attr("href")
                        .trim()

                    val imageElement = card.selectFirst(
                        "picture.card-media img"
                    )

                    val imageUrl = imageElement?.let { img ->

                        // Primeiro tenta src
                        val src = img.attr("src").trim()

                        if (src.isNotEmpty()) {
                            img.absUrl("src")
                        } else {
                            // Caso o site use lazy loading
                            val dataSrc = img.attr("data-src").trim()

                            if (dataSrc.isNotEmpty()) {
                                img.absUrl("data-src")
                            } else {
                                ""
                            }
                        }
                    } ?: ""

                    if (title.isEmpty() || link.isEmpty() || imageUrl.isEmpty()) {
                        null
                    } else {
                        WebRecipeModel(
                            title = title,
                            imageUrl = imageUrl,
                            recipeLinksPath = link
                        )
                    }
                }

                Log.i("getFeed", "Receitas encontradas: ${webRecipes.size}")

                webRecipes.forEach {
                    Log.i(
                        "getFeed",
                        """
                    Título: ${it.title}
                    Link: ${it.recipeLinksPath}
                    Imagem: ${it.imageUrl}
                    """.trimIndent()
                    )
                }

                webRecipes

            } catch (e: Exception) {
                Log.e("getFeed", "Erro ao buscar feed", e)
                emptyList()
            }
        }


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




               val recipe = Recipe(
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

                Log.i("TGimport", recipe.toString() )

                recipe
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