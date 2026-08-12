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

/**
 * Activity for managing a specific shopping cart/list.
 * Allows users to view ingredients, mark them as purchased, and share the list.
 */
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

    /**
     * Initializes the cart view, sets up listeners for item creation, editing, and checking,
     * and handles the back press logic for exiting edit mode.
     */
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

    /**
     * Shows a confirmation dialog for deletion actions.
     * @param message The dialog message.
     * @param onResult Callback receiving the user's choice (OK or CANCELED).
     */
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

    /**
     * Removes all items that are marked as checked from the cart.
     */
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

    /**
     * Clears all items from the current cart.
     */
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

    /**
     * Unchecks all items in the cart.
     */
    private fun deselectItems() {
        cartItems.forEachIndexed { index, item ->
            if (item.isChecked) {
                item.isChecked = false
                cartItemAdapter.notifyItemChanged(index)
            }
        }
        saveChanges()
    }

    /**
     * Shares the current cart's items as a plain text string.
     */
    private fun shareCart() {
        val text = currentCart?.toShareText() ?: "Lista esta vazia :("
        Utils.shareText(this, text)
    }

    /**
     * Sorts the cart items alphabetically by name.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun sortItems() {
        cartItems.sortWith(
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.itemName }
        )
        cartItemAdapter.notifyDataSetChanged()
        saveChanges()
    }

    /**
     * Shows a bottom sheet dialog with batch actions for the cart items.
     */
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
    /**
     * Loads the items for a specific cart from the database.
     * @param id The cart ID.
     */
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
    /**
     * Updates the cart's data in the database.
     * @param cart The cart object to update.
     */
    private fun updateCart(cart: Cart) {
        thread {
            val dao = (application as App).db.cartDao()
            dao.update(cart)
        }
    }

    // Salva mudanças no carrinho
    /**
     * Saves the current list of items to the database.
     */
    private fun saveChanges() {
        val cart = currentCart ?: return
        val updated = cart.copy(cartItems = cartItems)

        currentCart = updated
        updateCart(updated)
    }

    /**
     * Exits the inline edit mode for cart items and hides the keyboard.
     */
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

    /**
     * Utility to show the soft keyboard for a specific view.
     */
    private fun showKeyboard(view: android.view.View) {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * Utility to hide the soft keyboard.
     */
    private fun hideKeyboard(view: android.view.View) {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


}