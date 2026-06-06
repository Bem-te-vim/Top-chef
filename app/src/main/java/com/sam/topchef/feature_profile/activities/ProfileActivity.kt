package com.sam.topchef.feature_profile.activities

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
import com.sam.topchef.databinding.ActivityProfileBinding
import com.sam.topchef.feature_profile.adaper.ProfilePageAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val include = binding.includeHeader
        include.btnBack.setOnClickListener { finish() }

        include.editProfileBtn.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.layout_edit_profile, null)
            dialog.setContentView(view)
            dialog.show()

            val btnSaveFromDialog: Button = view.findViewById(R.id.btn_save)
            val editNameFromDialog: EditText = view.findViewById(R.id.edit_name)
            val imageProfileFromDialog: ShapeableImageView = view.findViewById(R.id.image_profile)

            var profileImage: String? = null

            imageProfileFromDialog.setOnClickListener {
                pickImage.launch("image/*")

                imageUriCallback = { uri ->
                    profileImage = uri

                    LoadImages().apply {
                        loadImagesWithBlur(uri, imageProfileFromDialog)
                        loadImagesWithBlur(uri, include.imageProfile)
                    }
                }
            }

            btnSaveFromDialog.setOnClickListener {
                include.profileUserName.text = editNameFromDialog.text.toString()
                dialog.dismiss()
                val user = User(
                    name = editNameFromDialog.text.toString(),
                    imageUri = profileImage
                )


                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        (application as App).userDao.saveUser(user)
                    }
                }
            }


        }


        lifecycleScope.launch {
            val recipesCount = withContext(Dispatchers.IO) {
                (application as App).recipeDao.getAllRecipes()
            }

            include.recipesCount.text = recipesCount.size.toString()
            include.favoritesCount.text = recipesCount.filter { it.isFavorite }.size.toString()


            val user = withContext(Dispatchers.IO) {
                (application as App).userDao.getUser()
            }
            include.profileUserName.text = if( user?.name.isNullOrEmpty()) "Olá." else "Olá, ${user.name}"
            LoadImages().loadImagesWithBlur(user?.imageUri, include.imageProfile)



        }

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = ProfilePageAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(this, R.drawable.grid_on_24dp)
                1 -> ContextCompat.getDrawable(this, R.drawable.round_favorite_border_24)
                2 -> ContextCompat.getDrawable(this, R.drawable.web_stories_24dp)
                else -> throw IllegalStateException()
            }
        }.attach()

    }


    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


}