package com.sam.topchef.feature_shopping_list.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.core.utils.Utils.swap
import com.sam.topchef.databinding.ActivityCartBinding
import com.sam.topchef.feature_shopping_list.adpters.CartItemAdapter
import com.sam.topchef.feature_shopping_list.data.model.CartItem
import kotlin.concurrent.thread

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var cartItemAdapter: CartItemAdapter

    private val cartItems = mutableListOf<CartItem>()
    private var currentCart: Cart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val cartId = intent.extras?.getInt("id") ?: throw NullPointerException()

        cartItemAdapter = CartItemAdapter(cartItems)

        val rvCartItems = binding.rvCartItems
        rvCartItems.layoutManager = LinearLayoutManager(this)
        rvCartItems.adapter = cartItemAdapter

        // Criar novo item
        binding.btnCreateNewCartItem.setOnClickListener {
            val txtItem = binding.createNewCartItem.text.toString().trim()
            if (txtItem.isEmpty()) return@setOnClickListener

            val newItem = CartItem(itemName = txtItem)
            binding.createNewCartItem.text.clear()

            cartItems.add(newItem)
            cartItemAdapter.notifyItemInserted(cartItems.size -1)

            saveChanges()

            rvCartItems.smoothScrollToPosition(cartItemAdapter.itemCount - 1)
            setResult(RESULT_OK)
        }

        cartItemAdapter.onCartItemChecked = { checkBoxState, itemPosition ->
            cartItems[itemPosition].isChecked = checkBoxState
            if(checkBoxState){
                cartItems.swap(itemPosition, cartItems.lastIndex)
                cartItemAdapter.notifyItemMoved(itemPosition, cartItems.lastIndex)
            }else{
                cartItems.swap(itemPosition, 0)
                cartItemAdapter.notifyItemMoved(itemPosition, 0)
            }
            saveChanges()
        }

        binding.btnBack.setOnClickListener { finish() }

        loadData(cartId)
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
}