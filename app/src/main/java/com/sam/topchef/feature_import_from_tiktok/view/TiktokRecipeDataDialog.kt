package com.sam.topchef.feature_import_from_tiktok.view

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sam.topchef.core.data.local.appDataBase.AppDataBase
import com.sam.topchef.databinding.DialogRecipeDataBinding
import com.sam.topchef.feature_feed_main.ui.activity.MainActivity
import com.sam.topchef.feature_import_from_tiktok.adapter.TiktokIngredientsAdapter
import com.sam.topchef.feature_import_from_tiktok.adapter.TiktokStepsAdapter
import com.sam.topchef.feature_import_from_tiktok.model.TikTokModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TiktokRecipeDataDialog : BottomSheetDialogFragment() {
    private var _binding: DialogRecipeDataBinding? = null
    private val binding get() = _binding!!

    private var recipeData: TikTokModel? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.apply {
            setOnShowListener {
                findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
                )?.setBackgroundResource(android.R.color.transparent)
            }
        }
        return dialog
    }

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


            val allIngredients = recipe.ingredients
            binding.rvIngredients.layoutManager = LinearLayoutManager(context)
            binding.rvIngredients.adapter = TiktokIngredientsAdapter(allIngredients)


            val allSteps = recipe.preparationMode
            binding.rvSteps.layoutManager = LinearLayoutManager(context)
            binding.rvSteps.adapter = TiktokStepsAdapter(allSteps)

            binding.save.setOnClickListener {
                saveRecipe(recipe)
            }
        }
    }

    private fun saveRecipe(recipe: TikTokModel) {
        val db = AppDataBase.getDataBase(requireContext())
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.tiktokDao().insert(recipe)
            }
            dismiss()
            activity?.let {
                val intent = Intent(it, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                it.finish()
            }
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
