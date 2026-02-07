package com.mangrove.bakajuan

import android.app.Dialog
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.mangrove.bakajuan.databinding.AdminMapBinding

class AdminMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: AdminMapBinding
    private var googleMap: GoogleMap? = null
    private lateinit var database: DatabaseReference
    private lateinit var mangroveInfoDb: DatabaseReference

    private var userMarker: Marker? = null

    // Cabusao Wetlands
    private val cabusaoWetlands = listOf(
        LatLng(13.747474, 123.092829),
        LatLng(13.754256, 123.079387),
        LatLng(13.761675, 123.065405),
        LatLng(13.766441, 123.057350),
        LatLng(13.766891, 123.057822),
        LatLng(13.761890, 123.066127),
        LatLng(13.754458, 123.079986),
        LatLng(13.747973, 123.093475)
    )

    // ZONE
    private val landwardZone = listOf(
        LatLng(13.761675, 123.065405),
        LatLng(13.766441, 123.057350),
        LatLng(13.766891, 123.057822),
        LatLng(13.761890, 123.066127)
    )

    private val midwardZone = listOf(
        LatLng(13.754256, 123.079387),
        LatLng(13.761675, 123.065405),
        LatLng(13.761890, 123.066127),
        LatLng(13.754458, 123.079986)
    )

    private val seawardZone = listOf(
        LatLng(13.747474, 123.092829),
        LatLng(13.754256, 123.079387),
        LatLng(13.754458, 123.079986),
        LatLng(13.747973, 123.093475)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance().getReference("Species Coordinates")
        mangroveInfoDb = FirebaseDatabase.getInstance().getReference("Mangrove Information")

        val receivedSpeciesId = intent.getStringExtra("speciesId")
        if (!receivedSpeciesId.isNullOrEmpty()) {
            binding.speciesID.setText(receivedSpeciesId)
        }

        val decimalFilter = object : InputFilter {
            override fun filter(
                source: CharSequence?, start: Int, end: Int,
                dest: Spanned?, dstart: Int, dend: Int
            ): CharSequence? {
                val newText = (dest?.substring(0, dstart) ?: "") +
                        (source?.substring(start, end) ?: "") +
                        (dest?.substring(dend) ?: "")
                return if (newText.matches(Regex("^-?\\d{0,3}(\\.\\d{0,6})?$"))) null else ""
            }
        }
        binding.latitude.filters = arrayOf(decimalFilter)
        binding.longitude.filters = arrayOf(decimalFilter)

        // map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // save map coordinates
        binding.saveCoordinates.setOnClickListener {
            if (googleMap == null) {
                Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.saveCoordinates.isEnabled = false
            saveCoordinatesProcess()
        }

        binding.backToDashboard.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
            noTransition()
        }
        binding.logMangrove.setOnClickListener {
            startActivity(Intent(this, AdminMangrove::class.java))
            noTransition()
        }
        binding.viewCatalog.setOnClickListener {
            startActivity(Intent(this, AdminCatalog::class.java))
            noTransition()
        }
        binding.accountSettings.setOnClickListener {
            startActivity(Intent(this, AccountSettings::class.java))
            noTransition()
        }

        // screen adjustments
        val metrics = resources.displayMetrics
        val screenDpWidth = metrics.widthPixels / metrics.density
        if (screenDpWidth <= 360) {
            binding.logSpecies.textSize = 9f
            binding.mapSpecies.textSize = 9f
            binding.catalogSpecies.textSize = 9f
            binding.accAdmin.textSize = 9f
        }

        // hide bottom navigation when keyboard is visible
        binding.adminMap.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.adminMap.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.adminMap.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            binding.bottomNav.visibility = if (keypadHeight > screenHeight * 0.15) View.GONE else View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)
    }

    private fun noTransition() {
        overridePendingTransition(0, 0)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE

        // Cabusao Polygon
        val polygonPoints = cabusaoWetlands + cabusaoWetlands.first()
        googleMap?.addPolygon(
            PolygonOptions()
                .addAll(polygonPoints)
                .strokeColor(Color.GREEN)
                .fillColor(Color.TRANSPARENT)
                .strokeWidth(5f)
        )

        googleMap?.addPolygon(PolygonOptions()
            .addAll(landwardZone)
            .strokeColor(Color.GREEN)
            .fillColor(Color.TRANSPARENT)
            .strokeWidth(5f))

        googleMap?.addPolygon(PolygonOptions()
            .addAll(midwardZone)
            .strokeColor(Color.GREEN)
            .fillColor(Color.TRANSPARENT)
            .strokeWidth(5f))

        googleMap?.addPolygon(PolygonOptions()
            .addAll(seawardZone)
            .strokeColor(Color.GREEN)
            .fillColor(Color.TRANSPARENT)
            .strokeWidth(5f))

        val centerLocation = getPolygonCenter(cabusaoWetlands)

        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLocation, 14f))

        googleMap?.setOnMapClickListener { point ->
            if (!isWithinCabusaoWetlands(point.latitude, point.longitude)) {
                Toast.makeText(this, "Outside Cabusao Wetlands", Toast.LENGTH_SHORT).show()
                return@setOnMapClickListener
            }

            val zone = getZone(point.latitude, point.longitude)

            userMarker?.remove()

            userMarker = googleMap?.addMarker(
                MarkerOptions()
                    .position(point)
                    .title("Lat: %.6f, Lng: %.6f".format(point.latitude, point.longitude))
                    .snippet("Zone: $zone")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            userMarker?.showInfoWindow()

            // fill fields
            binding.latitude.setText("%.6f".format(point.latitude))
            binding.longitude.setText("%.6f".format(point.longitude))
        }

        addWetlandsMarker()
    }

    private fun getZoneByBoundary(lat: Double, lng: Double): String? {
        return when {
            isPointInPolygon(lat, lng, landwardZone) -> "Landward"
            isPointInPolygon(lat, lng, midwardZone) -> "Midward"
            isPointInPolygon(lat, lng, seawardZone) -> "Seaward"
            else -> null
        }
    }

    private fun getZone(lat: Double, lng: Double): String {
        return when {
            isPointInPolygon(lat, lng, landwardZone) -> "Landward"
            isPointInPolygon(lat, lng, midwardZone) -> "Midward"
            isPointInPolygon(lat, lng, seawardZone) -> "Seaward"

            else -> "Outside Zones"
        }
    }

    private fun addWetlandsMarker() {
        val polygonCenter = LatLng(
            cabusaoWetlands.map { it.latitude }.average(),
            cabusaoWetlands.map { it.longitude }.average()
        )

        val drawable = ContextCompat.getDrawable(this, R.drawable.map_wetlands)!!
        val width = 200
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        val icon = BitmapDescriptorFactory.fromBitmap(bitmap)

        googleMap?.addMarker(
            MarkerOptions()
                .position(polygonCenter)
                .icon(icon)
                .anchor(0.5f, 1f)
        )
    }

    private fun getPolygonCenter(polygon: List<LatLng>): LatLng {
        val latSum = polygon.sumOf { it.latitude }
        val lngSum = polygon.sumOf { it.longitude }
        return LatLng(latSum / polygon.size, lngSum / polygon.size)
    }

    private fun updateMapMarker(lat: Double, lng: Double) {
        val speciesId = binding.speciesID.text.toString().trim()
        val location = LatLng(lat, lng)

        // marker pin
        val drawable = ContextCompat.getDrawable(this, R.drawable.marker_pin)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        userMarker?.remove()
        userMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .title(speciesId)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        )

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun saveCoordinatesProcess() {
        val latText = binding.latitude.text.toString().trim()
        val lngText = binding.longitude.text.toString().trim()
        val speciesId = binding.speciesID.text.toString().trim()

        if (speciesId.isEmpty()) {
            binding.saveCoordinates.isEnabled = true
            showErrorDialog("Please enter a Species ID.")
            return
        }

        if (speciesId.startsWith("NMS", ignoreCase = true)) {
            showErrorDialog("Non-Mangrove Species cannot have a location.")
            return
        }

        if (!isValidDecimalFormat(latText) || !isValidDecimalFormat(lngText)) {
            binding.saveCoordinates.isEnabled = true
            showErrorDialog("Coordinates must have 6 decimal places.")
            return
        }

        val lat = latText.toDoubleOrNull()
        val lng = lngText.toDoubleOrNull()

        if (!isValidCoordinate(lat, lng)) {
            binding.saveCoordinates.isEnabled = true
            showErrorDialog("Invalid coordinates! Latitude (-90 to 90), Longitude (-180 to 180).")
            return
        }

        if (!isWithinCabusaoWetlands(lat!!, lng!!)) {
            binding.saveCoordinates.isEnabled = true
            showErrorDialog("Coordinates must be inside Cabusao Wetlands boundary.")
            return
        }

        //  if species ID matches the correct zone
        val pointZone = getZoneByBoundary(lat, lng)

        if (pointZone == null) {
            binding.saveCoordinates.isEnabled = true
            showErrorDialog("Coordinates are not inside any defined zone.")
            return
        }


        // if species exists in Mangrove Information
        mangroveInfoDb.child(speciesId).get().addOnSuccessListener { speciesSnapshot ->
            if (!speciesSnapshot.exists()) {
                binding.saveCoordinates.isEnabled = true
                showErrorDialog("Species ID $speciesId does not exist in Mangrove Information.")
                return@addOnSuccessListener
            }

            // if same lat/lng exists for species
            database.get().addOnSuccessListener { snapshot ->
                var duplicateFound = false
                var duplicateSpeciesId: String? = null

                for (species in snapshot.children) {
                    val savedLat = species.child("latitude").getValue(Double::class.java)
                    val savedLng = species.child("longitude").getValue(Double::class.java)
                    if (savedLat == lat && savedLng == lng) {
                        duplicateFound = true
                        duplicateSpeciesId = species.key
                        break
                    }
                }

                if (duplicateFound) {
                    binding.saveCoordinates.isEnabled = true
                    showErrorDialog("Coordinates already exist under Species ID: $duplicateSpeciesId.")
                    return@addOnSuccessListener
                }

                // if this species already has coordinates
                database.child(speciesId).get().addOnSuccessListener { existingSnapshot ->
                    if (existingSnapshot.exists()) {
                        binding.saveCoordinates.isEnabled = true
                        showErrorDialog("Species ID $speciesId already has saved coordinates.")
                        return@addOnSuccessListener
                    }

                    // save coordinates if unique and not yet assigned
                    saveCoordinatesToFirebase(speciesId, lat, lng)
                    updateMapMarker(lat, lng)

                }.addOnFailureListener {
                    binding.saveCoordinates.isEnabled = true
                    showErrorDialog("Database error: ${it.message}")
                }

            }.addOnFailureListener {
                binding.saveCoordinates.isEnabled = true
                showErrorDialog("Database error: ${it.message}")
            }

        }.addOnFailureListener {
            binding.saveCoordinates.isEnabled = true
            showErrorDialog("Database error: ${it.message}")
        }
    }

    private fun saveCoordinatesToFirebase(speciesId: String, lat: Double, lng: Double) {
        val coordinates = mapOf("latitude" to lat, "longitude" to lng)

        database.child(speciesId).setValue(coordinates)
            .addOnSuccessListener {
                binding.latitude.clearFocus()
                binding.longitude.clearFocus()
                binding.saveCoordinates.isEnabled = true
                showSuccessDialog("Coordinates saved for $speciesId!")
            }
            .addOnFailureListener {
                binding.saveCoordinates.isEnabled = true
                showErrorDialog("Failed to save coordinates")
            }
    }

    private fun isValidCoordinate(lat: Double?, lng: Double?): Boolean {
        return lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0
    }

    private fun isValidDecimalFormat(value: String): Boolean {
        return value.matches(Regex("^-?\\d{1,3}\\.\\d{6}$"))
    }

    private fun isPointInPolygon(lat: Double, lng: Double, polygon: List<LatLng>): Boolean {
        var inside = false
        val n = polygon.size
        var j = n - 1

        for (i in 0 until n) {
            val xi = polygon[i].longitude  // X = longitude
            val yi = polygon[i].latitude   // Y = latitude
            val xj = polygon[j].longitude
            val yj = polygon[j].latitude

            val intersect = ((yi > lat) != (yj > lat)) &&
                    (lng < (xj - xi) * (lat - yi) / (yj - yi) + xi)

            if (intersect) inside = !inside
            j = i
        }
        return inside
    }

    private fun isWithinCabusaoWetlands(lat: Double, lng: Double): Boolean {
        return isPointInPolygon(lat, lng, cabusaoWetlands)
    }

    private fun showSuccessDialog(message: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val layout = LayoutInflater.from(this).inflate(R.layout.acc_success_dialog, null)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = layout.findViewById<TextView>(R.id.title)
        val text = layout.findViewById<TextView>(R.id.text)
        val ok = layout.findViewById<TextView>(R.id.ok)
        title.text = "Map Successful"
        text.text = message

        ok.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val layout = LayoutInflater.from(this).inflate(R.layout.acc_failed_dialog, null)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        layout.findViewById<TextView>(R.id.title).text = "Map Failed"
        layout.findViewById<TextView>(R.id.text).text = message
        layout.findViewById<TextView>(R.id.ok).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}