package com.sam.topchef.feature_import_from_tiktok.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.core.utils.adapter.TextsAdapter
import com.sam.topchef.feature_import_from_tiktok.model.TiktokSection

class TiktokIngredientsAdapter(private val section: List<TiktokSection>) :
    RecyclerView.Adapter<TiktokIngredientsAdapter.TiktokIngredientsViewHolder>() {


    inner class TiktokIngredientsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sectionName: TextView = view.findViewById(R.id.txt_section_name)
        val ingredientsSection: RecyclerView = view.findViewById(R.id.listView_section)

        init {
            ingredientsSection.layoutManager = LinearLayoutManager(itemView.context)
        }
        fun bing(item: TiktokSection) {
            sectionName.text = item.sectionName
            ingredientsSection.adapter = TextsAdapter(item.sectionItems)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TiktokIngredientsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_ingredients_by_section_item, parent, false)
        return TiktokIngredientsViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TiktokIngredientsViewHolder,
        position: Int
    ) {
        val item = section[position]
        holder.bing(item)
    }

    override fun getItemCount(): Int {
        return section.size
    }


}