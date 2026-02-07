package com.mangrove.bakajuan

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.mangrove.bakajuan.databinding.GeomapBakajuanBinding
import com.mangrove.bakajuan.databinding.GeomapInformationUserBinding

class GeomapBakajuan : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: GeomapBakajuanBinding
    private lateinit var infoBinding: GeomapInformationUserBinding
    private lateinit var mangroveMap: GoogleMap
    private lateinit var database: DatabaseReference
    private var screenWidthDp: Int = 0

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

    // PLANTING ZONES
    private val plantedZone1 = listOf(
        LatLng(13.7563711, 123.0714903),
        LatLng(13.7571494, 123.0718075),
        LatLng(13.7571478, 123.0714769),
        LatLng(13.7569547, 123.0707242)
    )

    private val plantedZone2 = listOf(
        LatLng(13.7577945, 123.0690766),
        LatLng(13.7579489, 123.0698642),
        LatLng(13.7575073, 123.0698340),
        LatLng(13.7576353, 123.0694163)
    )

    private val plantedZone3 = listOf(
        LatLng(13.7578408, 123.0715268),
        LatLng(13.7578271, 123.0712100),
        LatLng(13.7578932, 123.0711114),
        LatLng(13.7579629, 123.0707007),
        LatLng(13.7576913, 123.0706695),
        LatLng(13.7574790, 123.0704623),
        LatLng(13.7576545, 123.0702880),
        LatLng(13.7574539, 123.0698746),
        LatLng(13.7569583, 123.0705482),
        LatLng(13.7572380, 123.0707795),
        LatLng(13.7575015, 123.0709398),
        LatLng(13.7575780, 123.0710856),
        LatLng(13.7573946, 123.0712492),
        LatLng(13.7574819, 123.0714668),
        LatLng(13.7576907, 123.0714551),
        LatLng(13.7578366, 123.0715517)
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
        binding = GeomapBakajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Species Coordinates")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // overlay for species info
        infoBinding = GeomapInformationUserBinding.inflate(LayoutInflater.from(this))
        binding.catalogOverlay.addView(infoBinding.root)
        binding.catalogOverlay.visibility = View.GONE

        binding.navHome.setOnClickListener {
            startActivity(Intent(this, HomeBakajuan::class.java))
            noTransition()
        }
        binding.navCatalog.setOnClickListener {
            startActivity(Intent(this, CatalogBakajuan::class.java))
            noTransition()
        }
        binding.mangroveCaptureUpload.setOnClickListener {
            startActivity(Intent(this, CaptureUploadBakajuan::class.java))
            noTransition()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mangroveMap = googleMap
        mangroveMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mangroveMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

        val polygonPoints = cabusaoWetlands + cabusaoWetlands.first()
        mangroveMap.addPolygon(
            PolygonOptions()
                .addAll(polygonPoints)
                .strokeColor(Color.GREEN)
                .fillColor(Color.TRANSPARENT)
                .strokeWidth(3f)
        )

        // Landward
        mangroveMap.addPolygon(
            PolygonOptions()
                .addAll(landwardZone)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(100, 144, 238, 144))
                .strokeWidth(5f)
        )
        addZoneTextOutside(landwardZone, "LANDWARD ZONE", Color.WHITE)

        // Midward
        mangroveMap.addPolygon(
            PolygonOptions()
                .addAll(midwardZone)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(100, 255, 182, 193))
                .strokeWidth(5f)
        )
        addZoneTextOutside(midwardZone, "MIDWARD ZONE", Color.WHITE)

        // Seaward
        mangroveMap.addPolygon(
            PolygonOptions()
                .addAll(seawardZone)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(100, 173, 216, 230))
                .strokeWidth(5f)

        )
        addZoneTextOutside(seawardZone, "SEAWARD ZONE", Color.WHITE)

        // Planting Zone
        mangroveMap.addPolygon(
            PolygonOptions()
                .addAll(plantedZone1)
                .strokeColor(Color.YELLOW)
                .fillColor(Color.argb(100, 173, 216, 230))
                .strokeWidth(5f)
        )
        addZoneTextInside(plantedZone1, "PLANTING ZONE", 12f, Color.BLACK)

        mangroveMap.addPolygon(
            PolygonOptions()
                .addAll(plantedZone2)
                .strokeColor(Color.BLUE)
                .fillColor(Color.argb(100, 173, 216, 230))
                .strokeWidth(5f)
        )
        addZoneTextInside(plantedZone2, "PLANTING ZONE", 12f, Color.BLACK)

        mangroveMap.addPolygon(
            PolygonOptions()
                .addAll(plantedZone3)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(100, 173, 216, 230))
                .strokeWidth(5f)
        )
        addZoneTextInside(plantedZone3, "PLANTING ZONE", 12f, Color.BLACK)

        // camera only to zones
        val allZones = landwardZone + midwardZone + seawardZone
        val builder = LatLngBounds.Builder()
        allZones.forEach { builder.include(it) }
        val zoneBounds = builder.build()

        mangroveMap.animateCamera(CameraUpdateFactory.newLatLngBounds(zoneBounds, 50))

        // call the functions
        addWetlandsMarker()
        loadSpeciesMarkers()

        // hide overlay when clicking the map
        mangroveMap.setOnMapClickListener {
            binding.catalogOverlay.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)
    }

    private fun noTransition() {
        overridePendingTransition(0, 0)
    }

    private fun addWetlandsMarker() {
        val polygonCenter = LatLng(
            cabusaoWetlands.map { it.latitude }.average(),
            cabusaoWetlands.map { it.longitude }.average()
        )

        val drawable = ContextCompat.getDrawable(this, R.drawable.map_wetlands)!!
        val width = 400
        val height = 400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        val icon = BitmapDescriptorFactory.fromBitmap(bitmap)

        mangroveMap.addMarker(
            MarkerOptions()
                .position(polygonCenter)
                .icon(icon)
                .anchor(0.5f, 1f)
        )
    }

    private fun addZoneTextOutside(
        polygon: List<LatLng>,
        text: String,
        color: Int
    ) {
        val center = getPolygonCenter(polygon)
        val offset = 0.001

        // move text below (south/down)
        val position = LatLng(center.latitude - offset, center.longitude)

        val icon = zone(text, color)

        mangroveMap.addMarker(
            MarkerOptions()
                .position(position)
                .icon(icon)
                .anchor(0.5f, 0.0f)
        )
    }

    private fun getPolygonCenter(points: List<LatLng>): LatLng {
        val lat = points.map { it.latitude }.average()
        val lng = points.map { it.longitude }.average()
        return LatLng(lat, lng)
    }

    private fun zone(text: String, color: Int): BitmapDescriptor {
        val paint = Paint().apply {
            this.color = color
            textSize = if (screenWidthDp <= 360) 20f else 30f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }

        val width = (paint.measureText(text) + 20).toInt()
        val height = 60

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, width / 2f, height - 15f, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun createHorizontalTextIcon(text: String, color: Int, textSize: Float): BitmapDescriptor {
        val paint = Paint().apply {
            this.color = color
            this.textSize = textSize
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }

        val width = (paint.measureText(text) + 20).toInt()
        val height = (textSize + 20).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, width / 2f, height - 10f, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun addZoneTextInside(
        polygon: List<LatLng>,
        text: String,
        textSize: Float = 12f,
        color: Int
    ) {
        val center = getPolygonCenter(polygon)

        val icon = createHorizontalTextIcon(text, color, textSize)

        mangroveMap.addMarker(
            MarkerOptions()
                .position(center)
                .icon(icon)
                .anchor(0.5f, 0.5f)
        )
    }

    private fun loadSpeciesMarkers() {
        val infoRef = FirebaseDatabase.getInstance().getReference("Mangrove Information")
        val coordRef = FirebaseDatabase.getInstance().getReference("Species Coordinates")

        coordRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(coordSnapshot: DataSnapshot) {
                infoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(infoSnapshot: DataSnapshot) {
                        val builder = LatLngBounds.Builder()
                        cabusaoWetlands.forEach { builder.include(it) }

                        var targetMarker: Marker? = null
                        val targetSpeciesID = intent.getStringExtra("speciesID")

                        for (speciesCoord in coordSnapshot.children) {
                            val speciesId = speciesCoord.key ?: continue
                            val lat = speciesCoord.child("latitude").getValue(Double::class.java)
                            val lng = speciesCoord.child("longitude").getValue(Double::class.java)

                            if (lat != null && lng != null) {
                                val location = LatLng(lat, lng)
                                val speciesSnapshot = infoSnapshot.child(speciesId)
                                val speciesName = speciesSnapshot.child("localName").getValue(String::class.java) ?: "Unknown"
                                val speciesImage = speciesSnapshot.child("mangroveImage").getValue(String::class.java) ?: ""
                                val speciesZone = speciesSnapshot.child("zone").getValue(String::class.java) ?: ""

                                val marker = mangroveMap.addMarker(
                                    MarkerOptions()
                                        .position(location)
                                        .title(speciesName)
                                        .snippet("Lat: ${location.latitude}, Lng: ${location.longitude}")
                                        .icon(speciesID(speciesId))
                                )

                                val markerData = GeomapSpeciesData(
                                    speciesID = speciesId,
                                    localName = speciesName,
                                    mangroveImage = speciesImage,
                                    mangroveZone = speciesZone,
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                                marker?.tag = markerData
                                builder.include(location)

                                if (speciesId == targetSpeciesID) {
                                    targetMarker = marker
                                }
                            }
                        }

                        // Default: show all zones
                        val allBounds = builder.build()
                        mangroveMap.animateCamera(CameraUpdateFactory.newLatLngBounds(allBounds, 50))

                        targetMarker?.let {
                            mangroveMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 18f))
                            it.showInfoWindow()
                            binding.catalogOverlay.visibility = View.VISIBLE
                            val data = it.tag as GeomapSpeciesData
                            infoBinding.speciesID.text = data.speciesID
                            infoBinding.localName.text = data.localName
                            infoBinding.mangroveZone.text = data.mangroveZone
                            infoBinding.speciesLatitude.text = data.latitude.toString()
                            infoBinding.speciesLongitude.text = data.longitude.toString()

                            Glide.with(this@GeomapBakajuan)
                                .load(data.mangroveImage)
                                .placeholder(R.drawable.unround)
                                .into(infoBinding.imageBakajuan)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    mangroveMap.setOnMarkerClickListener { clickedMarker ->
            val data = clickedMarker.tag as? GeomapSpeciesData
            data?.let {
                binding.catalogOverlay.visibility = View.VISIBLE
                infoBinding.speciesID.text = it.speciesID
                infoBinding.localName.text = it.localName
                infoBinding.mangroveZone.text = it.mangroveZone
                infoBinding.speciesLatitude.text = it.latitude.toString()
                infoBinding.speciesLongitude.text = it.longitude.toString()

                Glide.with(this)
                    .load(it.mangroveImage)
                    .placeholder(R.drawable.unround)
                    .into(infoBinding.imageBakajuan)

                infoBinding.viewCatalog.setOnClickListener { _ ->
                    val intent = Intent(this, CatalogInformation::class.java)
                    intent.putExtra("speciesID", it.speciesID)
                    intent.putExtra("speciesName", it.localName)
                    intent.putExtra("speciesImage", it.mangroveImage)
                    intent.putExtra("speciesZone", it.mangroveZone)
                    startActivity(intent)
                }
            }

            clickedMarker.showInfoWindow()
            false
        }
    }

    private fun speciesID(speciesId: String): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(this, R.drawable.marker_pin)!!
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight + 40

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, drawable.intrinsicHeight)
        drawable.draw(canvas)

        val montserrat = ResourcesCompat.getFont(this, R.font.montserratalternates_medium_font)
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = if (screenWidthDp <= 360) 20f else 30f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = montserrat
        }

        canvas.drawText(speciesId, width / 2f, height - 10f, paint)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}