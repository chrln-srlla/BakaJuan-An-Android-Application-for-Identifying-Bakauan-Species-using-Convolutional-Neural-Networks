package com.mangrove.bakajuan

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat

class AboutUs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_us)

        val next = findViewById<ImageView>(R.id.next)
        next.setOnClickListener {
            val intent = Intent(this, AboutUs2::class.java)
            startActivity(intent)
        }

        val skip = findViewById<TextView>(R.id.skip)
        skip.setOnClickListener {
            val intent = Intent(this, AdminUser::class.java)
            startActivity(intent)
        }

        val descriptionText = findViewById<TextView>(R.id.descriptionText)
        val styledText = HtmlCompat.fromHtml(
            "<font color='#263B1A'><b>Baka</b></font><font color='#799A0B'><b>Juan</b></font>" +
                    " is a mobile application developed to identify Mangrove (Bakauan) leaf species through image recognition found within the Cabusao Wetlands Critical Habitat, an ecological zone in Biong and Pandan, Cabusao, Camarines Sur. This app specifically focuses on the recognition of mangrove species such as:",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        descriptionText.text = styledText

    }
}
