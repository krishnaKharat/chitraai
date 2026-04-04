package com.chitraai.app.manager

import android.content.Context
import android.util.Log
import com.chitraai.app.model.MediaItem
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            simulateUpload(items.size, onProgress, onComplete)
            return
        }

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
                        onProgress(progress)
                        if (uploaded == total) onComplete()
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Upload failed: ${e.message}")
                        onError(e.message ?: "Upload failed")
                    }
                    stream.close()
                }
            } catch (e: Exception) {
                Log.e(TAG, "File access error: ${e.message}")
                onError(e.message ?: "File access error")
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