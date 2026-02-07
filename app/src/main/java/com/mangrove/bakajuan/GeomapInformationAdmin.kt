package com.mangrove.bakajuan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mangrove.bakajuan.databinding.GeomapInformationAdminBinding

class GeomapInformationAdmin : AppCompatActivity() {

    private lateinit var binding: GeomapInformationAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GeomapInformationAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        val speciesId = intent.getStringExtra("speciesId") ?: ""
        val speciesName = intent.getStringExtra("speciesName") ?: ""
        val speciesImageUrl = intent.getStringExtra("speciesImage") ?: ""

        // Set data in views
        binding.speciesID.text = speciesId
        binding.localName.text = speciesName

        if (speciesImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(speciesImageUrl)
                .placeholder(R.drawable.unround)
                .into(binding.imageBakajuan)
        }

        // View in Catalog -> directly to CatalogInformation
        binding.viewCatalog.setOnClickListener {
            val intent = Intent(this, CatalogInformation::class.java)
            intent.putExtra("speciesId", speciesId)
            intent.putExtra("speciesName", speciesName)
            intent.putExtra("speciesImage", speciesImageUrl)
            startActivity(intent)
        }
    }
}