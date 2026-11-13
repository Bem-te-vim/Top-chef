package com.sam.topchef.feature_import_from_tiktok.ia

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.type.content
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

    companion object {
        private const val MODEL_NAME = "gemini-2.5-flash"
        private const val AUDIO_MIME_TYPE = "audio/mpeg"

        private const val MAX_AUDIO_SIZE = 20L * 1024L * 1024L
    }

    private val httpClient = OkHttpClient()

    val model = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = BuildConfig.GEMINI_FLASH,
        generationConfig = generationConfig {
            // Altera o tipo de resposta para sempre ser JSON válido
            responseMimeType = "application/json"
        }
    )

    suspend fun downloadAudio(
        url: String,
        outputFile: File
    ): File = withContext(Dispatchers.IO) {

        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
            )
            .header(
                "Referer",
                "https://www.tiktok.com/"
            )
            .build()

        httpClient.newCall(request).execute().use { response ->

            if (!response.isSuccessful) {
                throw IOException(
                    "Falha ao baixar áudio: ${response.code}"
                )
            }

            val body = response.body
                ?: throw IOException("Corpo da resposta vazio")

            outputFile.outputStream().use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
        }

        if (!outputFile.exists() || outputFile.length() == 0L) {
            throw IOException(
                "O arquivo de áudio foi baixado vazio."
            )
        }

        outputFile
    }

    suspend fun importRecipe(
        description: List<String>,
        audioFile: File
    ): TikTokModel = withContext(Dispatchers.IO) {

        if (!audioFile.exists()) {
            throw IOException(
                "Arquivo de áudio não encontrado: ${audioFile.absolutePath}"
            )
        }

        if (audioFile.length() == 0L) {
            throw IOException(
                "O arquivo de áudio está vazio."
            )
        }

        /*
         * Firebase AI Logic usando Gemini Developer API
         * aceita arquivos inline de até 20 MB por requisição.
         */
        if (audioFile.length() > MAX_AUDIO_SIZE) {
            throw IOException(
                "O arquivo de áudio é muito grande. " +
                        "Tamanho máximo permitido: 20 MB."
            )
        }

        val prompt = """
            Você é um especialista em receitas culinárias.

            Analise conjuntamente:
            - a descrição do vídeo;
            - hashtags;
            - o áudio do vídeo.

            Sua tarefa é identificar e extrair a receita completa, incluindo uma categoria que melhor se encaixe com o tema da receita.

            Regras importantes:

            1. Retorne APENAS JSON válido.
            2. Não utilize Markdown.
            3. Não coloque ```json.
            4. Não invente ingredientes que não estejam presentes ou claramente identificáveis no conteúdo.
            5. Organize os ingredientes em seções quando isso fizer sentido.
            6. Organize o modo de preparo em etapas.
            7. Se uma informação não puder ser identificada, use uma string vazia ou uma lista vazia.
            8. Preserve quantidades e unidades mencionadas no áudio.
            9. A receita deve ser compreensível mesmo sem o vídeo original.
            10. Defina uma categoria apropriada (ex: Sobremesa, Massa, Lanche, Prato Principal, Salada, Bebida, etc.).

            O JSON deve seguir EXATAMENTE esta estrutura:

            {
              "name": "Nome da receita",
              "description": "Breve descrição da receita",
              "category": "Ex: Sobremesa",
              "ingredients_section": [
                {
                  "sectionName": "Ex: Massa",
                  "sectionItems": [
                    "ingrediente 1",
                    "ingrediente 2"
                  ]
                }
              ],
              "preparation_mode_section": [
                {
                  "step_name": "Ex: Massa",
                  "step_desc": "Descrição detalhada do preparo."
                }
              ]
            }

            DESCRIÇÃO DO VÍDEO E HASHTAGS:
            ${description.joinToString("\n")}
        """.trimIndent()

        try {
            val audioBytes = audioFile.readBytes()

            val response = model.generateContent(
                content {
                    text(prompt)
                    blob("audio/mpeg", audioBytes)
                }
            )


            val jsonString = response.text
                ?: throw IOException(
                    "A resposta da IA veio vazia."
                )

            val gson = GsonBuilder()
                .setLenient()
                .create()

            gson.fromJson(
                jsonString,
                TikTokModel::class.java
            )

        } finally {
            // Deleta o arquivo temporário local após o processamento
            if (audioFile.exists()) {
                audioFile.delete()
            }
        }
    }

    fun close() {
        httpClient.dispatcher.executorService.shutdown()
        httpClient.connectionPool.evictAll()
    }
}