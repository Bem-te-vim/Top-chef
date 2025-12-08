package com.sam.topchef.feature_shopping_list.adpters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.feature_shopping_list.data.model.CartItem

class CartItemAdapter() :
    RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    var onCartItemChecked: ((checkBoxState: Boolean, itemPosition: Int) -> Unit)? = null

    private val cartItems = mutableListOf<CartItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(items)
        notifyDataSetChanged()
    }

    fun setItem(cartItem: CartItem) {
        cartItems.add(cartItem)
        notifyItemInserted(cartItems.lastIndex)
    }


    inner class CartItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val context = itemView.context
        val itemName: TextView = view.findViewById(R.id.cart_item_name)
        val itemCheckBox: CheckBox = view.findViewById(R.id.cart_item_checkBox)

        fun bind(item: CartItem) {
            itemName.text = item.itemName
            itemCheckBox.isChecked = item.isChecked
            setTextColor(item.isChecked, itemName, context)

            itemCheckBox.setOnClickListener {
                item.isChecked = !item.isChecked
                setTextColor(item.isChecked, itemName, context)
                onCartItemChecked?.invoke(item.isChecked, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_checked_item, parent, false)
        return CartItemViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CartItemViewHolder,
        position: Int
    ) {
        val item = cartItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    private fun setTextColor(isChecked: Boolean, textView: TextView, context: Context) {
        if (isChecked) {
            textView.setTextColor(context.getColor(R.color.default_color_app))
        } else {
            textView.setTextColor(context.getColor(R.color.WhiteForTxt))
        }
    }

}