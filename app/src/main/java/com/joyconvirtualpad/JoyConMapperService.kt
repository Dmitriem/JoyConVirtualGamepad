package com.joyconvirtualpad

import com.joyconvirtualpad.utils.RootUtils
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class JoyConMapperService : Service() {
    private lateinit var virtualGamepad: VirtualGamepadManager
    private lateinit var inputHandler: JoyConInputHandler
    private var isRunning = false
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!RootUtils.isRootAvailable()) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        virtualGamepad = VirtualGamepadManager()
        inputHandler = JoyConInputHandler { code, value, deviceId ->
            // Отправка событий на виртуальный геймпад
            virtualGamepad.sendKeyEvent(code, value)
        }
        
        // Создание виртуального геймпада
        if (virtualGamepad.createVirtualGamepad()) {
            inputHandler.startListening()
            isRunning = true
            Log.d("JoyConMapperService", "Service started successfully")
        } else {
            Log.e("JoyConMapperService", "Failed to create virtual gamepad")
            stopSelf()
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        inputHandler.stopListening()
        virtualGamepad.destroy()
        isRunning = false
        Log.d("JoyConMapperService", "Service destroyed")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
