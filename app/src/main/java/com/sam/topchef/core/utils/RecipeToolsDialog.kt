package com.sam.topchef.core.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sam.topchef.R
import com.sam.topchef.core.data.local.appDataBase.AppDataBase
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.feature_shopping_list.data.model.CartItem
import com.sam.topchef.core.utils.Utils.toShareText
import com.sam.topchef.feature_edit_recipe.EditRecipeActivity
import com.sam.topchef.feature_import_from_tiktok.view.TiktokImportActivity
import com.sam.topchef.feature_import_from_tudogostoso.activities.TudoGostosoImportActivity
import com.sam.topchef.feature_import_from_tudogostoso.model.WebRecipeModel
import com.sam.topchef.feature_recipe_detail.ui.activity.RecipeDetailActivity
import com.sam.topchef.feature_shopping_list.activities.ShoppingListActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Common class that opens a dialog with tools/actions for recipes on long click.
 * Works for standard App Recipes, TikTok Recipes, and Web / TudoGostoso Recipes.
 */
object RecipeToolsDialog {

    sealed class RecipeTarget {
        /**
         * Standard App Recipe saved in local database.
         */
        data class AppRecipe(val id: Int) : RecipeTarget()

        /**
         * TikTok Recipe saved in local database.
         */
        data class TikTokRecipe(val id: Int) : RecipeTarget()

        /**
         * Web / TudoGostoso Recipe from external link.
         */
        data class WebRecipe(
            val title: String,
            val urlPath: String,
            val imageUrl: String? = null
        ) : RecipeTarget()
    }

    class Builder(private val context: Context) {
        private var target: RecipeTarget? = null
        private var onDeletedListener: (() -> Unit)? = null
        private var onEditedListener: (() -> Unit)? = null
        private var onMovedToCartListener: (() -> Unit)? = null

        fun setTarget(target: RecipeTarget) = apply {
            this.target = target
        }

        fun setAppRecipe(id: Int) = apply {
            this.target = RecipeTarget.AppRecipe(id)
        }

        fun setTikTokRecipe(id: Int) = apply {
            this.target = RecipeTarget.TikTokRecipe(id)
        }

        fun setWebRecipe(title: String, urlPath: String, imageUrl: String? = null) = apply {
            this.target = RecipeTarget.WebRecipe(title, urlPath, imageUrl)
        }

        fun setWebRecipe(webRecipe: WebRecipeModel) = apply {
            this.target = RecipeTarget.WebRecipe(webRecipe.title, webRecipe.recipeLinksPath, webRecipe.imageUrl)
        }

        fun setOnDeletedListener(listener: () -> Unit) = apply {
            this.onDeletedListener = listener
        }

        fun setOnEditedListener(listener: () -> Unit) = apply {
            this.onEditedListener = listener
        }

        fun setOnMovedToCartListener(listener: () -> Unit) = apply {
            this.onMovedToCartListener = listener
        }

        fun show() {
            val recipeTarget = target ?: return
            showDialog(
                context = context,
                target = recipeTarget,
                onDeleted = onDeletedListener,
                onEdited = onEditedListener,
                onMovedToCart = onMovedToCartListener
            )
        }
    }

    /**
     * Helper method to show tools dialog by recipe ID and isTikTok flag.
     */
    fun show(
        context: Context,
        id: Int,
        isTikTok: Boolean = false,
        onDeleted: (() -> Unit)? = null,
        onEdited: (() -> Unit)? = null,
        onMovedToCart: (() -> Unit)? = null
    ) {
        val target = if (isTikTok) RecipeTarget.TikTokRecipe(id) else RecipeTarget.AppRecipe(id)
        showDialog(context, target, onDeleted, onEdited, onMovedToCart)
    }

    /**
     * Helper method to show tools dialog for a Web / TudoGostoso recipe.
     */
    fun show(
        context: Context,
        webRecipe: WebRecipeModel,
        onDeleted: (() -> Unit)? = null
    ) {
        val target = RecipeTarget.WebRecipe(webRecipe.title, webRecipe.recipeLinksPath, webRecipe.imageUrl)
        showDialog(context, target, onDeleted, null, null)
    }

    /**
     * Helper method to show tools dialog for any [RecipeTarget].
     */
    fun show(
        context: Context,
        target: RecipeTarget,
        onDeleted: (() -> Unit)? = null,
        onEdited: (() -> Unit)? = null,
        onMovedToCart: (() -> Unit)? = null
    ) {
        showDialog(context, target, onDeleted, onEdited, onMovedToCart)
    }

    /**
     * Shows tools BottomSheetDialog for any [RecipeTarget].
     */
    fun showDialog(
        context: Context,
        target: RecipeTarget,
        onDeleted: (() -> Unit)? = null,
        onEdited: (() -> Unit)? = null,
        onMovedToCart: (() -> Unit)? = null
    ) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_tools_bottom_sheet, null)
        dialog.setContentView(view)

        val open: LinearLayout? = view.findViewById(R.id.tools_open)
        val delete: LinearLayout = view.findViewById(R.id.tools_delete)
        val edit: LinearLayout = view.findViewById(R.id.tools_edit)
        val share: LinearLayout = view.findViewById(R.id.tools_share)
        val moveToCart: LinearLayout = view.findViewById(R.id.tools_move_to_cart)

        when (target) {
            is RecipeTarget.AppRecipe -> {
                open?.visibility = View.VISIBLE
                edit.visibility = View.VISIBLE
                moveToCart.visibility = View.VISIBLE
                delete.visibility = View.VISIBLE
            }
            is RecipeTarget.TikTokRecipe -> {
                open?.visibility = View.VISIBLE
                edit.visibility = View.GONE
                moveToCart.visibility = View.GONE
                delete.visibility = View.VISIBLE
            }
            is RecipeTarget.WebRecipe -> {
                open?.visibility = View.VISIBLE
                edit.visibility = View.GONE
                moveToCart.visibility = View.GONE
                delete.visibility = View.GONE
            }
        }

        open?.setOnClickListener {
            dialog.dismiss()
            openRecipe(context, target)
        }

        delete.setOnClickListener {
            dialog.dismiss()
            confirmAndDeleteRecipe(context, target, onDeleted)
        }

        edit.setOnClickListener {
            dialog.dismiss()
            if (target is RecipeTarget.AppRecipe) {
                startActivityEditor(context, target.id)
                onEdited?.invoke()
            }
        }

        share.setOnClickListener {
            dialog.dismiss()
            shareRecipe(context, target)
        }

        moveToCart.setOnClickListener {
            dialog.dismiss()
            if (target is RecipeTarget.AppRecipe) {
                moveIngredientsToCart(context, target.id, onMovedToCart)
            }
        }

        dialog.show()
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
    }

    private fun openRecipe(context: Context, target: RecipeTarget) {
        when (target) {
            is RecipeTarget.AppRecipe -> {
                val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                    putExtra("id", target.id)
                }
                context.startActivity(intent)
            }
            is RecipeTarget.TikTokRecipe -> {
                val intent = Intent(context, TiktokImportActivity::class.java).apply {
                    putExtra("tiktokId", target.id)
                }
                context.startActivity(intent)
            }
            is RecipeTarget.WebRecipe -> {
                val intent = Intent(context, TudoGostosoImportActivity::class.java).apply {
                    putExtra("urlPath", target.urlPath)
                }
                context.startActivity(intent)
            }
        }
    }

    private fun confirmAndDeleteRecipe(
        context: Context,
        target: RecipeTarget,
        onDeleted: (() -> Unit)?
    ) {
        AlertDialog.Builder(context)
            .setTitle("Deletar essa receita?")
            .setNegativeButton("Cancelar") { p0, _ -> p0.dismiss() }
            .setPositiveButton("Deletar") { p0, _ ->
                p0.dismiss()
                deleteRecipeFromDb(context, target, onDeleted)
            }
            .show()
    }

    private fun deleteRecipeFromDb(
        context: Context,
        target: RecipeTarget,
        onDeleted: (() -> Unit)?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDataBase.getDataBase(context)
            when (target) {
                is RecipeTarget.AppRecipe -> {
                    db.recipeDao().delete(target.id)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Receita deletada", Toast.LENGTH_SHORT).show()
                        onDeleted?.invoke()
                    }
                }
                is RecipeTarget.TikTokRecipe -> {
                    db.tiktokDao().delete(target.id)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Receita do TikTok deletada", Toast.LENGTH_SHORT).show()
                        onDeleted?.invoke()
                    }
                }
                is RecipeTarget.WebRecipe -> {
                    withContext(Dispatchers.Main) {
                        onDeleted?.invoke()
                    }
                }
            }
        }
    }

    private fun startActivityEditor(context: Context, recipeId: Int) {
        val intent = Intent(context, EditRecipeActivity::class.java).apply {
            putExtra("id", recipeId)
        }
        context.startActivity(intent)
    }

    private fun shareRecipe(context: Context, target: RecipeTarget) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDataBase.getDataBase(context)
            val shareText = when (target) {
                is RecipeTarget.AppRecipe -> {
                    db.recipeDao().getRecipe(target.id)?.toShareText()
                }
                is RecipeTarget.TikTokRecipe -> {
                    db.tiktokDao().getById(target.id)?.toShareText()
                }
                is RecipeTarget.WebRecipe -> {
                    "*${target.title}*\n${target.urlPath}"
                }
            }
            shareText?.let { text ->
                withContext(Dispatchers.Main) {
                    Utils.shareText(context, text)
                }
            }
        }
    }

    private fun moveIngredientsToCart(
        context: Context,
        recipeId: Int,
        onMovedToCart: (() -> Unit)?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDataBase.getDataBase(context)
            val recipe = db.recipeDao().getRecipe(recipeId)

            if (recipe != null) {
                val cartItems = recipe.ingredients.map { CartItem(itemName = it) }
                val newCart = Cart(
                    title = recipe.title,
                    cartImage = recipe.imageUriString.firstOrNull(),
                    cartItems = cartItems
                )
                val cartId = db.cartDao().insert(newCart).toInt()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ingredientes movidos para o carrinho!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, ShoppingListActivity::class.java).apply {
                        putExtra("id", cartId)
                    }
                    context.startActivity(intent)
                    onMovedToCart?.invoke()
                }
            }
        }
    }
}

/**
 * Extension function to attach long click listener for RecipeToolsDialog to any View.
 */
fun View.setOnRecipeLongClickListener(
    id: Int,
    isTikTok: Boolean = false,
    onDeleted: (() -> Unit)? = null
) {
    this.setOnLongClickListener {
        RecipeToolsDialog.show(
            context = this.context,
            id = id,
            isTikTok = isTikTok,
            onDeleted = onDeleted
        )
        true
    }
}

/**
 * Extension function to attach long click listener for Web recipes to any View.
 */
fun View.setOnRecipeLongClickListener(
    webRecipe: WebRecipeModel,
    onDeleted: (() -> Unit)? = null
) {
    this.setOnLongClickListener {
        RecipeToolsDialog.show(
            context = this.context,
            webRecipe = webRecipe,
            onDeleted = onDeleted
        )
        true
    }
}

/**
 * Extension function to attach long click listener for any RecipeTarget to any View.
 */
fun View.setOnRecipeLongClickListener(
    target: RecipeToolsDialog.RecipeTarget,
    onDeleted: (() -> Unit)? = null
) {
    this.setOnLongClickListener {
        RecipeToolsDialog.show(
            context = this.context,
            target = target,
            onDeleted = onDeleted
        )
        true
    }
}
