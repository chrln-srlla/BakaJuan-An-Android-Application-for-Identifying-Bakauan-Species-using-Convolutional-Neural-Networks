package com.mangrove.bakajuan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView

class CatalogSplash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.catalog_splash)

        // To Admin or User
        val toAdminUser = findViewById<ImageView>(R.id.toAdminUser)
        toAdminUser.setOnClickListener {
            val intent = Intent(this, AdminUser::class.java)
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