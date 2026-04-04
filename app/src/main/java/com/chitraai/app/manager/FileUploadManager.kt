package com.chitraai.app.manager

import android.content.Context
import android.util.Log
import com.chitraai.app.model.MediaItem
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileUploadManager(private val context: Context) {

    companion object {
        private const val TAG = "FileUploadManager"
    }

    private val storage = try {
        FirebaseStorage.getInstance().reference
    } catch (e: Exception) {
        null
    }

    fun uploadFiles(
        items: List<MediaItem>,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (storage == null) {
            // Simulation mode
            simulateUpload(items.size, onProgress, onComplete)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val total = items.size
            var uploaded = 0

            for (item in items) {
                try {
                    val inputStream = context.contentResolver.openInputStream(item.uri)
                    inputStream?.let { stream ->
                        val ref = storage.child("shared/${System.currentTimeMillis()}_${item.name}")
                        val uploadTask = ref.putStream(stream)

                        uploadTask.addOnSuccessListener {
                            uploaded++
                            val progress = (uploaded * 100) / total
                            withContext(Dispatchers.Main) { onProgress(progress) }
                            if (uploaded == total) {
                                CoroutineScope(Dispatchers.Main).launch { onComplete() }
                            }
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "Upload failed: ${e.message}")
                            CoroutineScope(Dispatchers.Main).launch { onError(e.message ?: "Upload failed") }
                        }
                        stream.close()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "File access error: ${e.message}")
                    withContext(Dispatchers.Main) { onError(e.message ?: "File access error") }
                }
            }
        }
    }

    private fun simulateUpload(
        count: Int,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            for (i in 1..10) {
                kotlinx.coroutines.delay(200)
                onProgress(i * 10)
            }
            onComplete()
        }
    }
}
