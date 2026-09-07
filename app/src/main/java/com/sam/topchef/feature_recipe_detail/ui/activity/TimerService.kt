package com.sam.topchef.feature_recipe_detail.ui.activity

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.sam.topchef.R
import kotlinx.coroutines.flow.MutableStateFlow

class TimerService : Service() {

    private var countdownTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private val binder = TimerBinder()

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1
        
        val timeLeftFlow = MutableStateFlow<Long>(0L)
        val isTimerRunning = MutableStateFlow<Boolean>(false)
        var totalTime: Long = 0L
        var currentRecipeId: Int = -1

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_STOP_ALARM = "ACTION_STOP_ALARM"
        const val EXTRA_TIME = "EXTRA_TIME"
        const val EXTRA_RECIPE_ID = "EXTRA_RECIPE_ID"
    }

    override fun onBind(intent: Intent?): IBinder = binder

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            ACTION_START -> {
                val time = intent.getLongExtra(EXTRA_TIME, 0L)
                val recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, -1)
                startTimer(time, recipeId)
            }
            ACTION_STOP -> {
                stopTimer()
            }
            ACTION_STOP_ALARM -> {
                stopTimer()
            }
            else -> {
                // If the service is restarted by the system, ensure startForeground is called
                if (isTimerRunning.value) {
                    startForegroundServiceSafely(timeLeftFlow.value)
                } else {
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun startTimer(timeInSeconds: Long, recipeId: Int) {
        countdownTimer?.cancel()
        totalTime = timeInSeconds
        currentRecipeId = recipeId
        timeLeftFlow.value = timeInSeconds
        isTimerRunning.value = true

        startForegroundServiceSafely(timeInSeconds)

        countdownTimer = object : CountDownTimer(timeInSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                timeLeftFlow.value = seconds
                updateNotification(seconds)
            }

            override fun onFinish() {
                timeLeftFlow.value = 0
                isTimerRunning.value = false
                updateNotification(0, true)
                playFinishSoundAndVibrate()
            }
        }.start()
    }

    private fun playFinishSoundAndVibrate() {
        // Vibrate
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(2000)
        }

        // Sound
        try {
            val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            mediaPlayer = MediaPlayer.create(this, alert)
            mediaPlayer?.setOnCompletionListener {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            mediaPlayer?.start()
        } catch (_: Exception) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
        stopAlarm()
        isTimerRunning.value = false
        timeLeftFlow.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopAlarm() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        countdownTimer?.cancel()
        stopAlarm()
        super.onDestroy()
    }

    private fun startForegroundServiceSafely(seconds: Long) {
        val notification = createNotification(seconds)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recipe Timer",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(seconds: Long, isFinished: Boolean = false): Notification {
        val intent = Intent(this, RecipeDetailActivity::class.java).apply {
            putExtra("id", currentRecipeId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val timeStr = if (isFinished) "Concluído!" else formatTime(seconds)
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Temporizador de Cozimento")
            .setContentText(timeStr)
            .setSmallIcon(R.drawable.round_access_time_24)
            .setContentIntent(pendingIntent)
            .setOngoing(!isFinished)
            .setPriority(if (isFinished) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        if (isFinished) {
            val stopAlarmIntent = Intent(this, TimerService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            val stopAlarmPendingIntent = PendingIntent.getService(
                this, 1, stopAlarmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            builder.setDefaults(Notification.DEFAULT_ALL)
            builder.addAction(R.drawable.outline_close_small_24, "Parar Alarme", stopAlarmPendingIntent)
        }
        
        return builder.build()
    }

    private fun updateNotification(seconds: Long, isFinished: Boolean = false) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification(seconds, isFinished))
    }

    private fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}
