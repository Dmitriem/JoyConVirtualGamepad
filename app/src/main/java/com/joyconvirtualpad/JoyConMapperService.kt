package com.joyconvirtualpad

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.joyconvirtualpad.utils.RootUtils

class JoyConMapperService : Service() {
    private lateinit var virtualGamepad: VirtualGamepadManager
    private lateinit var inputHandler: JoyConInputHandler
    private var isRunning = false
    private val CHANNEL_ID = "JoyConServiceChannel"
    private val NOTIFICATION_ID = 101

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!RootUtils.isRootAvailable()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Создаем канал уведомлений (для Android 8.0+)
        createNotificationChannel()

        // Запускаем сервис в foreground с уведомлением
        startForeground(NOTIFICATION_ID, createNotification())

        virtualGamepad = VirtualGamepadManager()
        
        if (virtualGamepad.createVirtualGamepad()) {
            isRunning = true
            Log.d("JoyConMapperService", "Service started successfully in foreground")
        } else {
            Log.e("JoyConMapperService", "Failed to create virtual gamepad")
            stopSelf()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JoyCon Virtual Gamepad Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service for combining Joy-Cons into virtual gamepad"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JoyCon Virtual Gamepad")
            .setContentText("Combining Joy-Cons into virtual controller")
            .setSmallIcon(android.R.drawable.ic_menu_manage) // Временная иконка
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        virtualGamepad.destroy()
        isRunning = false
        Log.d("JoyConMapperService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
