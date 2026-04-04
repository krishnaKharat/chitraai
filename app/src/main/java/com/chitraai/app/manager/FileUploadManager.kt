package com.chitraai.app.manager

import android.content.Context
import android.util.Log
import com.chitraai.app.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FileUploadManager(private val context: Context) {

    companion object {
        private const val TAG = "FileUploadManager"
    }

    // Firebase disabled until google-services.json is configured
    // All uploads run in simulation mode
    private val storage = null

    fun uploadFiles(
        items: List<MediaItem>,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d(TAG, "Firebase not configured, using simulation mode")
        simulateUpload(items.size, onProgress, onComplete)
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
