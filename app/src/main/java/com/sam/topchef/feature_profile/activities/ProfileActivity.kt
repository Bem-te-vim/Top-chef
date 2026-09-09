package com.sam.topchef.feature_profile.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayoutMediator
import com.sam.topchef.R
import com.sam.topchef.core.data.local.app.App
import com.sam.topchef.core.data.model.User
import com.sam.topchef.core.utils.LoadImages
import com.sam.topchef.core.utils.Utils.clickAnimation
import com.sam.topchef.databinding.ActivityProfileBinding
import com.sam.topchef.feature_feed_main.ui.activity.MainActivity
import com.sam.topchef.feature_fullscreen_image.FullscreenImageActivity
import com.sam.topchef.feature_profile.adaper.ProfilePageAdapter
import com.sam.topchef.feature_settings.view.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for displaying and managing the user's profile.
 * Shows user-created recipes, favorites, and profile settings.
 */
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    private var currentImageUri: String? = null
    private var currentUserName: String? = null

    var imageUriCallback: ((uri: String?) -> Unit)? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->


        uri?.let {
            imageUriCallback?.invoke(uri.toString())

            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

    }


    /**
     * Initializes the profile view, loads user data, recipe counts, and sets up the tabbed layout.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        setupListeners()
        loadProfileData()
        setupViewPager()
    }

    private fun setupListeners() {
        val include = binding.includeHeader
        include.btnBack.setOnClickListener { finish() }

        include.btnSettings.setOnClickListener {
            it.clickAnimation()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        include.imageProfile.setOnClickListener {
            it.clickAnimation()
            val i = Intent(this, FullscreenImageActivity::class.java)
            i.putExtra("imageUri", currentImageUri)

            val options = ActivityOptions
                .makeSceneTransitionAnimation(
                    (this),
                    it,
                    "image_transition"
                )
            startActivity(i, options.toBundle())
        }

        include.editProfileBtn.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun showEditProfileDialog() {
        val include = binding.includeHeader
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_edit_profile, null)
        dialog.setContentView(view)
        dialog.show()

        val btnSaveFromDialog: Button = view.findViewById(R.id.btn_save)
        val editNameFromDialog: EditText = view.findViewById(R.id.edit_name)
        val imageProfileFromDialog: ShapeableImageView = view.findViewById(R.id.image_profile)

        editNameFromDialog.setText(currentUserName ?: "Ainda sem nome")
        LoadImages().loadImagesWithBlur(currentImageUri, imageProfileFromDialog)

        imageProfileFromDialog.setOnClickListener {
            pickImage.launch("image/*")

            imageUriCallback = { uri ->
                currentImageUri = uri
                LoadImages().apply {
                    loadImagesWithBlur(uri, imageProfileFromDialog)
                }
            }
        }

        btnSaveFromDialog.setOnClickListener {
            val userName = editNameFromDialog.text.toString()
            currentUserName = userName
            include.profileUserName.text = userName

            val user = User(
                name = editNameFromDialog.text.toString(),
                imageUri = currentImageUri
            )

            //Save the image and the username on DataBase
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    (application as App).userDao.saveUser(user)
                }
            }

            LoadImages().loadImagesWithBlur(currentImageUri, include.imageProfile)
            include.profileUserName.text = currentUserName ?: "Ainda sem nome"

            val resultIntent = Intent()
            resultIntent.putExtra(MainActivity.EXTRA_RELOAD, true)
            setResult(RESULT_OK, resultIntent)

            dialog.dismiss()
        }
    }

    private fun loadProfileData() {
        val include = binding.includeHeader
        lifecycleScope.launch {
            val app = application as App
            val recipesData = withContext(Dispatchers.IO) {
                val recipes = app.recipeDao.getAllRecipes()
                val tiktokRecipes = app.db.tiktokDao().getAll()

                val totalCount = recipes.size + tiktokRecipes.size
                val totalFavorites =
                    recipes.count { it.isFavorite } + tiktokRecipes.count { it.isFavorite }

                Pair(totalCount, totalFavorites)
            }

            include.recipesCount.text = recipesData.first.toString()
            include.favoritesCount.text = recipesData.second.toString()


            val user = withContext(Dispatchers.IO) {
                app.userDao.getUser()
            }
            currentImageUri = user?.imageUri
            currentUserName = user?.name
            include.profileUserName.text =
                if (user?.name.isNullOrEmpty()) "Olá." else "Olá, ${user.name}"
            LoadImages().loadImagesWithBlur(user?.imageUri, include.imageProfile)
        }
    }

    private fun setupViewPager() {
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = ProfilePageAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(this, R.drawable.grid_on_24dp)
                1 -> ContextCompat.getDrawable(this, R.drawable.favorite_24dp)
                2 -> ContextCompat.getDrawable(this, R.drawable.web_stories_24dp)
                3 -> ContextCompat.getDrawable(this, R.drawable.play_circle_24dp)
                else -> throw IllegalStateException()
            }
        }.attach()
    }


    /**
     * Overrides finish to provide a custom transition animation.
     */
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


}