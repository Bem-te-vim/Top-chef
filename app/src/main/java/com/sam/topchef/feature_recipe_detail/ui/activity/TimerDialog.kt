package com.sam.topchef.feature_recipe_detail.ui.activity

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sam.topchef.R
import com.sam.topchef.databinding.DialogTimerBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class TimerDialog : BottomSheetDialogFragment() {

    private var _binding: DialogTimerBinding? = null
    private val binding get() = _binding!!

    private var initialMinutes: Int = 0
    private var recipeId: Int = -1

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startTimerService()
        } else {
            Toast.makeText(requireContext(), "Permissão de notificação necessária para o timer", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val TAG = "TimerDialog"
        private const val ARG_MINUTES = "arg_minutes"
        private const val ARG_ID = "arg_id"

        fun newInstance(minutes: Int, id: Int): TimerDialog {
            val dialog = TimerDialog()
            val args = Bundle()
            args.putInt(ARG_MINUTES, minutes)
            args.putInt(ARG_ID, id)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialMinutes = arguments?.getInt(ARG_MINUTES) ?: 0
        recipeId = arguments?.getInt(ARG_ID) ?: -1
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?.setBackgroundResource(android.R.color.transparent)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPickers()
        setupPresets()
        setupListeners()
        observeTimer()
    }

    private fun setupPickers() {
        binding.pickerHours.apply {
            minValue = 0
            maxValue = 99
            value = initialMinutes / 60
        }
        binding.pickerMinutes.apply {
            minValue = 0
            maxValue = 59
            value = initialMinutes % 60
        }
        binding.pickerSeconds.apply {
            minValue = 0
            maxValue = 59
        }
    }

    private fun setupPresets() {
        binding.btnPreset10.setOnClickListener { setTime(0, 10, 0) }
        binding.btnPreset15.setOnClickListener { setTime(0, 15, 0) }
        binding.btnPreset30.setOnClickListener { setTime(0, 30, 0) }
    }

    private fun setTime(h: Int, m: Int, s: Int) {
        binding.pickerHours.value = h
        binding.pickerMinutes.value = m
        binding.pickerSeconds.value = s
    }

    private fun setupListeners() {
        binding.btnStartStop.setOnClickListener {
            if (TimerService.isTimerRunning.value) {
                stopTimer()
            } else {
                startTimer()
            }
        }
    }

    private fun startTimer() {
        val h = binding.pickerHours.value
        val m = binding.pickerMinutes.value
        val s = binding.pickerSeconds.value
        val totalSeconds = (h * 3600 + m * 60 + s).toLong()

        if (totalSeconds <= 0) {
            Toast.makeText(requireContext(), "Defina um tempo maior que zero", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                startTimerService(totalSeconds)
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startTimerService(totalSeconds)
        }
    }

    private fun startTimerService(seconds: Long = 0) {
        val finalSeconds = if (seconds > 0) seconds else {
            val h = binding.pickerHours.value
            val m = binding.pickerMinutes.value
            val s = binding.pickerSeconds.value
            (h * 3600 + m * 60 + s).toLong()
        }

        if (finalSeconds > 0) {
            val intent = Intent(requireContext(), TimerService::class.java).apply {
                action = TimerService.ACTION_START
                putExtra(TimerService.EXTRA_TIME, finalSeconds)
                putExtra(TimerService.EXTRA_RECIPE_ID, recipeId)
            }
            ContextCompat.startForegroundService(requireContext(), intent)
        }
    }

    private fun stopTimer() {
        val intent = Intent(requireContext(), TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        requireContext().startService(intent)
    }

    private fun observeTimer() {
        viewLifecycleOwner.lifecycleScope.launch {
            TimerService.isTimerRunning.collectLatest { isRunning ->
                updateUiState(isRunning)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            TimerService.timeLeftFlow.collectLatest { seconds ->
                binding.txtTimerDisplay.text = formatTime(seconds)
            }
        }
    }

    private fun updateUiState(isRunning: Boolean) {
        if (isRunning) {
            binding.containerPickers.visibility = View.GONE
            binding.containerPresets.visibility = View.GONE
            binding.txtTimerDisplay.visibility = View.VISIBLE
            binding.btnStartStop.setText(R.string.stop)
            binding.btnStartStop.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.myGray)
            )
        } else {
            binding.containerPickers.visibility = View.VISIBLE
            binding.containerPresets.visibility = View.VISIBLE
            binding.txtTimerDisplay.visibility = View.GONE
            binding.btnStartStop.setText(R.string.start)
            binding.btnStartStop.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.default_color_app)
            )
        }
    }

    private fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format(Locale.getDefault(), "%02d : %02d : %02d", h, m, s)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
