package com.sam.topchef.feature_recipe_detail.ui.activity


import android.R
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.databinding.ActivityRecipeDetailBinding

class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val i = intent
        val recipeId = i.extras?.getInt("id")

        val statusBarHeight = resources.getDimensionPixelSize(
            resources.getIdentifier("status_bar_height", "dimen", "android")
        )
        binding.statusBarOverlay.layoutParams.height = statusBarHeight
        binding.btnBack.setOnClickListener { finish() }

        val rvImageFromDetail = binding.rvImageFromDetail
        rvImageFromDetail.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvImageFromDetail.adapter = ImagesAdapter()

        val rvIngredients = binding.rvIngredients
        rvIngredients.layoutManager = LinearLayoutManager(this)
        rvIngredients.adapter //=todo:


        val items = listOf("1 Service", "2 Services", "3 Services")
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, items)

        val autoComplete = binding.autoCompleteService
        autoComplete.apply {
            setText(items[0])
            setAdapter(adapter)
            setDropDownBackgroundDrawable(
                ContextCompat.getDrawable(
                    this@RecipeDetailActivity,
                    com.sam.topchef.R.drawable.bg_dropdown_dark
                )
            )
        }
        if(autoComplete.text.toString() == items[items.size - 1]){startActivity(TODO())}

        binding.txtRecipeTitle.text = recipeId.toString()
    }

    private inner class ImagesAdapter() : RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ImagesViewHolder {
            val view = layoutInflater.inflate(com.sam.topchef.R.layout.row_images_from_detail, parent, false)
            return ImagesViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ImagesViewHolder,
            position: Int
        ) {

        }

        override fun getItemCount(): Int {
            return 5
        }

        private inner class ImagesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            // TODO: (Not implemented)
        }
    }
}