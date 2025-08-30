package com.joyconvirtualpad

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors
import kotlin.concurrent.thread

/**
 * Простая реализация: запускает "su -c getevent -lt" и парсит строки.
 * onEvent: code (int) — код события (EV_KEY/EV_ABS), value — значение (0/1 для кнопок, int для осей).
 *
 * Замечание: этот парсер ориентирован на вывод getevent -lt.
 */
class JoyConInputHandler(private val onEvent: (code: Int, value: Int) -> Unit) {
    private var proc: Process? = null
    private val executor = Executors.newSingleThreadExecutor()

    fun startListening() {
        try {
            val cmd = arrayOf("su", "-c", "getevent -lt")
            proc = Runtime.getRuntime().exec(cmd)
            val reader = BufferedReader(InputStreamReader(proc!!.inputStream))

            executor.submit {
                try {
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        line?.let { parseLine(it) }
                    }
                } catch (t: Throwable) {
                    Log.e("JoyConInputHandler", "Reader loop ended: ${t.message}", t)
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
        // Примеры строк getevent -lt:
        // [ 1756555658.590540] /dev/input/event5: 0003 0003 fffff8d1  (EV_ABS ABS_RX -29327)
        // [ 1756555658.590540] /dev/input/event5: 0001 014a 0001        (EV_KEY BTN_SOUTH DOWN)
        try {
            if (!line.contains("EV_")) return

            // извлечь код и значение внутри скобок
            val idx = line.indexOf('(')
            val idx2 = line.indexOf(')')
            if (idx == -1 || idx2 == -1) return
            val inside = line.substring(idx + 1, idx2) // e.g. "EV_KEY BTN_SOUTH DOWN" или "EV_ABS ABS_RX -29327"
            val parts = inside.trim().split(Regex("\\s+"))
            if (parts.size < 2) return

            val evType = parts[0] // EV_KEY or EV_ABS
            val codeName = parts[1] // e.g. BTN_SOUTH or ABS_RX

            // Преобразуем имя к коду (Android input keycodes в linux input.h)
            val code = mapNameToCode(codeName) ?: return

            if (evType == "EV_KEY") {
                // значение: DOWN/UP — в выводе иногда есть слово, но если нет, парсим число в конце
                val value = when {
                    inside.contains("DOWN") -> 1
                    inside.contains("UP") -> 0
                    else -> {
                        // попытаемся взять число из конца строки
                        val nums = Regex("(-?\\d+)$").find(line)
                        nums?.value?.toIntOrNull() ?: 0
                    }
                }
                onEvent(code, value)
            } else if (evType == "EV_ABS") {
                // ось — последнее число
                val nums = Regex("(-?\\d+)$").find(line)
                val value = nums?.value?.toIntOrNull() ?: 0
                onEvent(code, value)
            }
        } catch (e: Exception) {
            Log.e("JoyConInputHandler", "parseLine error: ${e.message}", e)
        }
    }

    private fun mapNameToCode(name: String): Int? {
        return when (name) {
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
            // ABS
            "ABS_X" -> 0
            "ABS_Y" -> 1
            "ABS_RX" -> 3
            "ABS_RY" -> 4
            "ABS_HAT0X" -> 16
            "ABS_HAT0Y" -> 17
            else -> {
                // дополнительные: map common aliases
                when {
                    name.startsWith("ABS_") -> {
                        // если не нашли — возвращаем null
                        null
                    }
                    name.startsWith("BTN_") -> null
                    else -> null
                }
            }
        }
    }
}
