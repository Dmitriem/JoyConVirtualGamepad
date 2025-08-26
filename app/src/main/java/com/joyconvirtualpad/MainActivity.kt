package com.joyconvirtualpad

import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.joyconvirtualpad.utils.RootUtils

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var enableSwitch: Switch
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        statusText = findViewById(R.id.tvStatus)
        enableSwitch = findViewById(R.id.switchEnable)
        
        // Проверка root-прав
        if (!RootUtils.isRootAvailable()) {
            statusText.text = "Требуется root доступ!"
            enableSwitch.isEnabled = false
            return
        }
        
        statusText.text = "Root доступ получен"
        
        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Запуск сервиса
                val serviceIntent = Intent(this, JoyConMapperService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                statusText.text = "Виртуальный геймпад включен"
            } else {
                // Остановка сервиса
                val serviceIntent = Intent(this, JoyConMapperService::class.java)
                stopService(serviceIntent)
                statusText.text = "Виртуальный геймпад выключен"
            }
        }
    }
}package com.joyconvirtualpad

import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.joyconvirtualpad.utils.RootUtils

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var enableSwitch: Switch
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        statusText = findViewById(R.id.tvStatus)
        enableSwitch = findViewById(R.id.switchEnable)
        
        // Проверка root-прав
        if (!RootUtils.isRootAvailable()) {
            statusText.text = "Требуется root доступ!"
            enableSwitch.isEnabled = false
            return
        }
        
        statusText.text = "Root доступ получен"
        
        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Запуск сервиса с root-правами
                RootUtils.executeCommand("am startservice -n com.joyconvirtualpad/.JoyConMapperService")
                statusText.text = "Виртуальный геймпад включен"
            } else {
                // Остановка сервиса
                RootUtils.executeCommand("am stopservice com.joyconvirtualpad/.JoyConMapperService")
                statusText.text = "Виртуальный геймпад выключен"
            }
        }
    }
}
