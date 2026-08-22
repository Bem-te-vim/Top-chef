package com.sam.topchef.core.utils.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sam.topchef.R
import com.sam.topchef.core.utils.Utils.setClicksListener

class TextsAdapter(private val texts: List<String>, var showDeleteBottom: Boolean = false) :
    RecyclerView.Adapter<TextsAdapter.TextsViewHolder>() {

    var onTextClickListener: ((txt: String) -> Unit)? = null
    var onTextLongClickListener: ((position: Int) -> Boolean)? = null
    var onTextDoubleClickListener: ((position: Int) -> Unit)? = null

    var onDeleteItemClickListener: ((position: Int) -> Unit)? = null

    inner class TextsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.text1)
        val btnDeleteItem: ImageButton = view.findViewById(R.id.btn_delete_item)

        init {
            if (showDeleteBottom) btnDeleteItem.visibility =
                View.VISIBLE else btnDeleteItem.visibility = View.GONE
        }

        fun bing(txt: String) {
            textView.setTextColor(itemView.context.getColor(R.color.WhiteForTxt))
            textView.text = "• $txt"
            
            textView.setClicksListener(
                onSingleClick = { onTextClickListener?.invoke(txt) },
                onDoubleClick = { onTextDoubleClickListener?.invoke(bindingAdapterPosition) }
            )

            btnDeleteItem.setOnClickListener { onDeleteItemClickListener?.invoke(bindingAdapterPosition) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TextsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_simple_list_item_1_custom, parent, false)
        return TextsViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TextsViewHolder,
        position: Int
    ) {
        val txt = texts[position]
        holder.bing(txt)
    }

    override fun getItemCount(): Int {
        return texts.size
    }


}