package com.mangrove.bakajuan

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mangrove.bakajuan.databinding.MangroveCatalogUserBinding

class MangroveCatalogUser : AppCompatActivity() {

    private lateinit var binding: MangroveCatalogUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MangroveCatalogUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val metrics = resources.displayMetrics
        val screenDpWidth = metrics.widthPixels / metrics.density

        val localName = findViewById<TextView>(R.id.localName)
        val speciesIDLabel = findViewById<TextView>(R.id.speciesIDLabel)
        val speciesID = findViewById<TextView>(R.id.speciesID)

        if (screenDpWidth <= 360) {
            localName.textSize = 16f
            speciesIDLabel.textSize = 15f
            speciesID.textSize = 15f
        } else {
            localName.textSize = 19f
            speciesIDLabel.textSize = 18f
            speciesID.textSize = 18f
        }
    }
}