package com.sam.topchef.feature_shopping_list.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.databinding.ActivityCartBinding
import com.sam.topchef.feature_shopping_list.adpters.CartItemAdapter
import com.sam.topchef.feature_shopping_list.data.model.CartItem
import kotlin.concurrent.thread

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var cartItemAdapter: CartItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val i = intent
        val cartId = i.extras?.getInt("id") ?: throw NullPointerException()

        cartItemAdapter = CartItemAdapter()
        val rvCartItems = binding.rvCartItems
        rvCartItems.layoutManager = LinearLayoutManager(this)
        rvCartItems.adapter = cartItemAdapter

        binding.btnCreateNewCartItem.setOnClickListener {
            val txtCreateNewCartItem = binding.createNewCartItem
            val txtItem =
                txtCreateNewCartItem.text.toString().trim().ifEmpty { return@setOnClickListener }
            txtCreateNewCartItem.text.clear()

            val cartItem = CartItem(itemName = txtItem)
            setItems(cartItem)

            rvCartItems.smoothScrollToPosition(cartItemAdapter.getLastPosition())
        }

        binding.btnBack.setOnClickListener { finish() }

        loadData(cartId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun loadData(id: Int) {
        thread {
            val app = application as App
            val dao = app.db.cartDao()
            val cart = dao.getCart(id)

            if (cart.cartItems.isEmpty()) return@thread

            runOnUiThread {
                val cartItems = cart.cartItems
                cartItemAdapter.setData(cartItems)
            }
        }
    }

    private fun updateCart(cart: Cart) {
        thread {
            val app = application as App
            val dao = app.db.cartDao()
            val cart = dao.update(cart)
        }
    }

    private fun setItems(item: CartItem) {
        cartItemAdapter.setItem(item)
    }


}