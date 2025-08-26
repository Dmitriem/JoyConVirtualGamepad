package com.joyconvirtualpad.utils

import android.util.Log
import java.io.*

object RootUtils {

    fun isRootAvailable(): Boolean {
        // Проверяем несколько способов получить root
        return checkSuExists() && checkRootAccessWithCommand()
    }

    private fun checkSuExists(): Boolean {
        // Ищем исполняемый файл 'su' в стандартных местах
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/data/local/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) {
                Log.d("RootUtils", "SU binary found at: $path")
                return true
            }
        }
        Log.d("RootUtils", "SU binary not found in known locations")
        return false
    }

    private fun checkRootAccessWithCommand(): Boolean {
        // Пытаемся выполнить простую команду от root
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val outputStream = DataOutputStream(process.outputStream)
            val inputStream = DataInputStream(process.inputStream)
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            process.waitFor()
            val exitValue = process.exitValue()
            exitValue == 0
        } catch (e: Exception) {
            Log.e("RootUtils", "Error checking root access with command", e)
            false
        }
    }

    fun executeCommand(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val outputStream = DataOutputStream(process.outputStream)
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            Log.e("RootUtils", "Error executing root command: $command", e)
            false
        }
    }
}
