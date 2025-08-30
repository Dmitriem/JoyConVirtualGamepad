package com.joyconvirtualpad

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.joyconvirtualpad.utils.RootUtils
import android.content.Context
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var enableSwitch: Switch
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Запрос исключения из оптимизации батареи
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        statusText = findViewById(R.id.tvStatus)
        enableSwitch = findViewById(R.id.switchEnable)
        
        // Проверка root-прав
        checkRootAccess()
        
        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startService()
            } else {
                stopService()
            }
        }
    }
    
    private fun checkRootAccess() {
        if (!RootUtils.isRootAvailable()) {
            statusText.text = "Требуется root доступ!"
            enableSwitch.isEnabled = false
        } else {
            statusText.text = "Root доступ получен"
            enableSwitch.isEnabled = true
        }
    }
    
    private fun startService() {
        try {
            val serviceIntent = Intent(this, JoyConMapperService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            statusText.text = "Виртуальный геймпад включен"
        } catch (e: Exception) {
            statusText.text = "Ошибка запуска сервиса"
            enableSwitch.isChecked = false
        }
    }
    
    private fun stopService() {
        try {
            val serviceIntent = Intent(this, JoyConMapperService::class.java)
            stopService(serviceIntent)
            statusText.text = "Виртуальный геймпад выключен"
        } catch (e: Exception) {
            statusText.text = "Ошибка остановки сервиса"
        }
    }
    
    override fun onResume() {
        super.onResume()
        // При возвращении в приложение обновляем статус
        checkRootAccess()
    }
}
