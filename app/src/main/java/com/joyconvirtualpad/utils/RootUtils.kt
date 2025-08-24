package com.joyconvirtualpad.utils

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

object JoyConUtils {
    fun isJoyConDevice(device: InputDevice): Boolean {
        val name = device.name.toLowerCase()
        return name.contains("joy-con") || name.contains("joycon") || 
               (device.vendorId == 0x057e && (device.productId == 0x2006 || device.productId == 0x2007))
    }
    
    fun mapKeyEvent(event: KeyEvent): KeyEvent {
        // Здесь преобразуем коды кнопок JoyCon в коды Xbox геймпада
        val newKeyCode = when (event.keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> KeyEvent.KEYCODE_BUTTON_A
            KeyEvent.KEYCODE_BUTTON_B -> KeyEvent.KEYCODE_BUTTON_B
            KeyEvent.KEYCODE_BUTTON_X -> KeyEvent.KEYCODE_BUTTON_X
            KeyEvent.KEYCODE_BUTTON_Y -> KeyEvent.KEYCODE_BUTTON_Y
            KeyEvent.KEYCODE_BUTTON_L1 -> KeyEvent.KEYCODE_BUTTON_L1
            KeyEvent.KEYCODE_BUTTON_R1 -> KeyEvent.KEYCODE_BUTTON_R1
            KeyEvent.KEYCODE_BUTTON_SELECT -> KeyEvent.KEYCODE_BUTTON_SELECT
            KeyEvent.KEYCODE_BUTTON_START -> KeyEvent.KEYCODE_BUTTON_START
            else -> event.keyCode
        }
        
        return KeyEvent(event.downTime, event.eventTime, event.action, newKeyCode, event.repeatCount, event.metaState, event.deviceId, event.scancode, event.flags, event.source)
    }
    
    fun mapMotionEvent(event: MotionEvent): MotionEvent {
        // Преобразование осей JoyCon в оси Xbox геймпада
        // Это упрощенный пример, может потребоваться более сложная логика
        return event
    }
}