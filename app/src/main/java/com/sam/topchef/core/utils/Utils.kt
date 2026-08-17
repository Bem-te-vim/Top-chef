package com.sam.topchef.core.utils

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
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

    fun View.hide(){
        this.visibility = View.GONE
    }

    fun View.show() {
        this.visibility = View.VISIBLE
    }

    fun View.clickAnimation(){
        this.animate().scaleX(0.99f).scaleY(0.99f).setDuration(100).withEndAction {
            animate().scaleX(1f).scaleY(1f).duration = 100
        }
    }

    /**
     * Set a listener that distinguishes between single and double clicks to avoid conflicts.
     * @param delay Time to wait for a second click in milliseconds.
     * @param onSingleClick Callback for single click.
     * @param onDoubleClick Callback for double click.
     */
    fun View.setClicksListener(
        delay: Long = 300L,
        onSingleClick: (View) -> Unit,
        onDoubleClick: (View) -> Unit
    ) {
        var clickCount = 0
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            if (clickCount == 1) {
                onSingleClick(this)
            } else if (clickCount >= 2) {
                onDoubleClick(this)
            }
            clickCount = 0
        }

        this.setOnClickListener {
            clickCount++
            if (clickCount == 1) {
                handler.postDelayed(runnable, delay)
            }
        }
    }
}
