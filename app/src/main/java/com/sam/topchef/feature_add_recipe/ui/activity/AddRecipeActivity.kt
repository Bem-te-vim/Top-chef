package com.sam.topchef.feature_add_recipe.ui.activity

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.R
import com.sam.topchef.databinding.ActivityAddRecipeBinding
import com.sam.topchef.feature_add_recipe.adapter.RecipeDifficultAdapter

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding
    private val difficultAdapter = RecipeDifficultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imgCover = binding.imgCoverAddRecipe
        val btnAddImages = binding.btnAddImages

        val edtxTitle = binding.edtxRecipeTitle
        val characterCountTitle  = binding.characterCountTitle
        setViewCount(edtxTitle, characterCountTitle, 45)

        val edtxDescription = binding.edtxRecipeDescription
        val characterCountDescription = binding.characterCountDescription
        setViewCount(edtxDescription,characterCountDescription, 300)

        val rv = binding.rvRecipeDifficult
        val layoutManager =
            object : LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) {
                override fun canScrollHorizontally(): Boolean {
                    return false
                }
            }
        rv.layoutManager = layoutManager
        rv.adapter = difficultAdapter

        difficultAdapter.getDifficultyLevel = {level ->
            val difficultLevel = binding.difficultLevel
            when(level){
                1 -> difficultLevel.setText(R.string.very_easy)
                2 -> difficultLevel.setText(R.string.easy)
                3 -> difficultLevel.setText(R.string.average)
                4 -> difficultLevel.setText(R.string.hard)
                5 -> difficultLevel.setText(R.string.very_hard)
            }
        }

    }
    //pega o length de um  editText e seta o valor em um textview
    private fun setViewCount(editText: EditText, textView: TextView, maxValueCont: Int){
        textView.text = getString(R.string.value_bar_value, 0, maxValueCont  )
        editText.addTextChangedListener { text ->
            val length = text?.length ?: 0
            textView.text = getString(R.string.value_bar_value, length, maxValueCont )
        }
    }
}
