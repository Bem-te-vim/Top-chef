package com.sam.topchef.feature_profile.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.sam.topchef.R
import com.sam.topchef.core.data.local.appDataBase.AppDataBase
import com.sam.topchef.databinding.FragmentVideoRecipeBinding
import com.sam.topchef.feature_profile.adaper.VideoThumbnailAdapter
import com.sam.topchef.feature_profile.model.VideoThumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoRecipeFragment : Fragment() {
    private var _binding: FragmentVideoRecipeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = FragmentVideoRecipeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadVideos()
    }

    private fun setupRecyclerView() {
        val adapter = VideoThumbnailAdapter()
        binding.videoRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.videoRecyclerView.adapter = adapter
    }

    private fun loadVideos() {
        val db = AppDataBase.getDataBase(requireContext())
        lifecycleScope.launch {
            val tiktokRecipes = withContext(Dispatchers.IO) {
                db.tiktokDao().getAll()
            }

            val videoThumbnails = tiktokRecipes.map {
                VideoThumbnail(
                    videoId = it.id,
                    thumbnailPath = it.thumbnail,
                    title = it.name,
                    description = it.description
                )
            }

            (binding.videoRecyclerView.adapter as? VideoThumbnailAdapter)?.submitList(videoThumbnails)
        }
    }

    override fun onResume() {
        super.onResume()
        loadVideos()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}