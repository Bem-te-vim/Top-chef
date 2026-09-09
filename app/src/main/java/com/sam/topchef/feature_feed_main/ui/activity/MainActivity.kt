package com.sam.topchef.feature_feed_main.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.Cart
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.core.utils.Utils.shareText
import com.sam.topchef.core.utils.Utils.toShareText
import com.sam.topchef.databinding.ActivityMainBinding
import com.sam.topchef.feature_add_recipe.ui.activity.AddRecipeActivity
import com.sam.topchef.feature_edit_recipe.EditRecipeActivity
import com.sam.topchef.feature_feed_main.adapter.CategoryHeaderAdapter
import com.sam.topchef.feature_feed_main.adapter.PopularHeaderAdapter
import com.sam.topchef.feature_feed_main.adapter.RecipePagingAdapter
import com.sam.topchef.feature_feed_main.adapter_interface.AdapterChanges
import com.sam.topchef.feature_feed_main.ui.viewmodel.MainViewModel
import com.sam.topchef.feature_feed_main.ui.viewmodel.MainViewModelFactory
import com.sam.topchef.feature_import_from_tiktok.view.TiktokImportActivity
import com.sam.topchef.feature_profile.activities.ProfileActivity
import com.sam.topchef.feature_recipe_detail.ui.activity.RecipeDetailActivity
import com.sam.topchef.feature_search.activities.SearchActivity
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity.Companion.ALL_CATEGORIES
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity.Companion.ALL_POPULAR_RECIPES
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity.Companion.CATEGORY_FILTER
import com.sam.topchef.feature_see_all.ui.activity.SeeAllActivity.Companion.EXTRA_CATEGORY_NAME
import com.sam.topchef.feature_shopping_list.activities.ShoppingListActivity
import com.sam.topchef.feature_shopping_list.data.model.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), AdapterChanges {
    private lateinit var result: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityMainBinding
    
    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory((application as App).db)
    }

    private lateinit var popularHeaderAdapter: PopularHeaderAdapter
    private lateinit var categoryHeaderAdapter: CategoryHeaderAdapter
    private lateinit var recipePagingAdapter: RecipePagingAdapter

    companion object {
        const val EXTRA_RECIPE_ID = "id"
        const val EXTRA_IS_FAVORITE = "isFavorite"
        const val EXTRA_RELOAD = "reload"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        setupActivityResultLauncher()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        
        checkIntent(intent)
    }

    private fun setupActivityResultLauncher() {
        result = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != RESULT_OK) return@registerForActivityResult
            val data = result.data ?: return@registerForActivityResult

            when {
                data.hasExtra(EXTRA_IS_FAVORITE) -> {
                    val id = data.getIntExtra(EXTRA_RECIPE_ID, -1)
                    if (id != -1) {
                        val isFavorite = data.getBooleanExtra(EXTRA_IS_FAVORITE, false)
                        notifyLike(id, isFavorite)
                    }
                }
                data.hasExtra(EXTRA_RELOAD) -> {
                    viewModel.loadHeaders()
                    recipePagingAdapter.refresh()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_RELOAD, false) == true) {
            viewModel.loadHeaders()
            recipePagingAdapter.refresh()
        }
    }

    private fun setupRecyclerView() {
        popularHeaderAdapter = PopularHeaderAdapter(this) {
            val i = Intent(this, SeeAllActivity::class.java)
            i.putExtra("show", ALL_POPULAR_RECIPES)
            startActivity(i)
        }

        categoryHeaderAdapter = CategoryHeaderAdapter({ category ->
            val i = Intent(this, SeeAllActivity::class.java).apply {
                putExtra("show", CATEGORY_FILTER)
                putExtra(EXTRA_CATEGORY_NAME, category)
            }
            startActivity(i)
        }, {
            val i = Intent(this, SeeAllActivity::class.java)
            i.putExtra("show", ALL_CATEGORIES)
            startActivity(i)
        })

        recipePagingAdapter = RecipePagingAdapter(this)

        val concatAdapter = ConcatAdapter(popularHeaderAdapter, categoryHeaderAdapter, recipePagingAdapter)
        binding.rvMainFeed.layoutManager = LinearLayoutManager(this)
        binding.rvMainFeed.adapter = concatAdapter

        recipePagingAdapter.addLoadStateListener { loadState ->
            val isEmpty = loadState.refresh is LoadState.NotLoading && recipePagingAdapter.itemCount == 0
            validateList(isEmpty)
        }
    }

    private fun setupListeners() {
        binding.imageProfile.setOnClickListener {
            val i = Intent(this, ProfileActivity::class.java)
            result.launch(i)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }

        binding.textInputSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.btnAddRecipe.setOnClickListener {
            val i = Intent(this, AddRecipeActivity::class.java)
            result.launch(i)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.recipePosts.collectLatest { pagingData ->
                recipePagingAdapter.submitData(pagingData)
            }
        }

        lifecycleScope.launch {
            viewModel.popularRecipes.collectLatest { items ->
                popularHeaderAdapter.setItems(items)
            }
        }

        lifecycleScope.launch {
            viewModel.categories.collectLatest { items ->
                categoryHeaderAdapter.setItems(items)
            }
        }
        
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                (application as App).db.userDao().getUser()
            }
            LoadImages().loadImagesWithBlur(user?.imageUri, binding.imageProfile)
            binding.HeloProfile.text =
                if (user?.name.isNullOrEmpty()) "Olá." else "Olá, ${user.name}"
        }
    }

    private fun startActivityEditor(id: Int) {
        val i = Intent(this, EditRecipeActivity::class.java)
        i.putExtra("id", id)
        result.launch(i)
    }

    private fun moveIngredientsToCart(id: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            val recipe = app.db.recipeDao().getRecipe(id)

            if (recipe != null) {
                val cartItems = recipe.ingredients.map { CartItem(itemName = it) }
                val newCart = Cart(
                    title = recipe.title,
                    cartImage = recipe.imageUriString.firstOrNull(),
                    cartItems = cartItems
                )
                val cartId = app.db.cartDao().insert(newCart).toInt()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ingredientes movidos para o carrinho!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, ShoppingListActivity::class.java)
                    intent.putExtra("id", cartId)
                    startActivity(intent)
                }
            }
        }
    }

    private fun deleteTikTokRecipe(id: Int) {
        AlertDialog.Builder(this)
            .setTitle("Deletar essa recaita?")
            .setNegativeButton("Cancelar") { p0, _ -> p0.dismiss() }
            .setPositiveButton("Deletar") { p0, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val app = application as App
                    val dao = app.db.tiktokDao()
                    dao.delete(id)
                    withContext(Dispatchers.Main) {
                        recipePagingAdapter.refresh()
                        popularHeaderAdapter.onDeleteNotify(id, true)
                        Toast.makeText(this@MainActivity, "Receita do Tiktok deletada", Toast.LENGTH_LONG).show()
                    }
                }
                p0.dismiss()
            }.show()
    }

    private fun showDeleteRecipeDialog(id: Int) {
        AlertDialog.Builder(this)
            .setTitle("Deletar essa recaita?")
            .setNegativeButton("Cancelar") { p0, _ -> p0.dismiss() }
            .setPositiveButton("Deletar") { p0, _ ->
                deleteRecipe(id)
                p0.dismiss()
            }
            .show()
    }

    private fun deleteRecipe(id: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            val dao = app.db.recipeDao()
            dao.delete(id)
            withContext(Dispatchers.Main) {
                viewModel.loadHeaders()
                recipePagingAdapter.refresh()
                popularHeaderAdapter.onDeleteNotify(id, false)
                Toast.makeText(this@MainActivity, "Receita deletada", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showRecipesNotFoundError(
        show: Boolean = false,
        title: String = "Oops...",
        message: String = "Parece que algo deu errado",
        buttonText: String = "Volar"
    ) {
        val include = binding.includeRecipeListEmpty
        include.errorLayoutRoot.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvMainFeed.visibility = if (!show) View.VISIBLE else View.GONE

        include.errorTitle.text = title
        include.errorMessage.text = message
        include.btnGoToHome.text = buttonText

        include.btnGoToHome.setOnClickListener {
            val i = Intent(this, AddRecipeActivity::class.java)
            result.launch(i)
        }
    }

    private fun saveLikeUpdate(id: Int, isFavorite: Boolean, isTikTok: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            if (isTikTok) {
                val tiktokDao = app.db.tiktokDao()
                val recipe = tiktokDao.getById(id)
                if (recipe != null) {
                    tiktokDao.update(recipe.copy(isFavorite = isFavorite))
                }
            } else {
                val recipeDao = app.db.recipeDao()
                val recipe = recipeDao.getRecipe(id)
                if (recipe != null) {
                    recipeDao.update(recipe.copy(isFavorite = isFavorite))
                }
            }
            withContext(Dispatchers.Main) {
                popularHeaderAdapter.onLikeNotify(id, isFavorite, isTikTok)
            }
        }
    }

    private fun notifyLike(id: Int, isFavorite: Boolean, isTikTok: Boolean = false) {
        saveLikeUpdate(id, isFavorite, isTikTok)
    }

    override fun onRecipeLiked(id: Int, isFavorite: Boolean, isTikTok: Boolean) {
        notifyLike(id, isFavorite, isTikTok)
    }

    override fun onRecipeClicked(id: Int) {
        val i = Intent(this, RecipeDetailActivity::class.java)
        i.putExtra("id", id)
        result.launch(i)
    }

    override fun onTikTokRecipeClicked(id: Int) {
        val i = Intent(this, TiktokImportActivity::class.java)
        i.putExtra("tiktokId", id)
        startActivity(i)
    }

    override fun onRecipeTools(id: Int, isTikTok: Boolean) {
        showBottomSheetsDialog(id, isTikTok)
    }

    @SuppressLint("InflateParams")
    private fun showBottomSheetsDialog(id: Int, isTikTok: Boolean = false) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_tools_bottom_sheet, null)
        dialog.setContentView(view)

        val delete: LinearLayout = view.findViewById(R.id.tools_delete)
        val edit: LinearLayout = view.findViewById(R.id.tools_edit)
        val share: LinearLayout = view.findViewById(R.id.tools_share)
        val moveToCart: LinearLayout = view.findViewById(R.id.tools_move_to_cart)

        if (isTikTok) {
            edit.visibility = View.GONE
            moveToCart.visibility = View.GONE
        }

        delete.setOnClickListener {
            if (isTikTok) deleteTikTokRecipe(id) else showDeleteRecipeDialog(id)
            dialog.dismiss()
        }
        edit.setOnClickListener {
            startActivityEditor(id)
            dialog.dismiss()
        }
        share.setOnClickListener {
            shareRecipe(id, isTikTok)
            dialog.dismiss()
        }
        moveToCart.setOnClickListener {
            moveIngredientsToCart(id)
            dialog.dismiss()
        }

        dialog.show()
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
    }

    private fun shareRecipe(id: Int, isTikTok: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            val app = application as App
            val text = if (isTikTok) {
                app.db.tiktokDao().getById(id)?.toShareText()
            } else {
                app.db.recipeDao().getRecipe(id)?.toShareText()
            }
            text?.let {
                withContext(Dispatchers.Main) {
                    shareText(this@MainActivity, it)
                }
            }
        }
    }

    private fun validateList(isEmpty: Boolean) {
        if (isEmpty) {
            showRecipesNotFoundError(
                show = true,
                title = "Olá!",
                message = "Parece que você ainda não tem nem uma receita salva.",
                buttonText = "Criar Receita"
            )
        } else {
            showRecipesNotFoundError(false)
        }
    }
}
