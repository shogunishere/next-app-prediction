package com.example.nextapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.nextapp.ui.theme.NextAppTheme


class MainActivity : ComponentActivity() {

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            setContentView(R.layout.activity_main)

            val updateStatusTextView = findViewById<TextView>(R.id.updateStatusTextView)
            val startServiceButton = findViewById<Button>(R.id.startServiceButton)
            val stopServiceButton = findViewById<Button>(R.id.stopServiceButton)

            startServiceButton.setOnClickListener {
                startService(Intent(this, UsageStatsService::class.java))
                updateStatusTextView.text = "Service Started"
            }

            stopServiceButton.setOnClickListener {
                stopService(Intent(this, UsageStatsService::class.java))
                updateStatusTextView.text = "Service Stopped"
            }


            NextAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background) {
                }
            }
        }
    }
}