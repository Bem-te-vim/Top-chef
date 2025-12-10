package com.sam.topchef.feature_shopping_list.adpters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sam.topchef.R
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.feature_shopping_list.adapter_interface.AdapterChanges

class CartsAdapter(val adapterChanges: AdapterChanges) :
    RecyclerView.Adapter<CartsAdapter.CartsViewHolder>() {

    private val carts  = mutableListOf<Cart>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: List<Cart>){
        carts.clear()
        carts.addAll(items)
        notifyDataSetChanged()
    }

    fun setNewCart(item: Cart){
        carts.add(item)
        notifyItemInserted(carts.lastIndex)
    }


    inner class CartsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val context = itemView.context

        val cartTitle: TextView = view.findViewById(R.id.cart_title)
        val itemsCount: TextView = view.findViewById(R.id.items_count)
        val cartImage: ShapeableImageView = view.findViewById(R.id.image_item_from_cart)

        fun bind(item: Cart) {
            LoadImages().loadImagesWithBlur(
                item.cartImage,
                cartImage,
                R.drawable.shopping_cart_24dp
            )

            cartImage.setOnClickListener {
             adapterChanges.onCartImageClick(item.cartImage, it)
            }
            cartTitle.text = item.title

            itemsCount.text = context.getString(R.string.qtn_items, item.cartItems.size)


            itemView.setOnClickListener { adapterChanges.onCartClick(item.id) }
            itemView.setOnLongClickListener {
                adapterChanges.onCartTools(item.id)
                true
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_cart, parent, false)
        return CartsViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CartsViewHolder,
        position: Int
    ) {
        val item = carts[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return carts.size
    }

   fun onDeleteNotify(id: Int){
       val index = carts.indexOfFirst { it.id == id }

       if (index != -1) {
           carts.removeAt(index)
           notifyItemRemoved(index)
       }
   }

    fun onItemChange(itemUpdated: Cart){
        val index = carts.indexOfFirst { it.id == itemUpdated.id }

        if (index != -1) {
            carts[index] = itemUpdated
            notifyItemChanged(index)
        }
    }


}