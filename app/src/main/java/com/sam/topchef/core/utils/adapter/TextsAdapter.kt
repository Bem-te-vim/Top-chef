package com.sam.topchef.core.utils.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TextsAdapter(private val texts: List<String>) :
    RecyclerView.Adapter<TextsAdapter.TextsViewHolder>() {

    var onTextClickListener: ((txt: String) -> Unit)? = null
    var onTextLongClickListener:((position: Int)-> Boolean)? = null

    inner class TextsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView = itemView as TextView
        fun bing(txt: String) {

            textView.text = "• $txt"
            textView.setOnClickListener { onTextClickListener?.invoke(txt) }
            textView.setOnLongClickListener {
                onTextLongClickListener?.invoke(adapterPosition)
                true }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TextsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
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