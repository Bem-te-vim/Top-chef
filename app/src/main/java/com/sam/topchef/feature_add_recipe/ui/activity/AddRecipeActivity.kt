package com.sam.topchef.feature_add_recipe.ui.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.core.data.model.Type
import com.sam.topchef.databinding.ActivityAddRecipeBinding
import com.sam.topchef.feature_add_recipe.adapter.RecipeDifficultAdapter
import kotlin.concurrent.thread

class AddRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddRecipeBinding
    private val difficultAdapter = RecipeDifficultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val imgCover = binding.imgCoverAddRecipe
        val btnAddImages = binding.btnAddImages

        val dbItems = mutableListOf<String>()
        thread {
            val app = application as App
            val typeDao = app.db.typeDao()
            val allTypes = typeDao.getAllTypes()

            runOnUiThread {
                val defaultLatItem = listOf(Type("Add+"))
                val all = listOf(allTypes.take(4), defaultLatItem).flatten()
                val items = mutableListOf<String>()
                all.forEach {
                    items.add(it.type)
                }
                dbItems.addAll(items)
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dbItems)

        val autoCompleteType = binding.autoCompleteType
        autoCompleteType.apply {
            setText(getString(R.string.type))
            setAdapter(adapter)
            setDropDownBackgroundDrawable(
                ContextCompat.getDrawable(
                    this@AddRecipeActivity,
                    R.drawable.bg_dropdown_dark
                )
            )
        }

        autoCompleteType.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = dbItems[position]
            if (selectedItem == "Add+") {
                //todo val dialogView = layoutInflater.inflate()
                val alertDialog = AlertDialog.Builder(this)

            }
        }


        val edtxTitle = binding.edtxRecipeTitle
        val characterCountTitle = binding.characterCountTitle
        setViewCount(edtxTitle, characterCountTitle, 45)


        val edtxDescription = binding.edtxRecipeDescription
        val characterCountDescription = binding.characterCountDescription
        setViewCount(edtxDescription, characterCountDescription, 300)


        val rv = binding.rvRecipeDifficult
        val layoutManager =
            object : LinearLayoutManager(this, HORIZONTAL, false) {
                override fun canScrollHorizontally(): Boolean {
                    return false
                }
            }
        rv.layoutManager = layoutManager
        rv.adapter = difficultAdapter


        binding.btnSave.setOnClickListener {
            val recipe = Recipe(
                title = edtxTitle.text.toString(),
                description = edtxDescription.text.toString(),
                difficult = difficultAdapter.getDifficultyLevel(),
            )
            thread {
                val app = application as App
                val dao = app.db.recipeDao()
                dao.insert(recipe)

                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Sua recita foi salva :)",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    //pega o length de um  editText e seta o valor em
    // um textview
    private fun setViewCount(editText: EditText, textView: TextView, maxValueCont: Int) {
        textView.text = getString(R.string.value_bar_value, 0, maxValueCont)
        editText.addTextChangedListener { text ->
            val length = text?.length ?: 0
            textView.text = getString(R.string.value_bar_value, length, maxValueCont)
        }
    }


}
