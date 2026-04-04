package com.chitraai.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chitraai.app.R
import com.chitraai.app.model.MediaItem

class GalleryAdapter(
    private val onItemToggle: (MediaItem, Boolean) -> Unit
) : ListAdapter<MediaItem, GalleryAdapter.ViewHolder>(DiffCallback()) {

    private val selectedIds = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun clearSelection() {
        selectedIds.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        private val ivVideoIcon: ImageView = itemView.findViewById(R.id.iv_video_icon)
        private val ivSelected: ImageView = itemView.findViewById(R.id.iv_selected)
        private val overlaySelected: View = itemView.findViewById(R.id.overlay_selected)

        fun bind(item: MediaItem) {
            Glide.with(itemView)
                .load(item.uri)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivThumbnail)

            ivVideoIcon.visibility = if (item.type == MediaItem.Type.VIDEO) View.VISIBLE else View.GONE

            val isSelected = selectedIds.contains(item.id)
            ivSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
            overlaySelected.visibility = if (isSelected) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                val nowSelected = !selectedIds.contains(item.id)
                if (nowSelected) selectedIds.add(item.id) else selectedIds.remove(item.id)
                item.isSelected = nowSelected
                ivSelected.visibility = if (nowSelected) View.VISIBLE else View.GONE
                overlaySelected.visibility = if (nowSelected) View.VISIBLE else View.GONE
                // Animate selection
                itemView.animate().scaleX(if (nowSelected) 0.93f else 1f)
                    .scaleY(if (nowSelected) 0.93f else 1f).setDuration(120).start()
                onItemToggle(item, nowSelected)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MediaItem>() {
        override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem) = oldItem == newItem
    }
}
