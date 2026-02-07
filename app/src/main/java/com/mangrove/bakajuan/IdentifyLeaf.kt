package com.mangrove.bakajuan

import android.os.Bundle
import android.widget.ImageView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity

class IdentifyLeaf : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.identify_leaf)

        val viewFlipper = findViewById<ViewFlipper>(R.id.viewFlipper)
        val cameraToggle = findViewById<ImageView>(R.id.cameraToggle)
        val galleryToggle = findViewById<ImageView>(R.id.galleryToggle)

        // Back
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            finish()
        }

        cameraToggle.setOnClickListener {
            // switch to camera instructions
            viewFlipper.displayedChild = 0
            cameraToggle.setImageResource(R.drawable.camera_cam_gal)
            galleryToggle.setImageResource(R.drawable.camera_gal_gal)
        }

        galleryToggle.setOnClickListener {
            // switch to gallery instructions
            viewFlipper.displayedChild = 1
            cameraToggle.setImageResource(R.drawable.gallery_cam_cam)
            galleryToggle.setImageResource(R.drawable.gallery_cam_gal)
        }
    }
}
