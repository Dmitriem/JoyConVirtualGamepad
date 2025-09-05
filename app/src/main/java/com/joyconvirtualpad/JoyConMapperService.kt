package com.joyconvirtualpad

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.joyconvirtualpad.utils.RootUtils
import java.io.File

class JoyConMapperService : Service() {
    private lateinit var virtualGamepad: VirtualGamepadManager
    private lateinit var inputHandler: JoyConInputHandler
    private val CHANNEL_ID = "JoyConServiceChannel"
    private val NOTIF_ID = 101
    private val TAG = "JoyConMapperService"

    private fun writeStatus(text: String) {
        try { File("/sdcard/joycon_status.txt").appendText("${System.currentTimeMillis()}: $text\n") } catch (_: Exception) {}
    }

    override fun onCreate() {
        super.onCreate()
        writeStatus("Service onCreate")
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        writeStatus("onStartCommand")
        Log.d(TAG, "Starting service")

        // Обработка команды остановки из уведомления
        if (intent?.action == "ACTION_STOP_SERVICE") {
            writeStatus("Received ACTION_STOP_SERVICE -> stopping")
            stopSelf()
            return START_STICKY
        }

        // Проверим root (вдвойне)
        if (!RootUtils.isRootAvailable()) {
            writeStatus("Root not available - abort")
            stopSelf()
            return START_STICKY
        }
        // Попробуем выполнить простую команду - убедимся, что su разрешён
        val ok = RootUtils.executeCommand("id")
        writeStatus("Root executeCommand(id) returned: $ok")
        if (!ok) {
            writeStatus("Root auth failed - please grant su to app")
            // можно уведомить пользователя, но продолжаем — createVirtualGamepad проверит
        }

        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())

        virtualGamepad = VirtualGamepadManager()
        if (!virtualGamepad.createVirtualGamepad()) {
            writeStatus("uinput creation failed - stopping")
            stopSelf()
            return START_STICKY
        }

        inputHandler = JoyConInputHandler { code, value ->
            if (value == 0 || value == 1) virtualGamepad.sendButton(code, value == 1)
            else virtualGamepad.sendAxis(code, value)
            // записываем пару событий для проверки
            writeStatus("evt code=$code val=$value")
        }
        inputHandler.startListening()
        writeStatus("Service started successfully")
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Когда приложение свайпнут, перезапускаем сервис через AlarmManager через 1 сек
        try {
            val restartIntent = Intent(applicationContext, JoyConMapperService::class.java)
            val pending = PendingIntent.getService(
                applicationContext,
                1,
                restartIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT else PendingIntent.FLAG_ONE_SHOT
            )
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, pending)
            writeStatus("onTaskRemoved: scheduled restart")
        } catch (e: Exception) {
            writeStatus("onTaskRemoved error: ${e.message}")
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            inputHandler.stopListening()
            virtualGamepad.destroy()
            writeStatus("Service destroyed")
        } catch (e: Exception) {
            writeStatus("onDestroy exception: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "JoyCon Virtual Gamepad", NotificationManager.IMPORTANCE_LOW)
            channel.description = "JoyCon -> virtual Xbox controller"
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        // создаём PendingIntent для остановки сервиса
        val stopIntent = Intent(this, JoyConMapperService::class.java).apply { action = "ACTION_STOP_SERVICE" }
        val stopPending = PendingIntent.getService(
            this,
            2,
            stopIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JoyCon Virtual Gamepad")
            .setContentText("Running — virtual Xbox controller active")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        return builder.build()
    }
}
