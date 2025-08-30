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
    private val CHANNEL_ID = "JoyConServiceChannel"
    private val NOTIFICATION_ID = 101

    override fun onCreate() {
        super.onCreate()
        Log.d("JoyConMapperService", "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            Log.d("JoyConMapperService", "Starting...")

            if (!RootUtils.isRootAvailable()) {
                Log.e("JoyConMapperService", "Root required")
                stopSelf()
                return START_STICKY
            }

            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())

            virtualGamepad = VirtualGamepadManager()
            inputHandler = JoyConInputHandler { code, value ->
                // value 0/1 => button, otherwise axis
                if (value == 0 || value == 1) {
                    virtualGamepad.sendButton(code, value == 1)
                } else {
                    virtualGamepad.sendAxis(code, value)
                }
            }

            if (!virtualGamepad.createVirtualGamepad()) {
                Log.e("JoyConMapperService", "uinput creation failed")
                stopSelf()
                return START_STICKY
            }

            inputHandler.startListening()
            Log.d("JoyConMapperService", "Service running")
        } catch (t: Throwable) {
            Log.e("JoyConMapperService", "start error: ${t.message}", t)
            stopSelf()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "JoyCon Virtual Gamepad", NotificationManager.IMPORTANCE_LOW)
            channel.description = "JoyCon to virtual Xbox controller"
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JoyCon Virtual Gamepad")
            .setContentText("Running — virtual Xbox controller active")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            inputHandler.stopListening()
            virtualGamepad.destroy()
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
