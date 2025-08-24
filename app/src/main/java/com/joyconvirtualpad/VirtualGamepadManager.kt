package com.joyconvirtualpad

import android.util.Log

class VirtualGamepadManager {
    private var isCreated = false
    
    fun createVirtualGamepad(): Boolean {
        Log.d("VirtualGamepadManager", "Creating virtual gamepad")
        isCreated = true
        return true
    }
    
    fun sendKeyEvent() {
        if (!isCreated) return
        Log.d("VirtualGamepadManager", "Sending key event")
    }
    
    fun sendMotionEvent() {
        if (!isCreated) return
        Log.d("VirtualGamepadManager", "Sending motion event")
    }
    
    fun destroy() {
        Log.d("VirtualGamepadManager", "Destroying virtual gamepad")
        isCreated = false
    }
}
