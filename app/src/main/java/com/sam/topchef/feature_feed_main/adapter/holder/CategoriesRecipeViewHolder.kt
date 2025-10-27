package com.sam.topchef.feature_feed_main.adapter.holder

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.feature_feed_main.adapter.CategoryRecipeAdapter
import com.sam.topchef.feature_feed_main.data.model.RecipeCategory

class CategoriesRecipeViewHolder(
    view: View,
) :
    RecyclerView.ViewHolder(view) {
    private val context = itemView.context

    private lateinit var categoryRecipeAdapter: CategoryRecipeAdapter

    private val rvCategoryRecipe: RecyclerView = view.findViewById(R.id.rv_horizontal)
    private val tvTitle: TextView = view.findViewById(R.id.tv_title)
    private val btnSeeAll: AppCompatButton = view.findViewById(R.id.btn_see_all)


    fun bind(
        categories: List<RecipeCategory>,
        onWhatShowListener: ((String) -> Unit)? = null,
        sharedPool: RecyclerView.RecycledViewPool
    ) {
        categoryRecipeAdapter = CategoryRecipeAdapter(categories) { category ->
            onWhatShowListener?.invoke(category.type)
        }

        rvCategoryRecipe.apply {
            layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryRecipeAdapter
            setRecycledViewPool(sharedPool)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            setItemViewCacheSize(10)
        }

        if (categories.isEmpty()) {
            tvTitle.visibility = View.GONE
            btnSeeAll.visibility = View.GONE
        } else {
            tvTitle.visibility = View.VISIBLE
            btnSeeAll.visibility = View.VISIBLE
        }

        tvTitle.text = context.getString(R.string.categoria)

        btnSeeAll.setOnClickListener {
            onWhatShowListener?.invoke("AllCategories")
        }

    }
}