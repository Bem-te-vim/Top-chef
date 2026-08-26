package com.sam.topchef.feature_import_from_tiktok.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sam.topchef.R
import com.sam.topchef.core.utils.adapter.TextsAdapter
import com.sam.topchef.databinding.DialogEditTiktokRecipeBinding
import com.sam.topchef.databinding.DialogEditTextItemBinding
import com.sam.topchef.feature_import_from_tiktok.model.TikTokModel
import com.sam.topchef.feature_import_from_tiktok.model.TiktokSection
import com.sam.topchef.feature_import_from_tiktok.model.TiktokStep

class TiktokEditRecipeDialog : BottomSheetDialogFragment() {

    private var _binding: DialogEditTiktokRecipeBinding? = null
    private val binding get() = _binding!!

    private var recipeData: TikTokModel? = null

    private lateinit var ingredientsAdapter: TextsAdapter
    private lateinit var preparationAdapter: TextsAdapter

    private val ingredients = mutableListOf<String>()
    private val preparations = mutableListOf<String>()

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
        _binding = DialogEditTiktokRecipeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        recipeData?.let { recipe ->
            binding.edtxRecipeTitle.setText(recipe.name)
            binding.edtxRecipeDescription.setText(recipe.description)

            // Flatten ingredients
            ingredients.clear()
            recipe.ingredients.forEach { section ->
                ingredients.addAll(section.sectionItems)
            }

            // Flatten steps
            preparations.clear()
            recipe.preparationMode.forEach { step ->
                preparations.add(step.stepDesc)
            }

            setupAdapters()
            setupListeners()
            setupCharacterCounters()
        }
    }

    private fun setupAdapters() {
        ingredientsAdapter = TextsAdapter(ingredients, true)
        preparationAdapter = TextsAdapter(preparations, true)

        binding.rvIngredients.layoutManager = LinearLayoutManager(context)
        binding.rvIngredients.adapter = ingredientsAdapter

        binding.rvPreparation.layoutManager = LinearLayoutManager(context)
        binding.rvPreparation.adapter = preparationAdapter

        setupItemTouchHelper(binding.rvIngredients, ingredients, ingredientsAdapter)
        setupItemTouchHelper(binding.rvPreparation, preparations, preparationAdapter)
    }

    private fun setupListeners() {
        ingredientsAdapter.onDeleteItemClickListener = { position ->
            ingredients.removeAt(position)
            ingredientsAdapter.notifyItemRemoved(position)
        }

        preparationAdapter.onDeleteItemClickListener = { position ->
            preparations.removeAt(position)
            preparationAdapter.notifyItemRemoved(position)
        }

        ingredientsAdapter.onTextDoubleClickListener = { position ->
            showEditItemDialog(ingredients, ingredientsAdapter, position, "Editar Ingrediente")
        }

        preparationAdapter.onTextDoubleClickListener = { position ->
            showEditItemDialog(preparations, preparationAdapter, position, "Editar Passo")
        }

        binding.addIngredient.setOnClickListener {
            val ingredient = binding.edtxIngredient.text.toString().trim()
            if (ingredient.isNotEmpty()) {
                ingredients.add(ingredient)
                ingredientsAdapter.notifyItemInserted(ingredients.size - 1)
                binding.edtxIngredient.text.clear()
            }
        }

        binding.addStep.setOnClickListener {
            val step = binding.edtxStep.text.toString().trim()
            if (step.isNotEmpty()) {
                preparations.add(step)
                preparationAdapter.notifyItemInserted(preparations.size - 1)
                binding.edtxStep.text.clear()
            }
        }

        binding.btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun setupCharacterCounters() {
        setViewCount(binding.edtxRecipeTitle, binding.characterCountTitle, 45)
        setViewCount(binding.edtxRecipeDescription, binding.characterCountDescription, 300)
    }

    private fun setViewCount(editText: EditText, textView: TextView, maxValueCont: Int) {
        textView.text = getString(R.string.value_bar_value, editText.text.length, maxValueCont)
        editText.filters = arrayOf(InputFilter.LengthFilter(maxValueCont))

        editText.addTextChangedListener { text ->
            val length = text?.length ?: 0
            textView.text = getString(R.string.value_bar_value, length, maxValueCont)
        }
    }

    private fun setupItemTouchHelper(
        recyclerView: RecyclerView,
        list: MutableList<String>,
        adapter: TextsAdapter
    ) {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition

                java.util.Collections.swap(list, fromPosition, toPosition)
                adapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        }

        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    private fun showEditItemDialog(
        list: MutableList<String>,
        adapter: TextsAdapter,
        position: Int,
        title: String
    ) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogEditTextItemBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.txtDialogTitle.text = title
        dialogBinding.edtxEditItem.setText(list[position])
        dialogBinding.edtxEditItem.setSelection(dialogBinding.edtxEditItem.text.length)

        dialogBinding.btnSaveItem.setOnClickListener {
            val newText = dialogBinding.edtxEditItem.text.toString().trim()
            if (newText.isNotEmpty()) {
                list[position] = newText
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            } else {
                dialogBinding.edtxEditItem.error = "O campo não pode estar vazio"
            }
        }

        dialog.show()
    }

    private fun saveChanges() {
        recipeData?.let { recipe ->
            val updatedRecipe = recipe.copy(
                name = binding.edtxRecipeTitle.text.toString(),
                description = binding.edtxRecipeDescription.text.toString(),
                ingredients = listOf(TiktokSection("Ingredientes", ingredients.toList())),
                preparationMode = preparations.mapIndexed { index, s ->
                    TiktokStep("Passo ${index + 1}", s)
                }
            )
            (activity as? TiktokImportActivity)?.onRecipeUpdated(updatedRecipe)
            dismiss()
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
        const val TAG = "TiktokEditRecipeDialog"
        fun newInstance(data: TikTokModel): TiktokEditRecipeDialog {
            val dialog = TiktokEditRecipeDialog()
            dialog.setRecipeData(data)
            return dialog
        }
    }
}
