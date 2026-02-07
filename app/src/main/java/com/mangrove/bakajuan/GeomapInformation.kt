package com.mangrove.bakajuan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class GeomapInformation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geomap_information)

        /*  val imageBakajuan = findViewById<ImageView>(R.id.imageBakajuan)
        imageBakajuanSpecies.imageUri?.let {
            val uri = Uri.parse(it)
            imageBakajuan.setImageURI(uri)
            imageBakajuan.visibility = ImageView.VISIBLE
        } */

        // Back to Geomap
        val backToGeomap = findViewById<ImageView>(R.id.backToGeomap)
        backToGeomap.setOnClickListener {
            val intent = Intent(this, GeomapBakajuan::class.java)
            startActivity(intent)
        }

        // View in Catalog
        val viewCatalog = findViewById<Button>(R.id.viewCatalog)
        viewCatalog.setOnClickListener {
            val intent = Intent(this, CatalogBakajuan::class.java)
            startActivity(intent)
        }

        // Bottom Navigation buttons
        val navHome = findViewById<ImageButton>(R.id.navHome)
        navHome.setOnClickListener {
            startActivity(Intent(this, HomeBakajuan::class.java))
        }

        val navCatalog = findViewById<ImageButton>(R.id.navCatalog)
        navCatalog.setOnClickListener {
            startActivity(Intent(this, CatalogBakajuan::class.java))
        }

        // "Capture or Upload Mangrove" button
        val mangroveCaptureUpload = findViewById<ImageButton>(R.id.mangroveCaptureUpload)
        mangroveCaptureUpload.setOnClickListener {
            startActivity(Intent(this, CaptureUploadBakajuan::class.java))
        }

    }
}