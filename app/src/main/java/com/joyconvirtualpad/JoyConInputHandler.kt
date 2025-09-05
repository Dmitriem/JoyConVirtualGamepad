package com.joyconvirtualpad

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class JoyConInputHandler(private val onEvent: (code: Int, value: Int) -> Unit) {
    private var proc: Process? = null
    private val executor = Executors.newSingleThreadExecutor()

    fun startListening() {
        try {
            // слушаем все input события через getevent -lt (root)
            proc = Runtime.getRuntime().exec(arrayOf("su", "-c", "getevent -lt"))
            val reader = BufferedReader(InputStreamReader(proc!!.inputStream))
            executor.submit {
                try {
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        line?.let { parseLine(it) }
                    }
                } catch (t: Throwable) {
                    Log.e("JoyConInputHandler", "reader loop ended: ${t.message}", t)
                }
            }
        } catch (e: Exception) {
            Log.e("JoyConInputHandler", "Failed to start getevent: ${e.message}", e)
        }
    }

    fun stopListening() {
        try {
            proc?.destroy()
            proc = null
            executor.shutdownNow()
        } catch (_: Exception) {}
    }

    private fun parseLine(line: String) {
        if (!line.contains("(") || !line.contains("EV_")) return
        try {
            val inside = line.substringAfter("(").substringBefore(")")
            val parts = inside.trim().split(Regex("\\s+"))
            if (parts.size < 2) return
            val type = parts[0]      // EV_KEY / EV_ABS
            val codeName = parts[1]  // BTN_* / ABS_*

            val value = extractValue(line)
            when (codeName) {
                // Кнопки (совпадают с Xbox)
                "BTN_SOUTH" -> onEvent(304, value) // A
                "BTN_EAST"  -> onEvent(305, value) // B
                "BTN_NORTH" -> onEvent(307, value) // X
                "BTN_WEST"  -> onEvent(308, value) // Y
                "BTN_TL"    -> onEvent(310, value) // LB
                "BTN_TR"    -> onEvent(311, value) // RB
                "BTN_TL2"   -> onEvent(312, value) // LT (цифрово)
                "BTN_TR2"   -> onEvent(313, value) // RT (цифрово)
                "BTN_SELECT"-> onEvent(314, value) // Back
                "BTN_START" -> onEvent(315, value) // Start
                "BTN_MODE"  -> onEvent(316, value) // Guide
                "BTN_THUMBL"-> onEvent(317, value) // L3
                "BTN_THUMBR"-> onEvent(318, value) // R3

                // D-Pad → HAT
                "BTN_DPAD_LEFT"  -> onEvent(16, if (value == 1) -1 else 0) // ABS_HAT0X
                "BTN_DPAD_RIGHT" -> onEvent(16, if (value == 1)  1 else 0)
                "BTN_DPAD_UP"    -> onEvent(17, if (value == 1) -1 else 0) // ABS_HAT0Y
                "BTN_DPAD_DOWN"  -> onEvent(17, if (value == 1)  1 else 0)

                // Стики
                "ABS_X"  -> onEvent(0, value)
                "ABS_Y"  -> onEvent(1, value)
                "ABS_RX" -> onEvent(3, value)
                "ABS_RY" -> onEvent(4, value)
            }
        } catch (_: Exception) {}
    }

    private fun extractValue(line: String): Int {
        return when {
            line.contains("DOWN") -> 1
            line.contains("UP")   -> 0
            else -> Regex("(-?\\d+)$").find(line)?.value?.toIntOrNull() ?: 0
        }
    }
}
