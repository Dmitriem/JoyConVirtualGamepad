package com.joyconvirtualpad

import android.util.Log
import com.joyconvirtualpad.utils.NativeUinputManager

class VirtualGamepadManager {
    private var uinputFd: Int = -1
    
    fun createVirtualGamepad(): Boolean {
        return try {
            uinputFd = NativeUinputManager.createUinputDevice()
            if (uinputFd > 0) {
                Log.d("VirtualGamepadManager", "Virtual gamepad created successfully")
                true
            } else {
                Log.e("VirtualGamepadManager", "Failed to create virtual gamepad")
                false
            }
        } catch (e: Exception) {
            Log.e("VirtualGamepadManager", "Error creating virtual gamepad", e)
            false
        }
    }
    
    fun sendKeyEvent(code: Int, value: Int) {
        if (uinputFd <= 0) return
        
        try {
            NativeUinputManager.sendKeyEvent(uinputFd, code, value)
            Log.d("VirtualGamepadManager", "Sent key event: code=$code, value=$value")
        } catch (e: Exception) {
            Log.e("VirtualGamepadManager", "Error sending key event", e)
        }
    }
    
    fun destroy() {
        if (uinputFd > 0) {
            NativeUinputManager.destroyUinputDevice(uinputFd)
            uinputFd = -1
            Log.d("VirtualGamepadManager", "Virtual gamepad destroyed")
        }
    }
}
