package com.joyconvirtualpad

import com.joyconvirtualpad.utils.NativeUinputManager

class VirtualGamepadManager {
    private var uinputFd: Int = -1
    
    fun createVirtualGamepad(): Boolean {
        return try {
            uinputFd = NativeUinputManager.createUinputDevice()
            uinputFd > 0
        } catch (e: Exception) {
            false
        }
    }
    
    fun sendKeyEvent(event: KeyEvent) {
        if (uinputFd <= 0) return
        
        val mappedEvent = JoyConUtils.mapKeyEvent(event)
        NativeUinputManager.sendKeyEvent(uinputFd, mappedEvent.keyCode, mappedEvent.action)
    }
    
    fun sendMotionEvent(event: MotionEvent) {
        // Реализация для отправки событий движения
    }
    
    fun destroy() {
        if (uinputFd > 0) {
            NativeUinputManager.destroyUinputDevice(uinputFd)
            uinputFd = -1
        }
    }
}