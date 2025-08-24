package com.joyconvirtualpad.utils

import android.util.Log

object RootUtils {
    fun isRootAvailable(): Boolean {
        return try {
            // Простая проверка root доступа
            Runtime.getRuntime().exec("su").exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }
}
