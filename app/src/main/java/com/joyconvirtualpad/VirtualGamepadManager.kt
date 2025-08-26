package com.joyconvirtualpad

import android.util.Log
import com.joyconvirtualpad.utils.NativeUinputManager

class VirtualGamepadManager {
    private var uinputFd: Int = -1
    
    fun createVirtualGamepad(): Boolean {
        return try {
            uinputFd = NativeUinputManager.createUinputDevice()
            uinputFd > 0
        } catch (e: Exception) {
            Log.e("VirtualGamepadManager", "Error creating virtual gamepad", e)
            false
        }
    }
    
    fun sendKeyEvent(code: Int, value: Int) {
        if (uinputFd <= 0) return
        NativeUinputManager.sendKeyEvent(uinputFd, code, value)
    }
    
    fun destroy() {
        if (uinputFd > 0) {
            NativeUinputManager.destroyUinputDevice(uinputFd)
            uinputFd = -1
        }
    }
}
