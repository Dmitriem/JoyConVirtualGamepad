package com.joyconvirtualpad.utils

import android.view.InputDevice

object JoyConUtils {
    fun isJoyConDevice(device: InputDevice): Boolean {
        val name = device.name.toLowerCase()
        return name.contains("joy-con") || name.contains("joycon") || 
               (device.vendorId == 0x057e && (device.productId == 0x2006 || device.productId == 0x2007))
    }
}
