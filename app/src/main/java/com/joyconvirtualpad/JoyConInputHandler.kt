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
        if (!line.contains("(") || !line.contains("EV_")) return
        try {
            val inside = line.substringAfter("(").substringBefore(")")
            val parts = inside.trim().split(Regex("\\s+"))
            if (parts.size < 2) return
            val evType = parts[0] // EV_KEY or EV_ABS
            val codeName = parts[1]

            // map JoyCon → Xbox
            val mapped = mapNameToXbox(codeName, line)
            val code = mapped?.first ?: return
            val value = mapped.second

            onEvent(code, value)
        } catch (e: Exception) {
            Log.e("JoyConInputHandler", "parse error: ${e.message}", e)
        }
    }

    private fun mapNameToXbox(name: String, line: String): Pair<Int, Int>? {
        return when (name) {
            // ---- Buttons (совпадают с Xbox) ----
            "BTN_SOUTH" -> Pair(304, extractValue(line)) // A
            "BTN_EAST" -> Pair(305, extractValue(line))  // B
            "BTN_NORTH" -> Pair(307, extractValue(line)) // X
            "BTN_WEST" -> Pair(308, extractValue(line))  // Y
            "BTN_TL" -> Pair(310, extractValue(line))    // LB
            "BTN_TR" -> Pair(311, extractValue(line))    // RB
            "BTN_TL2" -> Pair(312, extractValue(line))   // LT
            "BTN_TR2" -> Pair(313, extractValue(line))   // RT
            "BTN_SELECT" -> Pair(314, extractValue(line))// Back
            "BTN_START" -> Pair(315, extractValue(line)) // Start
            "BTN_MODE" -> Pair(316, extractValue(line))  // Xbox/Guide
            "BTN_THUMBL" -> Pair(317, extractValue(line))// L3
            "BTN_THUMBR" -> Pair(318, extractValue(line))// R3

            // ---- D-Pad: конвертация в HAT ----
            "BTN_DPAD_UP" -> Pair(17, if (extractValue(line) == 1) -1 else 0)    // ABS_HAT0Y up
            "BTN_DPAD_DOWN" -> Pair(17, if (extractValue(line) == 1) 1 else 0)   // ABS_HAT0Y down
            "BTN_DPAD_LEFT" -> Pair(16, if (extractValue(line) == 1) -1 else 0)  // ABS_HAT0X left
            "BTN_DPAD_RIGHT" -> Pair(16, if (extractValue(line) == 1) 1 else 0)  // ABS_HAT0X right

            // ---- Axes ----
            "ABS_X" -> Pair(0, extractValue(line))   // Left stick X
            "ABS_Y" -> Pair(1, extractValue(line))   // Left stick Y
            "ABS_RX" -> Pair(3, extractValue(line))  // Right stick X
            "ABS_RY" -> Pair(4, extractValue(line))  // Right stick Y

            else -> null
        }
    }

    private fun extractValue(line: String): Int {
        return when {
            line.contains("DOWN") -> 1
            line.contains("UP") -> 0
            else -> Regex("(-?\\d+)$").find(line)?.value?.toIntOrNull() ?: 0
        }
    }
}
