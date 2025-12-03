package com.sam.topchef.feature_shopping_list.activities

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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


    override fun onCartClick(id: Int) {
        val i = Intent(this, CartActivity::class.java)
        i.putExtra("id", id)
        startActivity(i)
    }

    override fun onCartTools(id: Int) {

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