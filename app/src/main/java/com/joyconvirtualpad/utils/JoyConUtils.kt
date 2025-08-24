package com.joyconvirtualpad.utils

import android.util.Log

object RootUtils {
    private const val TAG = "RootUtils"
    
    fun isRootAvailable(): Boolean {
        return try {
            Runtime.getRuntime().exec("su").exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }
    
    fun executeCommand(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream
            outputStream.write(command.toByteArray())
            outputStream.flush()
            outputStream.close()
            process.waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error executing root command", e)
            false
        }
    }
}