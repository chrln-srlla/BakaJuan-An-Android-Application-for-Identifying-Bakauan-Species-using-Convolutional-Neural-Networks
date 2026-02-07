package com.mangrove.bakajuan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.mangrove.bakajuan.databinding.CatalogInformationBinding

class CatalogInformation : AppCompatActivity() {

    private lateinit var binding: CatalogInformationBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var coordinatesReference: DatabaseReference

    // store lat & lon if available
    private var speciesLatitude: String? = null
    private var speciesLongitude: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CatalogInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get speciesID from intent
        val speciesID = intent.getStringExtra("speciesID")
        if (speciesID != null) {
            fetchSpeciesData(speciesID)
            fetchSpeciesCoordinates(speciesID)
        } else {
            Toast.makeText(this, "Species ID not found", Toast.LENGTH_SHORT).show()
        }

        // Back to Catalog
        binding.back.setOnClickListener {
            finish()
        }

        binding.locateMangrove.setOnClickListener {
            if (!speciesLatitude.isNullOrEmpty() && !speciesLongitude.isNullOrEmpty()) {
                val intent = Intent(this, GeomapBakajuan::class.java).apply {
                    putExtra("latitude", speciesLatitude)
                    putExtra("longitude", speciesLongitude)
                    putExtra("speciesID", speciesID)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Coordinates not available for this species", Toast.LENGTH_SHORT).show()
            }
        }

        // Bottom Navigation Buttons
        binding.navHome.setOnClickListener {
            startActivity(Intent(this, HomeBakajuan::class.java))
        }

        binding.navGeomap.setOnClickListener {
            startActivity(Intent(this, GeomapBakajuan::class.java))
        }

        // "Capture or Upload Mangrove" button
        binding.mangroveCaptureUpload.setOnClickListener {
            startActivity(Intent(this, CaptureUploadBakajuan::class.java))
        }
    }

    private fun fetchSpeciesData(speciesID: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Mangrove Information")

        databaseReference.child(speciesID).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val species = snapshot.getValue(BakajuanSpeciesData::class.java)
                    Log.d("MangroveInformation", "Species data found: $species")

                    species?.let {
                        binding.localName.text = it.localName
                        binding.speciesID.text = it.speciesID
                        binding.mangroveZone.text = it.zone
                        binding.scientificName.text = it.scientificName
                        binding.mangroveDescription.text = it.characteristics

                        binding.height.text = if (species.height.isNullOrEmpty()) "No Height" else "${species.height} m"
                        binding.circumference.text = if (species.circumference.isNullOrEmpty()) "No Circumference" else "${species.circumference} cm"
                        binding.carbonSequestered.text = if (species.estimatedCarbonSequestered.isNullOrEmpty()) "No CO₂ Sequestered" else species.estimatedCarbonSequestered
                        binding.estimatedAge.text = if (species.estimatedAge.isNullOrEmpty()) "No Est. Age" else species.estimatedAge

                        // image
                        if (!it.mangroveImage.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(it.mangroveImage)
                                .into(binding.mangroveImage)
                        }

                        // date catalogued
                        val dateTextView = binding.dateCatalogued
                        it.dateCatalogued?.let { timestamp ->
                            try {
                                val tsLong = timestamp.toLong()
                                val sdf = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
                                dateTextView.text = sdf.format(java.util.Date(tsLong))
                            } catch (e: Exception) {
                                dateTextView.text = it.dateCatalogued.toString()
                            }
                        } ?: run {
                            dateTextView.text = "N/A"
                        }
                    }
                } else {
                    Toast.makeText(this, "No data found for ID: $speciesID", Toast.LENGTH_SHORT).show()
                    Log.w("MangroveInformation", "Snapshot does not exist for ID: $speciesID")
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to load species data", Toast.LENGTH_SHORT).show()
                Log.e("MangroveInformation", "Firebase error: ${error.message}")
            }
    }

    // latitude & longitude
    private fun fetchSpeciesCoordinates(speciesID: String) {
        coordinatesReference = FirebaseDatabase.getInstance().getReference("Species Coordinates")

        coordinatesReference.child(speciesID).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val latitude = snapshot.child("latitude").getValue(Double::class.java)
                    val longitude = snapshot.child("longitude").getValue(Double::class.java)

                    if (latitude != null && longitude != null) {
                        speciesLatitude = latitude.toString()
                        speciesLongitude = longitude.toString()

                        binding.latitude.text = speciesLatitude
                        binding.longitude.text = speciesLongitude
                        binding.latitudeRow.visibility = View.VISIBLE
                        binding.longitudeRow.visibility = View.VISIBLE
                    } else {
                        binding.latitudeRow.visibility = View.GONE
                        binding.longitudeRow.visibility = View.GONE
                    }
                } else {
                    binding.latitudeRow.visibility = View.GONE
                    binding.longitudeRow.visibility = View.GONE
                    Log.w("SpeciesCoordinates", "No coordinates found for ID: $speciesID")
                }
            }
            .addOnFailureListener { error ->
                Log.e("SpeciesCoordinates", "Firebase error: ${error.message}")
            }
    }
}