package com.mangrove.bakajuan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mangrove.bakajuan.databinding.MangroveCatalogAdminBinding

class MangroveCatalogAdmin : AppCompatActivity() {

    private lateinit var binding: MangroveCatalogAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MangroveCatalogAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.updateMangrove.setOnClickListener {
            startActivity(Intent(this, UpdateMangrove::class.java))
        }
    }
}