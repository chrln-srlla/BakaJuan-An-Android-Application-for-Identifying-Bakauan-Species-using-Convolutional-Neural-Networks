package com.mangrove.bakajuan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView

class IdentifySplash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.identify_splash)

        // To Map
        val toMap = findViewById<ImageView>(R.id.toMap)
        toMap.setOnClickListener {
            val intent = Intent(this, MapSplash::class.java)
            startActivity(intent)
        }

        // Skip
        val skip = findViewById<TextView>(R.id.skip)
        skip.setOnClickListener {
            val intent = Intent(this, AdminUser::class.java)
            startActivity(intent)
        }

    }
}