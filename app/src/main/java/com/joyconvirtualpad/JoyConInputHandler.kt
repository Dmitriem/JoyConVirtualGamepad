package com.joyconvirtualpad

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

/**
 * Запускает "su -c getevent -lt" и парсит вывод.
 * Передаёт в onEvent код (линейный код EV_KEY/EV_ABS) и значение.
 */
class JoyConInputHandler(private val onEvent: (code: Int, value: Int) -> Unit) {
    private var proc: Process? = null
    private val executor = Executors.newSingleThreadExecutor()

    fun startListening() {
        try {
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
            Log.d("JoyConInputHandler", "Started getevent parser")
        } catch (e: Exception) {
            Log.e("JoyConInputHandler", "Failed to start getevent: ${e.message}", e)
        }
    }

    fun stopListening() {
        try {
            proc?.destroy()
            proc = null
            executor.shutdownNow()
            Log.d("JoyConInputHandler", "Stopped listening")
        } catch (e: Exception) {
            Log.e("JoyConInputHandler", "Error stopping: ${e.message}", e)
        }
    }

    private fun parseLine(line: String) {
        // интересуют строки с '(EV_KEY ...)' или '(EV_ABS ...)'
        if (!line.contains("(") || !line.contains("EV_")) return
        try {
            val inside = line.substringAfter("(").substringBefore(")")
            val parts = inside.trim().split(Regex("\\s+"))
            if (parts.size < 2) return
            val evType = parts[0] // EV_KEY or EV_ABS
            val codeName = parts[1] // e.g. BTN_SOUTH or ABS_RX

            val code = mapNameToCode(codeName) ?: return

            if (evType == "EV_KEY") {
                val value = when {
                    inside.contains("DOWN") -> 1
                    inside.contains("UP") -> 0
                    else -> Regex("(-?\\d+)$").find(line)?.value?.toIntOrNull() ?: 0
                }
                onEvent(code, value)
            } else if (evType == "EV_ABS") {
                val value = Regex("(-?\\d+)$").find(line)?.value?.toIntOrNull() ?: 0
                onEvent(code, value)
            }
        } catch (e: Exception) {
            Log.e("JoyConInputHandler", "parse error: ${e.message}", e)
        }
    }

    private fun mapNameToCode(name: String): Int? {
        // EV_KEY -> linux BTN_* codes, EV_ABS -> ABS_* integer codes
        return when (name) {
            // buttons -> BTN_* linux codes
            "BTN_SOUTH" -> 304 // A
            "BTN_EAST" -> 305  // B
            "BTN_NORTH" -> 307 // X
            "BTN_WEST" -> 308  // Y
            "BTN_TL" -> 310    // L
            "BTN_TR" -> 311    // R
            "BTN_TL2" -> 312   // L2
            "BTN_TR2" -> 313   // R2
            "BTN_THUMBL" -> 317
            "BTN_THUMBR" -> 318
            "BTN_START" -> 315
            "BTN_SELECT" -> 314

            // axes -> ABS_* codes (numbers as in linux/input-event-codes.h)
            "ABS_X" -> 0
            "ABS_Y" -> 1
            "ABS_RX" -> 3
            "ABS_RY" -> 4
            "ABS_HAT0X" -> 16
            "ABS_HAT0Y" -> 17

            else -> null
        }
    }
}
