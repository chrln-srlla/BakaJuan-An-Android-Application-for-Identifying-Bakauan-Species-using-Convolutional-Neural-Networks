package com.mangrove.bakajuan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView

class MapSplash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_splash)

        // To Catalog
        val toCatalog = findViewById<ImageView>(R.id.toCatalog)
        toCatalog.setOnClickListener {
            val intent = Intent(this, CatalogSplash::class.java)
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