package com.joyconvirtualpad

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class JoyConMapperService : Service() {
    private var isRunning = false
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("JoyConMapperService", "Service started")
        isRunning = true
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("JoyConMapperService", "Service destroyed")
        isRunning = false
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
