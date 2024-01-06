package com.example.nextapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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

        // Start the background service
        startService(Intent(this, UsageStatsService::class.java))

        setContent {
            setContentView(R.layout.activity_main)

            // Access the TextView and update its text
            val updateStatusTextView = findViewById<TextView>(R.id.updateStatusTextView)
            updateStatusTextView.text = "Update OK"

            NextAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background) {
                }
            }
        }
    }
}