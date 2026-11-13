package com.sam.topchef.feature_settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.databinding.RowSettingsCategoryItemBinding
import com.sam.topchef.feature_settings.model.SettingsCategoryUiModel

class SettingsCategoryAdapter(
    private val onItemClick: (SettingsCategoryUiModel) -> Unit,
    private val onDeleteClick: (SettingsCategoryUiModel) -> Unit
) : RecyclerView.Adapter<SettingsCategoryAdapter.CategoryViewHolder>() {

    private var items = emptyList<SettingsCategoryUiModel>()

    fun submitList(newList: List<SettingsCategoryUiModel>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = RowSettingsCategoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CategoryViewHolder(
        private val binding: RowSettingsCategoryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingsCategoryUiModel) {
            val context = binding.root.context
            binding.tvCategoryName.text = item.name

            binding.tvRecipeCount.text = when (item.recipeCount) {
                0 -> context.getString(R.string.recipe_count_zero)
                1 -> context.getString(R.string.recipe_count_singular, 1)
                else -> context.getString(R.string.recipe_count_plural, item.recipeCount)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }

            if (item.canDelete) {
                binding.btnDeleteCategory.visibility = View.VISIBLE
                binding.btnDeleteCategory.setOnClickListener {
                    onDeleteClick(item)
                }
            } else {
                binding.btnDeleteCategory.visibility = View.GONE
                binding.btnDeleteCategory.setOnClickListener(null)
            }
        }
    }
}
