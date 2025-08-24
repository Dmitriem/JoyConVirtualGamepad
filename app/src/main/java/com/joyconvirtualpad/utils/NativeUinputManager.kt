package com.joyconvirtualpad.utils

object NativeUinputManager {
    fun createUinputDevice(): Int {
        return 1 // Заглушка для тестирования
    }
    
    fun sendKeyEvent(fd: Int, code: Int, value: Int) {
        // Заглушка для тестирования
    }
    
    fun destroyUinputDevice(fd: Int) {
        // Заглушка для тестирования
    }
}
