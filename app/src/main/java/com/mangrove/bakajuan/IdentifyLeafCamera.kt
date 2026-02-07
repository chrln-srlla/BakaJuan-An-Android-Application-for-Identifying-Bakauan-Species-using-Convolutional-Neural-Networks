package com.mangrove.bakajuan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

class IdentifyLeafCamera : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.identify_leaf_camera)

        // Back
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            finish()
        }

        // Gallery button
        val gallery = findViewById<ImageView>(R.id.gallery)
        gallery.setOnClickListener {
            val intent = Intent(this, IdentifyLeafGallery::class.java)
            startActivity(intent)
        }
    }
}