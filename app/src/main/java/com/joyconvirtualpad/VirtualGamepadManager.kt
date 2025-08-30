package com.joyconvirtualpad

import android.util.Log
import com.joyconvirtualpad.utils.NativeUinputManager

class VirtualGamepadManager {
    private var uinputFd: Int = -1

    fun createVirtualGamepad(): Boolean {
        return try {
            uinputFd = NativeUinputManager.createUinputDevice()
            if (uinputFd > 0) {
                Log.d("VirtualGamepadManager", "Virtual gamepad created (fd=$uinputFd)")
                true
            } else {
                Log.e("VirtualGamepadManager", "Failed to create virtual gamepad (fd=$uinputFd)")
                false
            }
        } catch (t: Throwable) {
            Log.e("VirtualGamepadManager", "Error createVirtualGamepad: ${t.message}", t)
            false
        }
    }

    fun sendButton(code: Int, pressed: Boolean) {
        if (uinputFd <= 0) return
        try {
            NativeUinputManager.sendKeyEvent(uinputFd, code, if (pressed) 1 else 0)
            NativeUinputManager.sync(uinputFd)
        } catch (t: Throwable) {
            Log.e("VirtualGamepadManager", "sendButton error: ${t.message}", t)
        }
    }

    fun sendAxis(code: Int, value: Int) {
        if (uinputFd <= 0) return
        try {
            val clamped = value.coerceIn(-32768, 32767)
            NativeUinputManager.sendAbsEvent(uinputFd, code, clamped)
            NativeUinputManager.sync(uinputFd)
        } catch (t: Throwable) {
            Log.e("VirtualGamepadManager", "sendAxis error: ${t.message}", t)
        }
    }

    fun destroy() {
        if (uinputFd > 0) {
            try {
                NativeUinputManager.destroyUinputDevice(uinputFd)
            } catch (t: Throwable) {
                // ignore
            }
            uinputFd = -1
        }
    }
}
