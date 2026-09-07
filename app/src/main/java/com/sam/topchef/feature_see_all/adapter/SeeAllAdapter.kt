package com.sam.topchef.feature_see_all.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.utils.LoadImages
import java.util.Locale

class SeeAllAdapter(@LayoutRes private val layout: Int = R.layout.row_images) :
    RecyclerView.Adapter<SeeAllAdapter.SeeAllViewHolder>() {

    var itemClick: ((id: Int) -> Unit)? = null
    var itemLongClick: ((id: Int) -> Unit)? = null
    var categoryClick: ((category: String) -> Unit)? = null
    var likeClick: ((id: Int, isFavorite: Boolean) -> Unit)? = null

    private val recipes = mutableListOf<Recipe>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<Recipe>) {
        recipes.clear()
        recipes.addAll(list)
        notifyDataSetChanged()
    }

    inner class SeeAllViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imageview: ShapeableImageView? = view.findViewById(R.id.image_item)
        val imagePopular: ShapeableImageView? = view.findViewById(R.id.img_popular_recipe)
        val imageCategory: ShapeableImageView? = view.findViewById(R.id.img_category)
        val tvCategory: TextView? = view.findViewById(R.id.tv_category)
        val txtTitle: TextView? = view.findViewById(R.id.txt_popularRecipe_title)
        val txtInfo: TextView? = view.findViewById(R.id.txt_popularRecipe_info)
        val btnFavorite: ImageButton? = view.findViewById(R.id.btn_favorite_popular_recipe)

        fun bind(item: Recipe) {
            val img = imageview ?: imagePopular ?: imageCategory
            img?.let {
                LoadImages().loadImagesWithBlur(item.imageUriString.firstOrNull(), it)
            }

            txtTitle?.text = item.title
            tvCategory?.text = item.type
            txtInfo?.let {
                it.text = it.context.getString(
                    R.string.recipe_info,
                    timeFormater(item.preparationTime),
                    difficultFormater(item.difficult, it.context),
                    item.chef ?: "Top Chef"
                )
            }

            btnFavorite?.let { btn ->
                setButtonState(item.isFavorite, btn, btn.context)
                btn.setOnClickListener {
                    item.isFavorite = !item.isFavorite
                    setButtonState(item.isFavorite, btn, btn.context)
                    likeClick?.invoke(item.id, item.isFavorite)
                }
            }

            itemView.setOnClickListener {
                if (layout == R.layout.row_categories_recipe_item && item.type != null) {
                    categoryClick?.invoke(item.type)
                } else {
                    itemClick?.invoke(item.id)
                }
            }
            itemView.setOnLongClickListener {
                itemLongClick?.invoke(item.id)
                true
            }
        }

        private fun difficultFormater(difficult: Int, context: Context): String {
            return when (difficult) {
                1 -> context.getString(R.string.very_easy)
                2 -> context.getString(R.string.easy)
                3 -> context.getString(R.string.average)
                4 -> context.getString(R.string.hard)
                5 -> context.getString(R.string.very_hard)
                else -> ""
            }
        }

        @SuppressLint("DefaultLocale")
        private fun timeFormater(totalMinutes: Int): String {
            val h = totalMinutes / 60
            val min = totalMinutes % 60
            return String.format("%dh:%02dmin", h, min)
        }

        private fun setButtonState(isFavorite: Boolean, btnFavorite: ImageButton, context: Context) {
            if (isFavorite) btnFavorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.default_color_app)
            ) else btnFavorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.myGray)
            )
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SeeAllViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        
        if (layout == R.layout.row_popular_recipe_item || layout == R.layout.row_categories_recipe_item) {
            val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
            layoutParams?.let {
                val marginInPx = (20 * parent.context.resources.displayMetrics.density).toInt()
                it.bottomMargin = marginInPx
                view.layoutParams = it
            }
        }
        
        return SeeAllViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SeeAllViewHolder,
        position: Int
    ) {
        val item = recipes[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = recipes.size


}