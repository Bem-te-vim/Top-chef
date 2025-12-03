package com.sam.topchef.core.utils

import android.net.Uri
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import jp.wasabeef.glide.transformations.BlurTransformation

class LoadImages {
    fun loadImagesWithBlur(
        imageUrl: String?,
        into: ShapeableImageView,
        @DrawableRes placeHolder: Int = R.drawable.placeholder_item
    ) {
        Glide.with(into.context)
            .load(imageUrl)
            .thumbnail(
                Glide.with(into.context)
                    .load(imageUrl)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
            )
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(placeHolder)
            .into(into)
    }

    fun loadImagesWithBlur(
        imageUrl: Uri?,
        into: ShapeableImageView,
        @DrawableRes placeHolder: Int = R.drawable.placeholder_item
    ) {
        Glide.with(into.context)
            .load(imageUrl)
            .thumbnail(
                Glide.with(into.context)
                    .load(imageUrl)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
            )
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(placeHolder)
            .into(into)
    }
}