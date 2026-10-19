package com.sam.topchef.feature_settings.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.databinding.RowSettingsRecipeItemBinding
import com.sam.topchef.feature_settings.model.SettingsRecipeUiModel

class SettingsRecipeAdapter(
    var onItemClick: (SettingsRecipeUiModel) -> Unit = {},
    var onItemLongClick: (SettingsRecipeUiModel) -> Unit = {},
    var onSelectionChanged: () -> Unit = {}
) : RecyclerView.Adapter<SettingsRecipeAdapter.ViewHolder>() {

    private val items = mutableListOf<SettingsRecipeUiModel>()
    var isSelectionMode: Boolean = false
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            if (!value) {
                items.forEach { it.isSelected = false }
            }
            notifyDataSetChanged()
        }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<SettingsRecipeUiModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<SettingsRecipeUiModel> {
        return items.filter { it.isSelected }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll() {
        items.forEach { it.isSelected = true }
        notifyDataSetChanged()
        onSelectionChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelection() {
        items.forEach { it.isSelected = false }
        notifyDataSetChanged()
        onSelectionChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowSettingsRecipeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: RowSettingsRecipeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingsRecipeUiModel) {
            binding.recipeTitle.text = item.title
            binding.recipeInfo.text = item.subtitle

            LoadImages().loadImagesWithBlur(item.imageUrl, binding.imageRecipeItem)

            if (isSelectionMode) {
                binding.cbSelect.visibility = View.VISIBLE
                binding.cbSelect.isChecked = item.isSelected
                binding.btnEnterRecipe.visibility = View.GONE
            } else {
                binding.cbSelect.visibility = View.GONE
                binding.btnEnterRecipe.visibility = View.VISIBLE
            }

            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    item.isSelected = !item.isSelected
                    binding.cbSelect.isChecked = item.isSelected
                    onSelectionChanged()
                } else {
                    onItemClick(item)
                }
            }

            binding.root.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    item.isSelected = true
                    notifyDataSetChanged()
                    onItemLongClick(item)
                } else {
                    item.isSelected = !item.isSelected
                    binding.cbSelect.isChecked = item.isSelected
                    onSelectionChanged()
                }
                true
            }
        }
    }
}
