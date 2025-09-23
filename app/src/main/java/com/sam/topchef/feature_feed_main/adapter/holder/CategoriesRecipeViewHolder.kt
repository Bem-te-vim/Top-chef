package com.sam.topchef.feature_feed_main.adapter.holder

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.feature_feed_main.adapter.CategoryRecipeAdapter
import com.sam.topchef.feature_feed_main.data.model.RecipeCategory

class CategoriesRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val context = itemView.context

    private lateinit var categoryRecipeAdapter: CategoryRecipeAdapter

    private val rvCategoryRecipe: RecyclerView = view.findViewById(R.id.rv_horizontal)
    private val tvTitle: TextView = view.findViewById(R.id.tv_title)
    private val btnSeeAll: AppCompatButton = view.findViewById(R.id.btn_see_all)

    fun bind(
        onWhatShowListener: ((String) -> Unit)? = null
    ) {
        //test
        val categories = mutableListOf<RecipeCategory>()
        for (i in 0 until 10) {
            val cat = RecipeCategory(i, "molho $i","Categoria $i")
            categories.add(cat)
        }


        tvTitle.text = context.getString(R.string.categoria)

        btnSeeAll.setOnClickListener {
            onWhatShowListener?.invoke("AllCategories")
        }

        categoryRecipeAdapter = CategoryRecipeAdapter(categories) { category ->
            onWhatShowListener?.invoke(category.type)
        }


        rvCategoryRecipe.layoutManager =
            LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        rvCategoryRecipe.adapter = categoryRecipeAdapter
    }
}