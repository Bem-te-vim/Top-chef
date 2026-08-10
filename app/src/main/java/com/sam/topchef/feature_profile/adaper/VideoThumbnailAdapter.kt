package com.sam.topchef.feature_profile.adaper

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.core.utils.Utils.clickAnimation
import com.sam.topchef.feature_profile.model.VideoThumbnail

class VideoThumbnailAdapter() :
    RecyclerView.Adapter<VideoThumbnailAdapter.VideoThumbnailViewHolder>() {

    var itemClick: ((id: Int) -> Unit)? = null
    var itemLongClick: ((id: Int) -> Unit)? = null

    private val videos = mutableListOf<VideoThumbnail>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<VideoThumbnail>) {
        videos.clear()
        videos.addAll(list)
        notifyDataSetChanged()
    }

    inner class VideoThumbnailViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imageview: ShapeableImageView = view.findViewById(R.id.image_item)
        val title: TextView = view.findViewById(R.id.txt_video_title)
        val desc: TextView = view.findViewById(R.id.txt_video_desc)
        fun bind(item: VideoThumbnail) {
            LoadImages().loadImagesWithBlur(item.thumbnailPath, imageview)
            title.text = item.title
            desc.text = item.description

            itemView.setOnClickListener {
                it.clickAnimation()
                itemClick?.invoke(item.videoId)
            }
            itemView.setOnLongClickListener {
                itemLongClick?.invoke(item.videoId)
                true
            }

        }

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideoThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_video_thumbnail, parent, false)
        return VideoThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: VideoThumbnailViewHolder,
        position: Int
    ) {
        val item = videos[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = videos.size


}