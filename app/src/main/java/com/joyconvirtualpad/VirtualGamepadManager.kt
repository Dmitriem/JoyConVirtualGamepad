package com.joyconvirtualpad

import android.util.Log
import com.joyconvirtualpad.utils.NativeUinputManager

class VirtualGamepadManager {
    private var uinputFd: Int = -1
    
    fun createVirtualGamepad(): Boolean {
        return try {
            Log.d("VirtualGamepadManager", "Creating virtual gamepad...")
            uinputFd = NativeUinputManager.createUinputDevice()
            if (uinputFd > 0) {
                Log.d("VirtualGamepadManager", "Virtual gamepad created successfully")
                true
            } else {
                Log.e("VirtualGamepadManager", "Failed to create virtual gamepad")
                false
            }
        } catch (e: Exception) {
            Log.e("VirtualGamepadManager", "Error creating virtual gamepad: ${e.message}", e)
            false
        }
    }
    
    fun sendKeyEvent(code: Int, value: Int) {
        if (uinputFd <= 0) {
            Log.e("VirtualGamepadManager", "Cannot send key event - virtual gamepad not created")
            return
        }
        
        try {
            Log.d("VirtualGamepadManager", "Sending key event: code=$code, value=$value")
            NativeUinputManager.sendKeyEvent(uinputFd, code, value)
        } catch (e: Exception) {
            Log.e("VirtualGamepadManager", "Error sending key event: ${e.message}", e)
        }
    }
    
    fun destroy() {
        if (uinputFd > 0) {
            try {
                NativeUinputManager.destroyUinputDevice(uinputFd)
                uinputFd = -1
                Log.d("VirtualGamepadManager", "Virtual gamepad destroyed")
            } catch (e: Exception) {
                Log.e("VirtualGamepadManager", "Error destroying virtual gamepad: ${e.message}", e)
            }
        }
    }
}
