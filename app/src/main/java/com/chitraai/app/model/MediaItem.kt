package com.chitraai.app.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val type: Type,
    var isSelected: Boolean = false
) {
    enum class Type { IMAGE, VIDEO }
}
