package com.joyconvirtualpad

import android.util.Log
import com.joyconvirtualpad.utils.NativeUinputManager
import com.joyconvirtualpad.utils.RootUtils
import java.io.File

class VirtualGamepadManager {
    private var uinputFd: Int = -1
    private val TAG = "VirtualGamepadManager"

    private fun statusFile(): File =
        File(MyApp.appContext.getExternalFilesDir(null), "joycon_status.txt")

    private fun writeStatus(text: String) {
        try { statusFile().appendText("${System.currentTimeMillis()}: $text\n") } catch (_: Exception) {}
    }

    fun createVirtualGamepad(): Boolean {
        try {
            writeStatus("createVirtualGamepad: start")
            RootUtils.executeCommand("modprobe uinput || true")
            RootUtils.executeCommand("chmod 666 /dev/uinput || true")

            uinputFd = NativeUinputManager.createUinputDevice()
            if (uinputFd > 0) {
                writeStatus("uinput created fd=$uinputFd")
                return true
            }
            writeStatus("first create failed, retrying")
            RootUtils.executeCommand("chmod 666 /dev/uinput || true")
            uinputFd = NativeUinputManager.createUinputDevice()
            val ok = uinputFd > 0
            writeStatus(if (ok) "uinput created on retry fd=$uinputFd" else "FAILED to create uinput")
            return ok
        } catch (e: Exception) {
            writeStatus("Exception createVirtualGamepad: ${e.message}")
            Log.e(TAG, "create err", e)
            return false
        }
    }

    fun sendButton(code: Int, pressed: Boolean) {
        if (uinputFd <= 0) { writeStatus("sendButton ignored - no uinput"); return }
        NativeUinputManager.sendKeyEvent(uinputFd, code, if (pressed) 1 else 0)
        NativeUinputManager.sync(uinputFd)
    }

    fun sendAxis(code: Int, value: Int) {
        if (uinputFd <= 0) { writeStatus("sendAxis ignored - no uinput"); return }
        val clamped = value.coerceIn(-32767, 32767)
        NativeUinputManager.sendAbsEvent(uinputFd, code, clamped)
        NativeUinputManager.sync(uinputFd)
    }

    fun destroy() {
        if (uinputFd > 0) {
            NativeUinputManager.destroyUinputDevice(uinputFd)
            uinputFd = -1
            writeStatus("uinput destroyed")
        }
    }
}
