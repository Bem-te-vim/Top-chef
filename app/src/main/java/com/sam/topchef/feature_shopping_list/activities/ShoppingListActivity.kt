package com.sam.topchef.feature_shopping_list.activities

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.core.utils.Utils
import com.sam.topchef.core.utils.Utils.toShareText
import com.sam.topchef.databinding.ActivityShoppingListBinding
import com.sam.topchef.feature_fullscreen_image.FullscreenImageActivity
import com.sam.topchef.feature_shopping_list.adapter_interface.AdapterChanges
import com.sam.topchef.feature_shopping_list.adpters.CartsAdapter
import kotlin.concurrent.thread

/**
 * Activity for displaying all shopping lists (carts).
 * Provides access to existing lists and allows creation of new ones.
 */
class ShoppingListActivity : AppCompatActivity(), AdapterChanges {
    private lateinit var cartsAdapter: CartsAdapter
    private lateinit var binding: ActivityShoppingListBinding
    private var cartImage: String? = null

    private lateinit var result: ActivityResultLauncher<Intent>

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                setImageToCart(uri)
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }

    /**
     * Initializes the shopping list view, sets up the adapter for carts,
     * and handles image attachment for new carts.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        result =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    loadData()
                }
            }




        cartsAdapter = CartsAdapter(this)
        val rvCarts = binding.rvCarts
        rvCarts.layoutManager = LinearLayoutManager(this)
        rvCarts.adapter = cartsAdapter


        binding.btnRemoveImageToCart.setOnClickListener {
            removeImageToCart()
        }


        binding.btnAddImageToCart.setOnClickListener {
            pickImages.launch("image/*")
        }

        binding.btnCreateNewCart.setOnClickListener {
            val txtCreateNewCart = binding.createNewCart
            val cartName =
                txtCreateNewCart.text.toString().trim().ifEmpty { return@setOnClickListener }
            txtCreateNewCart.text.clear()

            val cart = Cart(title = cartName, cartImage = cartImage)
            removeImageToCart()
            createNewCart(cart)
        }


        binding.btnBack.setOnClickListener { finish() }

        loadData()
    }

    /**
     * Updates the UI to show the selected image for a new cart.
     * @param uri The URI of the selected image.
     */
    private fun setImageToCart(uri: Uri) {
        binding.btnRemoveImageToCart.visibility = View.VISIBLE
        val imageCart = binding.imageItemFromCart
        imageCart.visibility = View.VISIBLE
        LoadImages().loadImagesWithBlur(uri, imageCart)
        cartImage = uri.toString()
    }

    /**
     * Resets the image selection UI for new carts.
     */
    private fun removeImageToCart() {
        binding.btnRemoveImageToCart.visibility = View.GONE
        binding.imageItemFromCart.visibility = View.GONE
        cartImage = null
    }

    /**
     * Updates an existing cart's metadata in the database.
     * @param cart The cart to update.
     */
    private fun updateCart(cart: Cart) {
        thread {
            val dao = (application as App).db.cartDao()
            dao.update(cart)
        }
    }

    /**
     * Updates the cart in the database and notifies the adapter of the change.
     * @param updated The updated cart object.
     */
    private fun editCart(updated: Cart) {
        updateCart(updated)
        cartsAdapter.onItemChange(updated)
    }

    /**
     * Deletes a cart from the database by its ID.
     * @param id The ID of the cart to delete.
     */
    private fun deleteCart(id: Int) {
        thread {
            val dao = (application as App).db.cartDao()
            dao.delete(id)

            runOnUiThread {
                cartsAdapter.onDeleteNotify(id)

                Toast.makeText(this, "Carrinho deletado", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Retrieves a cart from the database and executes a callback with the result.
     * @param id The ID of the cart.
     * @param callback The callback function.
     */
    private fun getCart(id: Int, callback: (Cart) -> Unit) {
        thread {
            val dao = (application as App).db.cartDao()
            val cart = dao.getCart(id)

            if (cart != null) {
                runOnUiThread { callback(cart) }
            }
        }
    }

    /**
     * Shows a confirmation dialog before deleting a cart.
     * @param id The ID of the cart to delete.
     */
    private fun showDeleteCartDialog(id: Int) {
        getCart(id) { cart ->

            AlertDialog.Builder(this)

                .setTitle("Deletar: ${cart.title}?")

                .setNegativeButton("Cancelar") { p0, p1 -> p0.dismiss() }

                .setPositiveButton("Deletar") { p0, _ ->
                    deleteCart(id)
                    p0.dismiss()
                }
                .show()
        }


    }

    /**
     * Shows a dialog to edit the title of an existing cart.
     * @param id The ID of the cart to edit.
     */
    private fun showEditCart(id: Int) {
        getCart(id) { cart ->
            val editText = EditText(this)
            editText.hint = "Novo nome"
            AlertDialog.Builder(this)
                // todo: create custom view to this AlertDialog
                .setTitle("Editar: ${cart.title}?")
                .setView(editText)
                .setNegativeButton("Cancelar") { p0, p1 -> p0.dismiss() }

                .setPositiveButton("Ok") { p0, _ ->
                    val newCartName = editText.text.toString().trim()
                    if (newCartName.isNotEmpty()) {
                        val updated = cart.copy(title = newCartName)
                        editCart(updated)
                        p0.dismiss()
                    }
                }
                .show()
        }

    }

    /**
     * Shares the contents of a specific cart as plain text.
     * @param id The ID of the cart to share.
     */
    private fun shareCart(id: Int){
        getCart(id){
            val  text = it.toShareText()
            Utils.shareText(this, text )
        }
    }


    /**
     * Shows a bottom sheet with tools (edit, delete, share) for a specific cart.
     * @param id The ID of the cart.
     */
    private fun showBottomSheetsDialog(id: Int) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_tools_shopping_list, null)
        dialog.setContentView(view)
        dialog.show()


        val delete: LinearLayout = view.findViewById(R.id.tools_delete)
        val edit: LinearLayout = view.findViewById(R.id.tools_edit)
        val share: LinearLayout = view.findViewById(R.id.tools_share)

        delete.setOnClickListener {
            showDeleteCartDialog(id)
            dialog.dismiss()
        }

        edit.setOnClickListener {
            showEditCart(id)
            dialog.dismiss()
        }

        share.setOnClickListener {
            shareCart(id)
            dialog.dismiss()
        }

        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
    }


    /**
     * Callback when a cart is clicked. Navigates to the items view for that cart.
     * @param id The ID of the clicked cart.
     */
    override fun onCartClick(id: Int) {
        val i = Intent(this, CartActivity::class.java)
        i.putExtra("id", id)
        result.launch(i)
    }

    /**
     * Callback when the tools menu for a cart is requested.
     */
    override fun onCartTools(id: Int) {
        showBottomSheetsDialog(id)
    }

    /**
     * Callback when a cart's image is clicked. Opens the image in full screen.
     * @param imageUri The URI of the image.
     * @param view The view for transition animation.
     */
    override fun onCartImageClick(imageUri: String?, view: View) {
        val i = Intent(this, FullscreenImageActivity::class.java)
        i.putExtra("imageUri", imageUri)

        val options = ActivityOptions
            .makeSceneTransitionAnimation(
                (this),
                view,
                "image_transition"
            )
        startActivity(i, options.toBundle())
    }

    /**
     * Loads all saved carts from the database and updates the adapter.
     */
    private fun loadData() {
        thread {
            val app = application as App
            val dao = app.db.cartDao()
            val allCarts = dao.getAllCarts()

            if (allCarts.isEmpty()) return@thread

            runOnUiThread {
                cartsAdapter.setData(allCarts)
            }
        }
    }

    /**
     * Inserts a new cart into the database and updates the UI.
     * @param cart The new cart object.
     */
    fun createNewCart(cart: Cart) {
        thread {
            val app = application as App
            val dao = app.db.cartDao()
            val newId = dao.insert(cart).toInt()

            cart.id = newId
            runOnUiThread {
                cartsAdapter.setNewCart(cart)
            }

        }
    }
}