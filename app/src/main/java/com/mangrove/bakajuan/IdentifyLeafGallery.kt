package com.mangrove.bakajuan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

class IdentifyLeafGallery : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.identify_leaf_gallery)

        // Back
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            finish()
        }

        // Camera button
        val camera = findViewById<ImageView>(R.id.camera)
        camera.setOnClickListener {
            val intent = Intent(this, IdentifyLeafCamera::class.java)
            startActivity(intent)
        }
    }
}