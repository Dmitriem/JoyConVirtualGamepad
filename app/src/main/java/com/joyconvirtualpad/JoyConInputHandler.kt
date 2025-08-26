package com.joyconvirtualpad

import android.util.Log
import java.io.DataInputStream
import java.io.FileInputStream

class JoyConInputHandler(private val eventCallback: (Int, Int, Int) -> Unit) {
    private var isListening = false
    
    // Эти пути нужно определить экспериментально для вашего устройства
    private val joyConPaths = arrayOf(
        "/dev/input/event0", // Возможный путь к левому Joy-Con
        "/dev/input/event1", // Возможный путь к правому Joy-Con
        "/dev/input/event2",
        "/dev/input/event3"
    )
    
    fun startListening() {
        isListening = true
        
        // Запускаем мониторинг для каждого возможного пути
        joyConPaths.forEachIndexed { deviceId, path ->
            Thread {
                monitorInputDevice(path, deviceId)
            }.start()
        }
        
        Log.d("JoyConInputHandler", "Started listening for Joy-Con events")
    }
    
    private fun monitorInputDevice(path: String, deviceId: Int) {
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
                
                // Фильтруем только интересующие нас события
                if (type == 1 || type == 3) { // EV_KEY или EV_ABS
                    // Преобразуем код события Joy-Con в код виртуального геймпада
                    val mappedCode = mapJoyConToXbox(code, deviceId)
                    if (mappedCode != -1) {
                        eventCallback(mappedCode, value, deviceId)
                    }
                }
            }
            dis.close()
        } catch (e: Exception) {
            // Этот путь может не существовать или не быть Joy-Con - это нормально
            Log.d("JoyConInputHandler", "Cannot read from $path: ${e.message}")
        }
    }
    
    private fun mapJoyConToXbox(joyConCode: Int, deviceId: Int): Int {
        // Здесь нужно преобразовать коды кнопок Joy-Con в коды Xbox геймпада
        // Это упрощенная реализация - нужно будет настроить точное соответствие
        
        return when (joyConCode) {
            // Пример маппинга для левого Joy-Con (deviceId = 0)
            304 -> if (deviceId == 0) 304 else -1 // Кнопка A
            305 -> if (deviceId == 0) 305 else -1 // Кнопка B
            // Добавьте другие кнопки по аналогии
            else -> -1 // Игнорируем неизвестные коды
        }
    }
    
    fun stopListening() {
        isListening = false
        Log.d("JoyConInputHandler", "Stopped listening for Joy-Con events")
    }
}
