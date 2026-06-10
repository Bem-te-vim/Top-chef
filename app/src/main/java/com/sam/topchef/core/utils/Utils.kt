package com.sam.topchef.core.utils

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import com.sam.topchef.core.data.model.Cart

object Utils {
    fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
        val element = this[index1]
        this.removeAt(index1)
        this.add(index2, element)
    }


    fun Cart.toShareText(): String {
        val builder = StringBuilder()

        builder.append(title)
        builder.append("\n\n")

        cartItems.forEachIndexed { index, item ->
            builder.append("${index + 1}. ${item.itemName}\n")
        }

        return builder.toString()
    }

    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        context.startActivity(
            Intent.createChooser(intent, "Compartilhar via")
        )
    }

    fun ProgressBar.hide() {
        this.visibility = View.GONE
    }

    fun ProgressBar.show() {
        this.visibility = View.VISIBLE
    }
}
