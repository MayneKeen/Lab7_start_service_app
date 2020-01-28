package com.example.lab7_start_service_app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity = this

        start_service_button.setOnClickListener {
            val intent = Intent(this, DownloadImageService::class.java)
            intent.putExtra(
                "EXTRA_URL",
                edittext_with_url.text.toString()
            )
            startService(intent)
            Log.i("MainActivity", "Service started with $intent")
        }
    }

    companion object {
        private lateinit var activity: MainActivity
    }
}
