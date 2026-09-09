package com.sam.topchef.feature_feed_main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.feature_feed_main.data.model.RecipeCategory

class CategoryHeaderAdapter(
    private val onCategoryClick: (String) -> Unit,
    private val onSeeAllClick: () -> Unit
) : RecyclerView.Adapter<CategoryHeaderAdapter.ViewHolder>() {

    private var items = emptyList<RecipeCategory>()

    fun setItems(newItems: List<RecipeCategory>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_rv_horizontal_categories, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items)
    }

    override fun getItemCount() = if (items.isNotEmpty()) 1 else 0

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val rv = view.findViewById<RecyclerView>(R.id.rv_categories)
        private val btnSeeAll = view.findViewById<View>(R.id.btn_see_all_categories)
        private val adapter = CategoryRecipeAdapter()

        init {
            rv.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            rv.adapter = adapter
            adapter.onCategoryClick = onCategoryClick
            btnSeeAll.setOnClickListener { onSeeAllClick() }
        }

        fun bind(items: List<RecipeCategory>) {
            adapter.setItems(items)
        }
    }
}
