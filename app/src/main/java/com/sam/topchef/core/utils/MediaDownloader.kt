package com.sam.topchef.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream

/**
 * Utility class to download, cache, and recover media (images) locally into internal storage,
 * updating entities with local paths and filtering out records without valid local media.
 */
class MediaDownloader(private val context: Context) {

    suspend fun downloadRecipeImages(recipe: Recipe): Recipe? = withContext(Dispatchers.IO) {
        val imagesDir = File(context.filesDir, "recipe_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val localPaths = mutableListOf<String>()
        val originalUris = recipe.imageUriString

        for ((index, uriStr) in originalUris.withIndex()) {
            if (uriStr.isBlank()) continue
            val targetFile = File(imagesDir, "recipe_${recipe.id}_${index}_${uriStr.hashCode()}.jpg")

            if (targetFile.exists() && targetFile.length() > 0) {
                localPaths.add(targetFile.absolutePath)
                continue
            }

            val success = downloadOrCopyImage(uriStr, targetFile)
            if (success && targetFile.exists() && targetFile.length() > 0) {
                localPaths.add(targetFile.absolutePath)
            }
        }

        // If recipe has images specified but none could be downloaded/resolved locally, remove/filter out
        if (originalUris.isNotEmpty() && localPaths.isEmpty()) {
            return@withContext null
        }

        recipe.copy(localImagePaths = localPaths)
    }

    suspend fun downloadUserImage(user: User): User? = withContext(Dispatchers.IO) {
        val userDir = File(context.filesDir, "user_images")
        if (!userDir.exists()) userDir.mkdirs()

        val uriStr = user.imageUri
        if (uriStr.isNullOrBlank()) {
            return@withContext user
        }

        val targetFile = File(userDir, "user_${user.id}_${uriStr.hashCode()}.jpg")
        if (targetFile.exists() && targetFile.length() > 0) {
            return@withContext user.copy(localImageUri = targetFile.absolutePath)
        }

        val success = downloadOrCopyImage(uriStr, targetFile)
        if (success && targetFile.exists() && targetFile.length() > 0) {
            user.copy(localImageUri = targetFile.absolutePath)
        } else {
            user.copy(localImageUri = null)
        }
    }

    suspend fun downloadCartImage(cart: Cart): Cart? = withContext(Dispatchers.IO) {
        val cartDir = File(context.filesDir, "cart_images")
        if (!cartDir.exists()) cartDir.mkdirs()

        val uriStr = cart.cartImage
        if (uriStr.isNullOrBlank()) {
            return@withContext cart
        }

        val targetFile = File(cartDir, "cart_${cart.id}_${uriStr.hashCode()}.jpg")
        if (targetFile.exists() && targetFile.length() > 0) {
            return@withContext cart.copy(localCartImage = targetFile.absolutePath)
        }

        val success = downloadOrCopyImage(uriStr, targetFile)
        if (success && targetFile.exists() && targetFile.length() > 0) {
            cart.copy(localCartImage = targetFile.absolutePath)
        } else {
            cart.copy(localCartImage = null)
        }
    }

    suspend fun downloadOrCopyImage(source: String, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(outputFile.parentFile, "${outputFile.name}.tmp")
            if (source.startsWith("http://") || source.startsWith("https://")) {
                val request = Request.Builder().url(source).build()
                val client = OkHttpClient.Builder().build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext false
                    val body = response.body ?: return@withContext false
                    tempFile.outputStream().use { output ->
                        body.byteStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                }
            } else if (source.startsWith("content://") || source.startsWith("file://")) {
                val uri = Uri.parse(source)
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    tempFile.outputStream().use { output ->
                        inputStream.use { input ->
                            input.copyTo(output)
                        }
                    }
                } else {
                    return@withContext false
                }
            } else {
                val file = File(source)
                if (file.exists()) {
                    file.copyTo(tempFile, overwrite = true)
                } else {
                    return@withContext false
                }
            }

            if (tempFile.exists() && tempFile.length() > 0) {
                if (outputFile.exists()) outputFile.delete()
                val renamed = tempFile.renameTo(outputFile)
                if (!renamed) {
                    tempFile.copyTo(outputFile, overwrite = true)
                    tempFile.delete()
                }
                true
            } else {
                if (tempFile.exists()) tempFile.delete()
                false
            }
        } catch (e: Exception) {
            Log.e("MediaDownloader", "Error downloading/copying image from $source", e)
            false
        }
    }
}
