package com.sam.topchef.feature_import_from_tiktok.offline

import android.content.Context
import android.util.Log
import com.sam.topchef.core.data.local.appDataBase.AppDataBase
import com.sam.topchef.feature_import_from_tiktok.model.TikTokModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.Collections

/**
 * Handles downloading TikTok media (videos, thumbnails, avatars) and saving them locally for offline usage.
 */
class TiktokVideoDownloader(private val context: Context) {
    private val activeDownloads = Collections.synchronizedSet(HashSet<String>())

    /**
     * Downloads the video, thumbnail, and author avatar for the given recipe if not already downloaded or cached,
     * updates the database with local file paths, and invokes the callback on completion.
     */
    suspend fun downloadAndSaveVideo(recipe: TikTokModel, onUpdated: (TikTokModel) -> Unit = {}) {
        val videoUrl = recipe.videoUrl ?: return
        if (activeDownloads.contains(videoUrl)) return

        withContext(Dispatchers.IO) {
            val videosDir = File(context.filesDir, "tiktok_videos")
            val imagesDir = File(context.filesDir, "tiktok_images")
            if (!videosDir.exists()) videosDir.mkdirs()
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val targetVideoFile = if (recipe.id != 0) {
                File(videosDir, "tiktok_${recipe.id}.mp4")
            } else {
                File(videosDir, "tiktok_temp_${videoUrl.hashCode()}.mp4")
            }

            val targetThumbFile = if (recipe.id != 0) {
                File(imagesDir, "tiktok_thumb_${recipe.id}.jpg")
            } else {
                File(imagesDir, "tiktok_thumb_temp_${videoUrl.hashCode()}.jpg")
            }

            val targetAvatarFile = if (recipe.id != 0) {
                File(imagesDir, "tiktok_avatar_${recipe.id}.jpg")
            } else {
                File(imagesDir, "tiktok_avatar_temp_${videoUrl.hashCode()}.jpg")
            }

            var localVideoPath = recipe.localVideoPath
            var localThumbnailPath = recipe.localThumbnailPath
            var localAuthorAvatarPath = recipe.localAuthorAvatarPath

            // 1. Download video if not present
            if (!targetVideoFile.exists() || targetVideoFile.length() == 0L) {
                if (downloadFileFromNetwork(videoUrl, targetVideoFile)) {
                    localVideoPath = targetVideoFile.absolutePath
                }
            } else {
                localVideoPath = targetVideoFile.absolutePath
            }

            // 2. Download thumbnail if not present
            if (!recipe.thumbnail.isNullOrEmpty() && (!targetThumbFile.exists() || targetThumbFile.length() == 0L)) {
                if (downloadFileFromNetwork(recipe.thumbnail, targetThumbFile)) {
                    localThumbnailPath = targetThumbFile.absolutePath
                }
            } else if (targetThumbFile.exists() && targetThumbFile.length() > 0L) {
                localThumbnailPath = targetThumbFile.absolutePath
            }

            // 3. Download avatar if not present
            if (!recipe.authorAvatar.isNullOrEmpty() && (!targetAvatarFile.exists() || targetAvatarFile.length() == 0L)) {
                if (downloadFileFromNetwork(recipe.authorAvatar, targetAvatarFile)) {
                    localAuthorAvatarPath = targetAvatarFile.absolutePath
                }
            } else if (targetAvatarFile.exists() && targetAvatarFile.length() > 0L) {
                localAuthorAvatarPath = targetAvatarFile.absolutePath
            }

            activeDownloads.add(videoUrl)
            try {
                val db = AppDataBase.getDataBase(context)
                val existing = if (recipe.id != 0) {
                    db.tiktokDao().getById(recipe.id)
                } else {
                    recipe.originUrl?.let { db.tiktokDao().getByUrl(it) }
                }

                val updated = (existing ?: recipe).copy(
                    localVideoPath = localVideoPath,
                    localThumbnailPath = localThumbnailPath,
                    localAuthorAvatarPath = localAuthorAvatarPath
                )

                if (updated.id != 0) {
                    db.tiktokDao().update(updated)
                } else {
                    db.tiktokDao().insert(updated)
                }

                val finalModel = if (updated.id != 0) {
                    db.tiktokDao().getById(updated.id) ?: updated
                } else {
                    updated.originUrl?.let { db.tiktokDao().getByUrl(it) } ?: updated
                }

                Log.d("TiktokVideoDownloader", "Media downloaded and saved locally for recipe: ${finalModel.name}")

                withContext(Dispatchers.Main) {
                    onUpdated(finalModel)
                }
            } finally {
                activeDownloads.remove(videoUrl)
            }
        }
    }

    private suspend fun downloadFileFromNetwork(fileUrl: String, outputFile: File): Boolean =
        withContext(Dispatchers.IO) {
            try {
                outputFile.parentFile?.let {
                    if (!it.exists()) it.mkdirs()
                }
                val tempFile = File(outputFile.parentFile, "${outputFile.name}.tmp")

                val request = Request.Builder()
                    .url(fileUrl)
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                    )
                    .header("Referer", "https://www.tiktok.com/")
                    .build()

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
                Log.e("TiktokVideoDownloader", "Error downloading file from $fileUrl", e)
                false
            }
        }
}
