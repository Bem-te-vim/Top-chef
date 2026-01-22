package com.sam.topchef.fature_import_recipe.importer

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class TudoGostosoImporter {
    suspend fun import(url: String): String? =
        withContext(Dispatchers.IO) {

            try {

                val doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Android)")
                    .get()
                doc.toString()


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("error", "$e")
                null
            }
        }
}