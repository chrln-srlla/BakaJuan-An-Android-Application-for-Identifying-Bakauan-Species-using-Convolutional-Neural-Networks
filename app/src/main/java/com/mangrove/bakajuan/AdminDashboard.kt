package com.mangrove.bakajuan

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mangrove.bakajuan.databinding.AdminDashboardBinding
import com.mangrove.bakajuan.databinding.GeomapInformationAdminBinding

class AdminDashboard : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: AdminDashboardBinding
    private lateinit var infoBinding: GeomapInformationAdminBinding
    private lateinit var speciesListContainer: LinearLayout
    private lateinit var mangroveMapAdmin: GoogleMap
    private lateinit var database: DatabaseReference
    private var isExpanded = false
    private var screenWidthDp: Int = 0

    private var pendingZoomSpeciesID: String? = null

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
        binding = AdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Mangrove Information")

        speciesListContainer = binding.speciesListContainer
        loadSpeciesList()

        // screen adjustments
        val metrics = resources.displayMetrics
        val screenDpWidth = metrics.widthPixels / metrics.density

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        if (screenDpWidth <= 360) {
            binding.welcomeIcon.layoutParams.width = 65.dpToPx(this)
            binding.welcomeIcon.layoutParams.height = 65.dpToPx(this)
            binding.welcomeIcon.requestLayout()

            binding.adminTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 29f)
            binding.descriptionSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }

        // map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        infoBinding = GeomapInformationAdminBinding.inflate(
            LayoutInflater.from(this),
            binding.catalogOverlay,
            true
        )
        binding.catalogOverlay.visibility = View.GONE

        binding.catalog.setOnClickListener {
            startActivity(Intent(this, AdminCatalog::class.java))
        }

        infoBinding.viewCatalog.setOnClickListener {
            startActivity(Intent(this, AdminCatalog::class.java))
        }

        val zoomSpeciesID = intent.getStringExtra("zoomSpeciesID")
        val focusMap = intent.getBooleanExtra("focusMap", false)

        if (focusMap) {
            val scrollView = findViewById<ScrollView>(R.id.scrollviewAdmin)
            scrollView.post {
                ObjectAnimator.ofInt(
                    scrollView,
                    "scrollY",
                    scrollView.scrollY,
                    scrollView.getChildAt(0).height
                ).setDuration(800).start()
            }
        }

        if (!zoomSpeciesID.isNullOrEmpty()) pendingZoomSpeciesID = zoomSpeciesID

        binding.logOut.setOnClickListener {
            showLogoutDialog()
        }

        binding.logMangrove.setOnClickListener {
            startActivity(Intent(this, AdminMangrove::class.java))
            noTransition() }
        binding.plotCoordinates.setOnClickListener {
            startActivity(Intent(this, AdminMap::class.java))
            noTransition() }
        binding.viewCatalog.setOnClickListener {
            startActivity(Intent(this, AdminCatalog::class.java))
            noTransition() }
        binding.accountSettings.setOnClickListener {
            startActivity(Intent(this, AccountSettings::class.java))
            noTransition() }

        // screen adjustments
        if (screenDpWidth <= 360) {
            binding.logSpecies.textSize = 9f
            binding.mapSpecies.textSize = 9f
            binding.catalogSpecies.textSize = 9f
            binding.accAdmin.textSize = 9f
        }

        loadDashboardData()

        val showDialog = intent.getBooleanExtra("showSuccessDialog", false)
        if (showDialog) showDialogSuccess("You have successfully logged in as Contributor")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mangroveMapAdmin = googleMap
        mangroveMapAdmin.mapType = GoogleMap.MAP_TYPE_SATELLITE

        // Cabusao Polygon
        val polygonPoints = cabusaoWetlands + cabusaoWetlands.first()
        mangroveMapAdmin.addPolygon(
            PolygonOptions()
                .addAll(polygonPoints)
                .strokeColor(Color.GREEN)
                .fillColor(Color.TRANSPARENT)
                .strokeWidth(3f)
        )

        // Landward
        mangroveMapAdmin.addPolygon(
            PolygonOptions()
                .addAll(landwardZone)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(100, 144, 238, 144))
                .strokeWidth(5f)
        )
        addZoneTextOutside(landwardZone, "LANDWARD ZONE", Color.WHITE)

        // Midward
        mangroveMapAdmin.addPolygon(
            PolygonOptions()
                .addAll(midwardZone)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(100, 255, 182, 193))
                .strokeWidth(5f)
        )
        addZoneTextOutside(midwardZone, "MIDWARD ZONE", Color.WHITE)

      // Seaward
        mangroveMapAdmin.addPolygon(
            PolygonOptions()
                .addAll(seawardZone)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(100, 173, 216, 230))
                .strokeWidth(5f)
        )
        addZoneTextOutside(seawardZone, "SEAWARD ZONE", Color.WHITE)

        // Planting Zone
        mangroveMapAdmin.addPolygon(
            PolygonOptions()
                .addAll(plantedZone1)
                .strokeColor(Color.YELLOW)
                .fillColor(Color.argb(100, 173, 216, 230))
                .strokeWidth(5f)
        )
        addZoneTextInside(plantedZone1, "PLANTING ZONE", 12f, Color.BLACK)

        mangroveMapAdmin.addPolygon(
            PolygonOptions()
                .addAll(plantedZone2)
                .strokeColor(Color.BLUE)
                .fillColor(Color.argb(100, 173, 216, 230))
                .strokeWidth(5f)
        )
        addZoneTextInside(plantedZone2, "PLANTING ZONE", 12f, Color.BLACK)

        mangroveMapAdmin.addPolygon(
            PolygonOptions()
                .addAll(plantedZone3)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(100, 173, 216, 230))
                .strokeWidth(5f)
        )
        addZoneTextInside(plantedZone3, "PLANTING ZONE", 12f, Color.BLACK)

        val boundsBuilder = LatLngBounds.builder()
        for (p in polygonPoints) boundsBuilder.include(p)
        val bounds = boundsBuilder.build()

        val mapView = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).view

        mapView?.post {
            mangroveMapAdmin.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
        }

        val center = LatLng(
            cabusaoWetlands.map { it.latitude }.average(),
            cabusaoWetlands.map { it.longitude }.average()
        )

        loadSpeciesMarkers()

        pendingZoomSpeciesID?.let {
            zoomToSpecies(it)
            pendingZoomSpeciesID = null
        }

        mangroveMapAdmin.setOnMapClickListener {
            binding.catalogOverlay.visibility = View.GONE
        }

        addWetlandsMarker()
        setupMapToggle()
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)
    }

    private fun noTransition() {
        overridePendingTransition(0, 0)
    }

    private fun setupMapToggle() {
        val mapCard = findViewById<CardView>(R.id.mapCard)
        val toggleButton = findViewById<ImageButton>(R.id.btnToggleMapSize)

        val defaultHeight = resources.getDimensionPixelSize(R.dimen.default_map_height)
        val expandedHeight = resources.getDimensionPixelSize(R.dimen.expanded_map_height)

        toggleButton.setOnClickListener {
            isExpanded = !isExpanded

            val newHeight = if (isExpanded) expandedHeight else defaultHeight

            val anim = ValueAnimator.ofInt(mapCard.height, newHeight)
            anim.addUpdateListener {
                mapCard.layoutParams.height = it.animatedValue as Int
                mapCard.requestLayout()
            }
            anim.duration = 300
            anim.start()

            toggleButton.animate()
                .alpha(0f)
                .setDuration(100)
                .withEndAction {
                    toggleButton.setImageResource(
                        if (isExpanded) R.drawable.expand_less else R.drawable.expand_more
                    )
                    toggleButton.animate().alpha(1f).setDuration(150).start()
                }.start()
        }
    }

    private fun loadSpeciesList() {
        val speciesRef = FirebaseDatabase.getInstance().getReference("Mangrove Information")
        speciesRef.get().addOnSuccessListener { snapshot ->
            speciesListContainer.removeAllViews()

            for (child in snapshot.children) {
                val speciesID = child.child("speciesID").getValue(String::class.java) ?: child.key ?: ""
                val localName = child.child("localName").getValue(String::class.java) ?: ""
                val imageUrl = child.child("mangroveImage").getValue(String::class.java) ?: ""

                val species = Species(
                    speciesID = speciesID,
                    localName = localName,
                    mangroveImage = imageUrl
                )
                addSpeciesCard(species)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load species list", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addSpeciesCard(species: Species) {
        if (species.speciesID.startsWith("NMS", ignoreCase = true)) {
            return
        }

        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.species_card, speciesListContainer, false)

        val imageView = cardView.findViewById<ImageView>(R.id.mangroveImage)
        val speciesIDView = cardView.findViewById<TextView>(R.id.speciesID)
        val localNameView = cardView.findViewById<TextView>(R.id.localName)

        speciesIDView.text = species.speciesID
        localNameView.text = species.localName

        if (species.mangroveImage.isNotEmpty()) {
            Glide.with(this)
                .load(species.mangroveImage)
                .placeholder(R.drawable.unround)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.unround)
        }

        speciesListContainer.addView(cardView)
    }

    private fun zoomToSpecies(speciesID: String) {
        val coordRef = FirebaseDatabase.getInstance().getReference("Species Coordinates")
        coordRef.child(speciesID).get().addOnSuccessListener { snapshot ->
            val lat = snapshot.child("latitude").getValue(Double::class.java)
            val lng = snapshot.child("longitude").getValue(Double::class.java)
            if (lat != null && lng != null && ::mangroveMapAdmin.isInitialized) {
                val location = LatLng(lat, lng)
                mangroveMapAdmin.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
            } else {
                Toast.makeText(this, "Coordinates for $speciesID not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch coordinates", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSpeciesMarkers() {
        val coordRef = FirebaseDatabase.getInstance().getReference("Species Coordinates")
        val infoRef = FirebaseDatabase.getInstance().getReference("Mangrove Information")

        coordRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(coordSnapshot: DataSnapshot) {
                infoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(infoSnapshot: DataSnapshot) {
                        for (speciesCoord in coordSnapshot.children) {
                            val speciesId = speciesCoord.key ?: continue
                            val lat = speciesCoord.child("latitude").getValue(Double::class.java)
                            val lng = speciesCoord.child("longitude").getValue(Double::class.java)
                            if (lat != null && lng != null) {
                                val location = LatLng(lat, lng)
                                // skip if outside polygon
                                if (!isPointInPolygon(location, cabusaoWetlands)) continue

                                val speciesSnapshot = infoSnapshot.child(speciesId)
                                val speciesName = speciesSnapshot.child("localName").getValue(String::class.java) ?: "Unknown"
                                val speciesZone = speciesSnapshot.child("zone").getValue(String::class.java) ?: "Unknown"
                                val speciesImage = speciesSnapshot.child("mangroveImage").getValue(String::class.java) ?: ""

                                val marker = mangroveMapAdmin.addMarker(
                                    MarkerOptions()
                                        .position(location)
                                        .title(speciesName)
                                        .snippet("Lat: ${location.latitude}, Lng: ${location.longitude}")
                                        .icon(speciesID(speciesId))
                                )

                                val markerData = GeomapSpeciesData(
                                    speciesID = speciesId,
                                    localName = speciesName,
                                    mangroveZone = speciesZone,
                                    mangroveImage = speciesImage,
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                                marker?.tag = markerData
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        mangroveMapAdmin.setOnMarkerClickListener { clickedMarker ->
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
            }

            clickedMarker.showInfoWindow()
            false
        }
    }

    private fun isPointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
        var intersects = 0
        val x = point.longitude
        val y = point.latitude
        for (i in polygon.indices) {
            val j = (i + 1) % polygon.size
            val xi = polygon[i].longitude
            val yi = polygon[i].latitude
            val xj = polygon[j].longitude
            val yj = polygon[j].latitude

            val cond1 = (yi > y) != (yj > y)
            val slope = if (yj - yi != 0.0) (xj - xi) / (yj - yi) else Double.POSITIVE_INFINITY
            val xIntersect = xi + (y - yi) * slope
            if (cond1 && x < xIntersect) intersects++
        }
        return intersects % 2 == 1
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

        mangroveMapAdmin.addMarker(
            MarkerOptions()
                .position(polygonCenter)
                .icon(icon)
                .anchor(0.5f, 1f)
        )
    }

    private fun addZoneTextOutside(polygon: List<LatLng>, text: String, color: Int) {
        val center = getPolygonCenter(polygon)
        val offset = 0.001

        val position = LatLng(center.latitude, center.longitude + offset)
        val icon = zone(text, color)

        mangroveMapAdmin.addMarker(
            MarkerOptions()
                .position(position)
                .icon(icon)
                .anchor(0.5f, 0.5f)
        )
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

        mangroveMapAdmin.addMarker(
            MarkerOptions()
                .position(center)
                .icon(icon)
                .anchor(0.5f, 0.5f)
        )
    }

    private fun loadDashboardData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var speciesCount = 0
                var totalCarbon = 0.0
                val zonesSet = mutableSetOf<String>()

                for (speciesSnapshot in snapshot.children) {

                    val speciesId = speciesSnapshot.child("speciesID").getValue(String::class.java)?.trim() ?: ""

                    // skip Non-Mangrove Species (NMS)
                    if (speciesId.startsWith("NMS", ignoreCase = true)) {
                        continue
                    }

                    // count only real mangroves
                    speciesCount++

                    // carbon
                    val carbon = speciesSnapshot.child("estimatedCarbonSequestered").getValue(String::class.java)
                    carbon?.let {
                        totalCarbon += it.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0
                    }

                    // zones
                    speciesSnapshot.child("zone").getValue(String::class.java)?.let { zonesSet.add(it) }
                }

                binding.totalSpecies.text = speciesCount.toString()
                binding.totalCarbonSequestered.text = String.format("%.2f", totalCarbon)

                binding.zonesMapped.text = zonesSet.size.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminDashboard, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.logout_dialog, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val yesLogout = dialogView.findViewById<AppCompatButton>(R.id.yesLogout)
        val noLogout = dialogView.findViewById<AppCompatButton>(R.id.noLogout)

        yesLogout.setOnClickListener {
            try {
                val prefs = getSharedPreferences("BakajuanPrefs", MODE_PRIVATE)
                prefs.edit().clear().apply()

                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, AdminUser::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                alertDialog.dismiss()
            } catch (e: Exception) {
                alertDialog.dismiss()
                showErrorDialog("Logout Failed: ${e.message}")
            }
        }

        noLogout.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }

    private fun showDialogSuccess(message: String) {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val layout = LayoutInflater.from(this).inflate(R.layout.success_dialog, null)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val titleText: TextView = layout.findViewById(R.id.title)
        val messageText: TextView = layout.findViewById(R.id.text)
        titleText.text = "Login Success"
        messageText.text = message

        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, 2000)
    }

    private fun showErrorDialog(message: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val layout = LayoutInflater.from(this).inflate(R.layout.acc_failed_dialog, null)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        layout.findViewById<TextView>(R.id.title).text = "Logout Failed"
        layout.findViewById<TextView>(R.id.text).text = message
        layout.findViewById<TextView>(R.id.ok).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}