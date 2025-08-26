package com.joyconvirtualpad.utils

import android.util.Log
import java.io.DataOutputStream
import java.io.IOException

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
            val outputStream = DataOutputStream(process.outputStream)
            outputStream.writeBytes("$command\n")
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            outputStream.close()
            process.waitFor() == 0
        } catch (e: IOException) {
            Log.e(TAG, "Error executing root command", e)
            false
        }
    }
}
