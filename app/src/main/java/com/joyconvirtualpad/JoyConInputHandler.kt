package com.joyconvirtualpad

import android.util.Log
import java.io.DataInputStream
import java.io.FileInputStream

class JoyConInputHandler(private val eventCallback: (Int, Int) -> Unit) {
    private var isListening = false
    
    // Путь к объединенному устройству Joy-Con
    private val joyConPath = "/dev/input/event8"
    
    fun startListening() {
        isListening = true
        
        Thread {
            monitorInputDevice(joyConPath)
        }.start()
        
        Log.d("JoyConInputHandler", "Started listening for Joy-Con events")
    }
    
    private fun monitorInputDevice(path: String) {
        try {
            val dis = DataInputStream(FileInputStream(path))
            while (isListening) {
                // Чтение события из input устройства
                // Структура: timeval (8 bytes) + type (2 bytes) + code (2 bytes) + value (4 bytes)
                val sec = dis.readInt()
                val usec = dis.readInt()
                val type = dis.readShort().toInt()
                val code = dis.readShort().toInt()
                val value = dis.readInt()
                
                // Обрабатываем только события кнопок (EV_KEY)
                if (type == 1) { // EV_KEY
                    // Преобразуем код события Joy-Con в код виртуального геймпада
                    val mappedCode = mapJoyConToXbox(code)
                    if (mappedCode != -1) {
                        eventCallback(mappedCode, value)
                        Log.d("JoyConInputHandler", "Mapped code: $code -> $mappedCode, value: $value")
                    }
                }
            }
            dis.close()
        } catch (e: Exception) {
            Log.e("JoyConInputHandler", "Error reading from $path: ${e.message}")
        }
    }
    
    private fun mapJoyConToXbox(joyConCode: Int): Int {
        // Преобразование кодов кнопок Joy-Con в коды Xbox геймпада
        return when (joyConCode) {
            // Правый Joy-Con: кластер кнопок
            305 -> 304 // Joy-Con A (BTN_EAST) -> Xbox A (BTN_SOUTH)
            304 -> 305 // Joy-Con B (BTN_SOUTH) -> Xbox B (BTN_EAST)
            308 -> 307 // Joy-Con Y (BTN_WEST) -> Xbox Y (BTN_NORTH)
            307 -> 308 // Joy-Con X (BTN_NORTH) -> Xbox X (BTN_WEST)
            
            // Левый Joy-Con: крестовина
            544 -> 544 // BTN_DPAD_UP -> BTN_DPAD_UP
            545 -> 545 // BTN_DPAD_DOWN -> BTN_DPAD_DOWN
            546 -> 546 // BTN_DPAD_LEFT -> BTN_DPAD_LEFT
            547 -> 547 // BTN_DPAD_RIGHT -> BTN_DPAD_RIGHT
            
            // Общие кнопки
            314 -> 314 // BTN_SELECT -> BTN_SELECT (Minus -> Back)
            315 -> 315 // BTN_START -> BTN_START (Plus -> Start)
            316 -> 316 // BTN_MODE -> BTN_MODE (Home -> Guide)
            
            // Триггеры и бамперы
            310 -> 310 // BTN_TL (L) -> BTN_TL (LB)
            311 -> 311 // BTN_TR (R) -> BTN_TR (RB)
            312 -> 312 // BTN_TL2 (ZL) -> оставляем как кнопку
            313 -> 313 // BTN_TR2 (ZR) -> оставляем как кнопку
            
            // Нажатие стиков
            317 -> 317 // BTN_THUMBL (L3) -> BTN_THUMBL
            318 -> 318 // BTN_THUMBR (R3) -> BTN_THUMBR
            
            // Кнопка Capture (игнорируем, так как на Xbox нет аналога)
            309 -> -1
            
            else -> -1 // Игнорируем неизвестные коды
        }
    }
    
    fun stopListening() {
        isListening = false
        Log.d("JoyConInputHandler", "Stopped listening for Joy-Con events")
    }
}
