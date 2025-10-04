package com.sam.topchef.feature_recipe_detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R

class ImagesAdapter(private val images: List<String>) :
    RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder>() {

    inner class ImagesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDetail: ShapeableImageView = view.findViewById(R.id.image_item_from_detail)
        fun bing(img: String) {
            Glide.with(itemView.context)
                .load(img)
                .placeholder(R.drawable.placeholder_item)
                .into(imgDetail)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImagesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.sam.topchef.R.layout.row_images_from_detail, parent, false)
        return ImagesViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ImagesViewHolder,
        position: Int
    ) {
        val img = images[position]
        holder.bing(img)
    }

    override fun getItemCount(): Int {
        return images.size
    }


}