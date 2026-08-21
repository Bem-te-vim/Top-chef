package com.sam.topchef.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.core.data.model.Recipe
import com.sam.topchef.feature_import_from_tiktok.model.TikTokModel
import kotlin.time.Duration

object Utils {
    /**
     * Swaps two elements in a mutable list.
     */
    fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
        val element = this[index1]
        this.removeAt(index1)
        this.add(index2, element)
    }


    /**
     * Converts a [Cart] object into a formatted string for sharing.
     */
    fun Cart.toShareText(): String {
        val builder = StringBuilder()

        builder.append(title)
        builder.append("\n\n")

        cartItems.forEachIndexed { index, item ->
            builder.append("${index + 1}. ${item.itemName}\n")
        }

        return builder.toString()
    }

    /**
     * Converts a [Recipe] object into a formatted string for sharing, including ingredients and steps.
     */
    fun Recipe.toShareText(): String {
        val builder = StringBuilder()
        builder.append("*${title}*\n")
        description?.let { builder.append("${it}\n\n") }

        builder.append("*Ingredientes:*\n")
        ingredients.forEach { builder.append("- ${it}\n") }

        builder.append("\n*Modo de Preparo:*\n")
        preparationMode.forEachIndexed { index, step ->
            builder.append("${index + 1}. $step\n")
        }

        return builder.toString()
    }

    /**
     * Converts a [TikTokModel] object into a formatted string for sharing, including original URL and sections.
     */
    fun TikTokModel.toShareText(): String {
        val builder = StringBuilder()
        builder.append("*${name}*\n")
        builder.append("${description}\n\n")

        builder.append("*Ingredientes:*\n")
        ingredients.forEach { section ->
            if (section.sectionName.isNotEmpty()) builder.append("\n[${section.sectionName}]\n")
            section.sectionItems.forEach { builder.append("- ${it}\n") }
        }

        builder.append("\n*Modo de Preparo:*\n")
        preparationMode.forEachIndexed { index, step ->
            if (step.stepName.isNotEmpty()) builder.append("\n${step.stepName}:\n")
            builder.append("${index + 1}. ${step.stepDesc}\n")
        }

        originUrl?.let { builder.append("\nVeja o vídeo original: $it") }

        return builder.toString()
    }

    /**
     * Opens the Android share sheet to share the provided text.
     */
    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        context.startActivity(
            Intent.createChooser(intent, "Compartilhar via")
        )
    }

    /**
     * Hides a ProgressBar.
     */
    fun ProgressBar.hide() {
        this.visibility = View.GONE
    }

    /**
     * Shows a ProgressBar.
     */
    fun ProgressBar.show() {
        this.visibility = View.VISIBLE
    }

    /**
     * Hides any View.
     */
    fun View.hide(){
        this.visibility = View.GONE
    }

    /**
     * Shows any View.
     */
    fun View.show() {
        this.visibility = View.VISIBLE
    }

    /**
     * Performs a subtle scale animation on a View to provide click feedback.
     */
    fun View.clickAnimation(defaultAnimationScale: Float = 1f,
                            startAnimationScale: Float = 0.99f,
                            startDuration: Long = 100,
                            endDuration: Long = 100
                            ){
        this.animate().scaleX(startAnimationScale).scaleY(startAnimationScale).setDuration(startDuration).withEndAction {
            animate().scaleX(defaultAnimationScale).scaleY(defaultAnimationScale).duration = endDuration
        }
    }

    /**
     * Set a listener that distinguishes between single click, double click, hold, and release to avoid conflicts.
     * @param delay Time to wait for a second click in milliseconds.
     * @param onSingleClick Callback for single click.
     * @param onDoubleClick Callback for double click.
     * @param onHold Callback for long press start.
     * @param onRelease Callback for long press release.
     */
    @SuppressLint("ClickableViewAccessibility")
    fun View.setClicksListener(
        delay: Long = 300L,
        onSingleClick: (View) -> Unit = {},
        onDoubleClick: (View) -> Unit = {},
        onHold: (View) -> Unit = {},
        onRelease: (View) -> Unit = {}
    ) {
        var clickCount = 0
        var isHolding = false
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

        this.setOnLongClickListener {
            isHolding = true
            onHold(this)
            true
        }

        this.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isHolding) {
                        isHolding = false
                        onRelease(v)
                    }
                }
            }
            false
        }
    }
}
