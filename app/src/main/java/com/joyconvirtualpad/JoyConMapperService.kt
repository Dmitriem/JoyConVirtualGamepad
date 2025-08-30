package com.joyconvirtualpad

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
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

    override fun onCreate() {
        super.onCreate()
        Log.d("JoyConMapperService", "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            Log.d("JoyConMapperService", "Starting service...")

            if (!RootUtils.isRootAvailable()) {
                Log.e("JoyConMapperService", "Root access not available")
                stopSelf()
                return START_STICKY
            }

            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())
            Log.d("JoyConMapperService", "Foreground service started")

            virtualGamepad = VirtualGamepadManager()
            inputHandler = JoyConInputHandler { code, value ->
                // Если value == 0/1 — считаем кнопкой, иначе — осью
                if (value == 0 || value == 1) {
                    virtualGamepad.sendButton(code, value == 1)
                } else {
                    virtualGamepad.sendAxis(code, value)
                }
            }

            if (virtualGamepad.createVirtualGamepad()) {
                inputHandler.startListening()
                isRunning = true
                Log.d("JoyConMapperService", "Service started successfully")
            } else {
                Log.e("JoyConMapperService", "Failed to create virtual gamepad")
                stopSelf()
            }

        } catch (e: Exception) {
            Log.e("JoyConMapperService", "Error in onStartCommand: ${e.message}", e)
            stopSelf()
        }

        // Важно: возвращаем START_STICKY чтобы сервис перезапускался если система убьёт процесс
        return START_STICKY
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = getSystemService(NotificationManager::class.java)
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "JoyCon Virtual Gamepad Service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Service for combining Joy-Cons into virtual gamepad" }
                manager.createNotificationChannel(channel)
                Log.d("JoyConMapperService", "Notification channel created")
            }
        } catch (e: Exception) {
            Log.e("JoyConMapperService", "Error creating channel: ${e.message}", e)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JoyCon Virtual Gamepad")
            .setContentText("Combining Joy-Cons into virtual controller")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            inputHandler.stopListening()
            virtualGamepad.destroy()
            isRunning = false
            Log.d("JoyConMapperService", "Service destroyed")
        } catch (e: Exception) {
            Log.e("JoyConMapperService", "Error in onDestroy: ${e.message}", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
