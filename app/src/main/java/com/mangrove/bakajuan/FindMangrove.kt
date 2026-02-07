package com.mangrove.bakajuan

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class FindMangrove : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_mangrove)

        // Back to Home button
        val backToHome = findViewById<ImageView>(R.id.backToHome)
        backToHome.setOnClickListener {
            val intent = Intent(this, HomeBakajuan::class.java)
            startActivity(intent)
        }
    }
}