package com.sam.topchef.feature_import_from_tiktok.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sam.topchef.core.utils.adapter.TextsAdapter
import com.sam.topchef.databinding.DialogRecipeDataBinding
import com.sam.topchef.feature_import_from_tiktok.adapter.TiktokStepsAdapter
import com.sam.topchef.feature_import_from_tiktok.model.TikTokModel
import com.sam.topchef.feature_recipe_detail.adapter.StepsAdapter
import com.sam.topchef.feature_recipe_detail.model.Step

class TiktokRecipeDataDialog : BottomSheetDialogFragment() {
    private var _binding: DialogRecipeDataBinding? = null
    private val binding get() = _binding!!

    private var recipeData: TikTokModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRecipeDataBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        recipeData?.let { recipe ->
            binding.txtRecipeTitle.text = recipe.name
            binding.txtRecipeDescription.text = recipe.description

            // Flattening ingredients from all sections
            val allIngredients = recipe.ingredients.flatMap { it.sectionItems }
            binding.rvIngredients.layoutManager = LinearLayoutManager(context)
            binding.rvIngredients.adapter = TextsAdapter(allIngredients)

            // Flattening steps from all sections
            val allSteps = recipe.preparationMode
            binding.rvSteps.layoutManager = LinearLayoutManager(context)
            binding.rvSteps.adapter = TiktokStepsAdapter(allSteps)
        }
    }

    fun setRecipeData(data: TikTokModel) {
        this.recipeData = data
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {
        const val TAG = "TiktokRecipeDataDialog"
        fun newInstance(data: TikTokModel): TiktokRecipeDataDialog {
            val dialog = TiktokRecipeDataDialog()
            dialog.setRecipeData(data)
            return dialog
        }
    }
}
