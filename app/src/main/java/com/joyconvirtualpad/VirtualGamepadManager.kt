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
                Log.d("VirtualGamepadManager", "Virtual gamepad created successfully (fd=$uinputFd)")
                true
            } else {
                Log.e("VirtualGamepadManager", "Failed to create virtual gamepad (fd=$uinputFd)")
                false
            }
        } catch (e: Exception) {
            Log.e("VirtualGamepadManager", "Error creating virtual gamepad: ${e.message}", e)
            false
        }
    }

    fun sendButton(code: Int, pressed: Boolean) {
        if (uinputFd <= 0) {
            Log.e("VirtualGamepadManager", "Cannot send button - virtual gamepad not created")
            return
        }
        try {
            NativeUinputManager.sendKeyEvent(uinputFd, code, if (pressed) 1 else 0)
            NativeUinputManager.sync(uinputFd)
            Log.d("VirtualGamepadManager", "Sent button: code=$code pressed=$pressed")
        } catch (e: Exception) {
            Log.e("VirtualGamepadManager", "Error sending button: ${e.message}", e)
        }
    }

    fun sendAxis(code: Int, value: Int) {
        if (uinputFd <= 0) {
            Log.e("VirtualGamepadManager", "Cannot send axis - virtual gamepad not created")
            return
        }
        try {
            val clamped = value.coerceIn(-32768, 32767)
            NativeUinputManager.sendAbsEvent(uinputFd, code, clamped)
            NativeUinputManager.sync(uinputFd)
            Log.d("VirtualGamepadManager", "Sent axis: code=$code value=$clamped")
        } catch (e: Exception) {
            Log.e("VirtualGamepadManager", "Error sending axis: ${e.message}", e)
        }
    }

    fun destroy() {
        if (uinputFd > 0) {
            try {
                NativeUinputManager.destroyUinputDevice(uinputFd)
            } catch (e: Exception) {
                Log.e("VirtualGamepadManager", "Error destroying uinput: ${e.message}", e)
            }
            uinputFd = -1
            Log.d("VirtualGamepadManager", "Virtual gamepad destroyed")
        }
    }
}
