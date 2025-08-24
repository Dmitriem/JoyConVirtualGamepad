package com.joyconvirtualpad

import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var enableSwitch: Switch
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        statusText = findViewById(R.id.tvStatus)
        enableSwitch = findViewById(R.id.switchEnable)
        
        statusText.text = "Приложение запущено"
        
        enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                statusText.text = "Виртуальный геймпад включен"
            } else {
                statusText.text = "Виртуальный геймпад выключен"
            }
        }
    }
}
