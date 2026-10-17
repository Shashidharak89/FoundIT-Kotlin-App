package com.shashidhara.foundit.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class CloudinaryUploadResult(
    val secureUrl: String,
    val publicId: String,
    val resourceType: String, // "image" or "video"
    val format: String = "",
    val bytes: Long = 0L,
    val duration: Double = 0.0
)

/**
 * Handles direct, unsigned media uploads to Cloudinary without exposing API secrets.
 * Supports both images and videos using Android's Photo Picker stream.
 */
class CloudinaryRepository(
    private val cloudName: String = CLOUD_NAME,
    private val uploadPreset: String = UPLOAD_PRESET
) {
    companion object {
        private const val TAG = "FoundIT_Cloudinary"
        const val CLOUD_NAME = "c1cbvjnj"
        const val UPLOAD_PRESET = "foundit_unsigned"
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    /**
     * Uploads an image or video directly to Cloudinary using the unsigned preset.
     * Reports upload progress from 0.0f to 1.0f.
     */
    suspend fun uploadMedia(
        context: Context,
        uri: Uri,
        isVideo: Boolean,
        onProgress: (Float) -> Unit = {}
    ): Result<CloudinaryUploadResult> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: if (isVideo) "video/mp4" else "image/jpeg"
            val mediaType = mimeType.toMediaTypeOrNull()

            val fileSize = getFileSize(contentResolver, uri)
            val fileName = getFileName(contentResolver, uri) ?: if (isVideo) "video_${System.currentTimeMillis()}.mp4" else "image_${System.currentTimeMillis()}.jpg"

            Log.d(TAG, "Starting Cloudinary upload: name=$fileName, mime=$mimeType, size=$fileSize, isVideo=$isVideo")
            onProgress(0.05f)

            val resourceTypePath = if (isVideo) "video" else "image"
            val uploadUrl = "https://api.cloudinary.com/v1_1/$cloudName/$resourceTypePath/upload"

            val progressRequestBody = ProgressStreamRequestBody(
                contentResolver = contentResolver,
                uri = uri,
                contentType = mediaType,
                totalBytes = fileSize,
                onProgress = { progress ->
                    // Scale progress between 5% and 95% during transmission
                    val scaled = 0.05f + (progress * 0.90f)
                    onProgress(scaled)
                }
            )

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart("file", fileName, progressRequestBody)
                .build()

            val request = Request.Builder()
                .url(uploadUrl)
                .post(multipartBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e(TAG, "Cloudinary upload failed HTTP ${response.code}: $responseBodyString")
                val errorMsg = try {
                    val errorObj = JSONObject(responseBodyString).optJSONObject("error")
                    errorObj?.optString("message") ?: "HTTP ${response.code}"
                } catch (_: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext Result.failure(IOException("Cloudinary upload failed: $errorMsg"))
            }

            Log.d(TAG, "Cloudinary upload success: $responseBodyString")
            val json = JSONObject(responseBodyString)
            val secureUrl = json.getString("secure_url")
            val publicId = json.getString("public_id")
            val returnedResourceType = json.optString("resource_type", if (isVideo) "video" else "image")
            val format = json.optString("format", "")
            val bytes = json.optLong("bytes", fileSize)
            val duration = json.optDouble("duration", 0.0)

            onProgress(1.0f)

            val result = CloudinaryUploadResult(
                secureUrl = secureUrl,
                publicId = publicId,
                resourceType = returnedResourceType,
                format = format,
                bytes = bytes,
                duration = duration
            )
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Cloudinary upload exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun getFileSize(contentResolver: ContentResolver, uri: Uri): Long {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && cursor.moveToFirst()) {
                    cursor.getLong(sizeIndex)
                } else -1L
            } ?: -1L
        } catch (_: Exception) {
            -1L
        }
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Streams file contents directly from ContentResolver to avoid OutOfMemoryErrors on large videos.
     */
    private class ProgressStreamRequestBody(
        private val contentResolver: ContentResolver,
        private val uri: Uri,
        private val contentType: MediaType?,
        private val totalBytes: Long,
        private val onProgress: (Float) -> Unit
    ) : RequestBody() {
        override fun contentType(): MediaType? = contentType

        override fun contentLength(): Long = totalBytes

        override fun writeTo(sink: BufferedSink) {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArray(16384) // 16 KB buffer
                var bytesWritten = 0L
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    sink.write(buffer, 0, read)
                    bytesWritten += read
                    if (totalBytes > 0) {
                        val fraction = (bytesWritten.toFloat() / totalBytes).coerceIn(0f, 1f)
                        onProgress(fraction)
                    }
                }
            } ?: throw IOException("Could not open stream for media Uri: $uri")
        }
    }
}
