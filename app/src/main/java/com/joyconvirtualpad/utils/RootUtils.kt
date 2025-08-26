package com.joyconvirtualpad.utils

import android.util.Log
import java.io.*

object RootUtils {
    private const val TAG = "RootUtils"
    
    fun isRootAvailable(): Boolean {
        Log.d(TAG, "Проверка root-доступа")
        return checkSuExists() || checkRootAccessWithCommand()
    }
    
    private fun checkSuExists(): Boolean {
        // Поиск исполняемого файла su в различных местах
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su", 
            "/sbin/su",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/data/local/su"
        )
        
        for (path in paths) {
            if (File(path).exists()) {
                Log.d(TAG, "Найден su-файл: $path")
                return true
            }
        }
        Log.d(TAG, "SU-файл не найден")
        return false
    }
    
    private fun checkRootAccessWithCommand(): Boolean {
        // Попытка выполнить простую команду с root-правами
        return try {
            Log.d(TAG, "Попытка выполнить команду с root-правами")
            val process = Runtime.getRuntime().exec("su -c id")
            val output = BufferedReader(InputStreamReader(process.inputStream))
            val response = output.readLine()
            output.close()
            process.waitFor()
            
            Log.d(TAG, "Ответ от su: $response")
            response != null && response.contains("uid=0")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при проверке root-доступа: ${e.message}")
            false
        }
    }
    
    fun executeCommand(command: String): Boolean {
        Log.d(TAG, "Выполнение команды: $command")
        return try {
            val process = Runtime.getRuntime().exec("su -c \"$command\"")
            process.waitFor()
            val result = process.exitValue() == 0
            Log.d(TAG, "Команда выполнена: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка выполнения команды: ${e.message}")
            false
        }
    }
}
