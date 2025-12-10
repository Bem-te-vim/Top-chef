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
import com.sam.topchef.databinding.ActivityShoppingListBinding
import com.sam.topchef.feature_fullscreen_image.FullscreenImageActivity
import com.sam.topchef.feature_shopping_list.adapter_interface.AdapterChanges
import com.sam.topchef.feature_shopping_list.adpters.CartsAdapter
import kotlin.concurrent.thread

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

    private fun setImageToCart(uri: Uri) {
        binding.btnRemoveImageToCart.visibility = View.VISIBLE
        val imageCart = binding.imageItemFromCart
        imageCart.visibility = View.VISIBLE
        LoadImages().loadImagesWithBlur(uri, imageCart)
        cartImage = uri.toString()
    }

    private fun removeImageToCart() {
        binding.btnRemoveImageToCart.visibility = View.GONE
        binding.imageItemFromCart.visibility = View.GONE
        cartImage = null
    }

    private fun updateCart(cart: Cart) {
        thread {
            val dao = (application as App).db.cartDao()
            dao.update(cart)
        }
    }

    private fun editCart(updated: Cart) {
        updateCart(updated)
        cartsAdapter.onItemChange(updated)
    }

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

    private fun getCart(id: Int, callback: (Cart) -> Unit) {
        thread {
            val dao = (application as App).db.cartDao()
            val cart = dao.getCart(id)

            if (cart != null) {
                runOnUiThread { callback(cart) }
            }
        }
    }

    private fun showDeleteCartDialog(id: Int) {
        getCart(id) { cart ->

            AlertDialog.Builder(this)
                // todo: create custom view to this AlertDialog

                .setTitle("Deletar: ${cart.title}?")

                .setNegativeButton("Cancelar") { p0, p1 -> p0.dismiss() }

                .setPositiveButton("Deletar") { p0, _ ->
                    deleteCart(id)
                    p0.dismiss()
                }
                .show()
        }


    }

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


        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
    }


    override fun onCartClick(id: Int) {
        val i = Intent(this, CartActivity::class.java)
        i.putExtra("id", id)
        result.launch(i)
    }

    override fun onCartTools(id: Int) {
        showBottomSheetsDialog(id)
    }

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