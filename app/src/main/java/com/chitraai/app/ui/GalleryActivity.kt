package com.chitraai.app.ui

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.chitraai.app.R
import com.chitraai.app.adapter.GalleryAdapter
import com.chitraai.app.databinding.ActivityGalleryBinding
import com.chitraai.app.manager.FileUploadManager
import com.chitraai.app.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding
    private lateinit var adapter: GalleryAdapter
    private lateinit var fileUploadManager: FileUploadManager
    private val selectedItems = mutableListOf<MediaItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileUploadManager = FileUploadManager(this)

        setupRecyclerView()
        setupUI()
        loadMedia()
    }

    private fun setupRecyclerView() {
        adapter = GalleryAdapter { item, isSelected ->
            if (isSelected) {
                selectedItems.add(item)
            } else {
                selectedItems.remove(item)
            }
            updateSendButton()
        }
        binding.rvGallery.apply {
            layoutManager = GridLayoutManager(this@GalleryActivity, 3)
            adapter = this@GalleryActivity.adapter
        }
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSendSelected.setOnClickListener {
            if (selectedItems.isNotEmpty()) {
                sendSelectedFiles()
            }
        }

        binding.btnSendSelected.isEnabled = false
        binding.btnSendSelected.text = "Select files to send"
    }

    private fun updateSendButton() {
        val count = selectedItems.size
        if (count == 0) {
            binding.btnSendSelected.isEnabled = false
            binding.btnSendSelected.text = "Select files to send"
        } else {
            binding.btnSendSelected.isEnabled = true
            binding.btnSendSelected.text = "Send $count file${if (count > 1) "s" else ""} to Krish"
        }
    }

    private fun loadMedia() {
        binding.progressLoading.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val items = queryMedia()
            withContext(Dispatchers.Main) {
                binding.progressLoading.visibility = View.GONE
                if (items.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    adapter.submitList(items)
                }
            }
        }
    }

    private fun queryMedia(): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"

        // Query images
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                items.add(MediaItem(id, uri, cursor.getString(nameCol), cursor.getString(mimeCol), MediaItem.Type.IMAGE))
            }
        }

        // Query videos
        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
                items.add(MediaItem(id, uri, cursor.getString(nameCol), cursor.getString(mimeCol), MediaItem.Type.VIDEO))
            }
        }

        return items.sortedByDescending { it.id }
    }

    private fun sendSelectedFiles() {
        binding.btnSendSelected.isEnabled = false
        binding.btnSendSelected.text = "Sending..."
        binding.uploadProgress.visibility = View.VISIBLE

        fileUploadManager.uploadFiles(selectedItems,
            onProgress = { progress ->
                binding.uploadProgress.progress = progress
            },
            onComplete = {
                binding.uploadProgress.visibility = View.GONE
                binding.btnSendSelected.text = "✓ Sent to Krish!"
                binding.btnSendSelected.setBackgroundResource(R.drawable.btn_success)
                selectedItems.clear()
                adapter.clearSelection()
                updateSendButton()
            },
            onError = { error ->
                binding.uploadProgress.visibility = View.GONE
                binding.btnSendSelected.isEnabled = true
                binding.btnSendSelected.text = "Retry sending"
            }
        )
    }
}
