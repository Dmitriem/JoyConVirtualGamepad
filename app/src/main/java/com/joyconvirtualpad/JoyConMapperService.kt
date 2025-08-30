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
        try {
            Log.d("JoyConMapperService", "Starting service...")
            
            if (!RootUtils.isRootAvailable()) {
                Log.e("JoyConMapperService", "Root access not available")
                stopSelf()
                return START_STICKY
            }

            // Создаем канал уведомлений
            createNotificationChannel()

            // Запускаем сервис в foreground с уведомлением
            startForeground(NOTIFICATION_ID, createNotification())
            Log.d("JoyConMapperService", "Foreground service started")

            virtualGamepad = VirtualGamepadManager()
            inputHandler = JoyConInputHandler { code, value ->
                // Отправляем события на виртуальный геймпад
                virtualGamepad.sendKeyEvent(code, value)
            }
            
            if (virtualGamepad.createVirtualGamepad()) {
                inputHandler.startListening()
                isRunning = true
                Log.d("JoyConMapperService", "Service started successfully")
            } else {
                Log.e("JoyConMapperService", "Failed to create virtual gamepad")
                stopSelf()
            }

            return START_STICKY
        } catch (e: Exception) {
            Log.e("JoyConMapperService", "Error in onStartCommand: ${e.message}", e)
            stopSelf()
            return START_NOT_STICKY
        }
    }

    private fun createNotificationChannel() {
        try {
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
                Log.d("JoyConMapperService", "Notification channel created")
            }
        } catch (e: Exception) {
            Log.e("JoyConMapperService", "Error creating notification channel: ${e.message}", e)
        }
    }

    private fun createNotification(): Notification {
        return try {
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("JoyCon Virtual Gamepad")
                .setContentText("Combining Joy-Cons into virtual controller")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        } catch (e: Exception) {
            Log.e("JoyConMapperService", "Error creating notification: ${e.message}", e)
            // Возвращаем простейшее уведомление в случае ошибки
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("JoyCon Service")
                .setContentText("Service is running")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()
        }
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
