package com.sam.topchef.feature_import_from_tiktok.ia

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sam.topchef.BuildConfig
import com.sam.topchef.feature_import_from_tiktok.model.TikTokModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

class RecipeInfoByIA {

    // Instancie uma única vez para reaproveitar o pool de conexões
    private val httpClient = OkHttpClient()

    val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_FLASH,
        generationConfig = generationConfig {
            // Altera o tipo de resposta para sempre ser JSON válido
            responseMimeType = "application/json"
        }
    )

    suspend fun downloadAudio(url: String, outputFile: File): File = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36")
            .header("Referer", "https://www.tiktok.com/")
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Falha ao baixar áudio: ${response.code}")
            val body = response.body ?: throw IOException("Corpo da resposta vazio")

            outputFile.outputStream().use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
        }
        outputFile
    }

    suspend fun importRecipe(
        description: List<String>,
        audioFile: File
    ): TikTokModel {

        val audioBytes = audioFile.readBytes()

        val prompt = """
        Você é um especialista em receitas.

        Analise:
        - descrição do vídeo
        - áudio

        Extraia a receita completa.

        Retorne APENAS JSON válido.

        {
          "name": "Nome da receita",
          "description": "Breve descrição",
          "ingredients_section": [
          sectionName: "ex: cobertura"
          sectionItems:["ingrediente1", "ingrediente 2"]
          ],
          "preparation_mode_section": [{
          step_name: "ex: massa"
          step_desc: "descricao do modo de preparo..."
          }
          ]
        }

        DESCRIÇÃO COM HASHTAGS
        ${description.joinToString("\n") }
    """.trimIndent()

        val response = model.generateContent(
            content {
                text(prompt)
                blob("audio/mpeg", audioBytes)
            }
        )

        // O response.text agora virá estritamente como uma string JSON pura, sem ```json
        val jsonString = response.text ?: throw Exception("Resposta da IA veio vazia")

        // Instancia o Gson configurado para aceitar caracteres UTF-8 de forma segura
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return gson.fromJson(
            jsonString,
            TikTokModel::class.java
        )
    }
}