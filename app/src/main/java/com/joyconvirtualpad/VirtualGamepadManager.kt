package com.joyconvirtualpad

import android.util.Log
import com.joyconvirtualpad.utils.NativeUinputManager
import com.joyconvirtualpad.utils.RootUtils
import java.io.File

class VirtualGamepadManager {
    private var uinputFd: Int = -1
    private val TAG = "VirtualGamepadManager"

    private fun writeStatus(text: String) {
        try {
            File("/sdcard/joycon_status.txt").appendText("${System.currentTimeMillis()}: $text\n")
        } catch (_: Exception) {}
    }

    fun createVirtualGamepad(): Boolean {
        try {
            writeStatus("createVirtualGamepad: starting")
            Log.d(TAG, "Attempting modprobe uinput")
            // Попробовать загрузить модуль uinput (если это применимо)
            try {
                RootUtils.executeCommand("modprobe uinput")
            } catch (_: Exception) {}

            // Поставим права на /dev/uinput на случай, если это помогает
            RootUtils.executeCommand("chmod 666 /dev/uinput")

            // Первый вызов нативной функции
            uinputFd = NativeUinputManager.createUinputDevice()
            if (uinputFd > 0) {
                Log.d(TAG, "Virtual gamepad created (fd=$uinputFd)")
                writeStatus("uinput created fd=$uinputFd")
                return true
            }

            // Если не получилось, попробуем ещё раз после небольшой операции
            Log.w(TAG, "First create failed, retrying after chmod/modprobe")
            writeStatus("createVirtualGamepad: first attempt failed, retrying")

            RootUtils.executeCommand("chmod 666 /dev/uinput")
            uinputFd = NativeUinputManager.createUinputDevice()
            if (uinputFd > 0) {
                Log.d(TAG, "Virtual gamepad created on retry (fd=$uinputFd)")
                writeStatus("uinput created on retry fd=$uinputFd")
                return true
            }

            Log.e(TAG, "Failed to create virtual gamepad, fd=$uinputFd")
            writeStatus("FAILED to create uinput (fd=$uinputFd)")
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error creating virtual gamepad: ${e.message}", e)
            writeStatus("Exception createVirtualGamepad: ${e.message}")
            return false
        }
    }

    fun sendButton(code: Int, pressed: Boolean) {
        if (uinputFd <= 0) {
            writeStatus("sendButton ignored - no uinput")
            return
        }
        try {
            NativeUinputManager.sendKeyEvent(uinputFd, code, if (pressed) 1 else 0)
            NativeUinputManager.sync(uinputFd)
        } catch (e: Exception) {
            Log.e(TAG, "sendButton error: ${e.message}", e)
            writeStatus("sendButton error: ${e.message}")
        }
    }

    fun sendAxis(code: Int, value: Int) {
        if (uinputFd <= 0) {
            writeStatus("sendAxis ignored - no uinput")
            return
        }
        try {
            val clamped = value.coerceIn(-32768, 32767)
            NativeUinputManager.sendAbsEvent(uinputFd, code, clamped)
            NativeUinputManager.sync(uinputFd)
        } catch (e: Exception) {
            Log.e(TAG, "sendAxis error: ${e.message}", e)
            writeStatus("sendAxis error: ${e.message}")
        }
    }

    fun destroy() {
        if (uinputFd > 0) {
            try {
                NativeUinputManager.destroyUinputDevice(uinputFd)
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying uinput: ${e.message}", e)
                writeStatus("destroy error: ${e.message}")
            }
            uinputFd = -1
            writeStatus("uinput destroyed")
        }
    }
}
