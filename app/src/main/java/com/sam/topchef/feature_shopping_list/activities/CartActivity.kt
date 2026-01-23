package com.sam.topchef.feature_shopping_list.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.core.utils.Utils
import com.sam.topchef.core.utils.Utils.swap
import com.sam.topchef.core.utils.Utils.toShareText
import com.sam.topchef.databinding.ActivityCartBinding
import com.sam.topchef.feature_shopping_list.adpters.CartItemAdapter
import com.sam.topchef.feature_shopping_list.data.model.CartItem
import kotlin.concurrent.thread

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var cartItemAdapter: CartItemAdapter
    private lateinit var backCallback: OnBackPressedCallback


    private val cartItems = mutableListOf<CartItem>()
    private var currentCart: Cart? = null

    private var editableState: Boolean = false
    private var editingPosition: Int? = null


    companion object {
        private const val OK = 1
        private const val CANCELED = 2
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val cartId = intent.extras?.getInt("id") ?: throw NullPointerException()


        backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                exitEditMode()
            }
        }

        onBackPressedDispatcher.addCallback(this, backCallback)


        cartItemAdapter = CartItemAdapter(cartItems)

        val rvCartItems = binding.rvCartItems
        rvCartItems.layoutManager = LinearLayoutManager(this)
        rvCartItems.adapter = cartItemAdapter

        // Criar novo item
        binding.btnCreateNewCartItem.setOnClickListener {
            val text = binding.createNewCartItem.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            if (editableState && editingPosition != null) {
                // ✏️ EDITAR ITEM
                cartItems[editingPosition!!].itemName = text
                cartItemAdapter.notifyItemChanged(editingPosition!!)
                saveChanges()
                exitEditMode()
            } else {
                // ➕ NOVO ITEM
                val newItem = CartItem(itemName = text)
                cartItems.add(newItem)
                cartItemAdapter.notifyItemInserted(cartItems.size - 1)
                saveChanges()
                binding.createNewCartItem.text.clear()
                binding.rvCartItems.smoothScrollToPosition(cartItems.lastIndex)
                setResult(RESULT_OK)
            }
        }

        cartItemAdapter.onCartItemLongClickListener = { position ->
            editableState = true
            editingPosition = position
            backCallback.isEnabled = true

            cartItemAdapter.editingPosition = position
            cartItemAdapter.notifyItemChanged(position)

            binding.rvCartItems.smoothScrollToPosition(position)


            binding.btnCreateNewCartItem.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.edit_24dp
                )
            )




            val editText = binding.createNewCartItem
            editText.setText(cartItems[position].itemName)
            editText.requestFocus()
            editText.setSelection(editText.text.length)

            showKeyboard(editText)
        }



        cartItemAdapter.onCartItemChecked = { checkBoxState, itemPosition ->
            exitEditMode()
            cartItems[itemPosition].isChecked = checkBoxState
            if (checkBoxState) {
                cartItems.swap(itemPosition, cartItems.lastIndex)
                cartItemAdapter.notifyItemMoved(itemPosition, cartItems.lastIndex)
            } else {
                cartItems.swap(itemPosition, 0)
                cartItemAdapter.notifyItemMoved(itemPosition, 0)
            }
            saveChanges()
        }



        binding.btnBack.setOnClickListener { finish() }
        binding.btnMoreOptions.setOnClickListener {
            showBottomSheetsDialog()
        }

        loadData(cartId)
    }

    private fun showDeleteDialog(message: String = "Deletar?", onResult: (Int) -> Unit) {
        AlertDialog.Builder(this)

            .setTitle(message)
            .setNegativeButton("Cancelar") { p0, p1 ->
                onResult(CANCELED)
                p0.dismiss()
            }

            .setPositiveButton("Ok") { p0, _ ->
                onResult(OK)
                p0.dismiss()
            }
            .show()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteSelectedItems() {
        showDeleteDialog("Deletar items selecionados?") { userAction ->
            if (userAction == OK) {
                cartItems.removeAll { it.isChecked }
                cartItemAdapter.notifyDataSetChanged()
                saveChanges()

                setResult(RESULT_OK)
                Toast.makeText(this, "Items deletados", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteAllItems() {

        showDeleteDialog("Deletar todos os items?") { userAction ->
            if (userAction == OK) {
                cartItems.clear()
                cartItemAdapter.notifyDataSetChanged()
                saveChanges()

                setResult(RESULT_OK)
                Toast.makeText(this, "Items deletados", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deselectItems() {
        cartItems.forEachIndexed { index, item ->
            if (item.isChecked) {
                item.isChecked = false
                cartItemAdapter.notifyItemChanged(index)
            }
        }
        saveChanges()
    }

    private fun shareCart() {
        val text = currentCart?.toShareText() ?: "Lista esta vazia :("
        Utils.shareText(this, text)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortItems() {
        cartItems.sortWith(
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.itemName }
        )
        cartItemAdapter.notifyDataSetChanged()
        saveChanges()
    }

    private fun showBottomSheetsDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_tools_cart_item, null)
        dialog.setContentView(view)
        dialog.show()


        val deleteSelectedItems: LinearLayout = view.findViewById(R.id.tools_delete_selected_items)
        val deleteAllItems: LinearLayout = view.findViewById(R.id.tools_delete_all)
        val deselectItems: LinearLayout = view.findViewById(R.id.tools_deselect_items)
        val share: LinearLayout = view.findViewById(R.id.tools_share)
        val sortItems: LinearLayout = view.findViewById(R.id.tools_sort_items)

        deleteSelectedItems.setOnClickListener {
            deleteSelectedItems()
            dialog.dismiss()
        }

        deleteAllItems.setOnClickListener {
            deleteAllItems()
            dialog.dismiss()
        }

        deselectItems.setOnClickListener {
            deselectItems()
            dialog.dismiss()
        }

        share.setOnClickListener {
            shareCart()
            dialog.dismiss()
        }

        sortItems.setOnClickListener {
            sortItems()
            dialog.dismiss()
        }

        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
    }

    // Carrega os dados do carrinho no início
    @SuppressLint("NotifyDataSetChanged")
    private fun loadData(id: Int) {
        thread {
            val dao = (application as App).db.cartDao()
            val cart = dao.getCart(id)

            runOnUiThread {
                currentCart = cart // ← Salva o carrinho atual
                cartItems.clear()
                cartItems.addAll(cart.cartItems)
                cartItemAdapter.notifyDataSetChanged()
            }
        }
    }

    // Atualiza o carrinho no banco
    private fun updateCart(cart: Cart) {
        thread {
            val dao = (application as App).db.cartDao()
            dao.update(cart)
        }
    }

    // Salva mudanças no carrinho
    private fun saveChanges() {
        val cart = currentCart ?: return
        val updated = cart.copy(cartItems = cartItems)

        currentCart = updated
        updateCart(updated)
    }

    private fun exitEditMode() {
        editableState = false
        editingPosition = null

        cartItemAdapter.editingPosition?.let {
            cartItemAdapter.notifyItemChanged(it)
        }
        cartItemAdapter.editingPosition = null


        binding.btnCreateNewCartItem.setImageDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.baseline_add_24
            )
        )
        val editText = binding.createNewCartItem
        editText.text.clear()
        editText.clearFocus()

        hideKeyboard(editText)

        backCallback.isEnabled = false
    }

    private fun showKeyboard(view: android.view.View) {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: android.view.View) {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


}