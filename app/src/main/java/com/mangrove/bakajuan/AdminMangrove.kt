package com.mangrove.bakajuan

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.database.*
import com.mangrove.bakajuan.databinding.AdminMangroveBinding
import java.io.File
import kotlin.math.pow

class AdminMangrove : AppCompatActivity() {

    private lateinit var binding: AdminMangroveBinding
    private lateinit var MangroveInformation: DatabaseReference
    private var imageUri: Uri? = null
    private var carbon: String = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.mangroveImage.setImageURI(it)
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && imageUri != null) {
            binding.mangroveImage.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminMangroveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MangroveInformation = FirebaseDatabase.getInstance().getReference("Mangrove Information")

        val screenDpWidth = resources.displayMetrics.widthPixels / resources.displayMetrics.density
        if (screenDpWidth <= 360) {
            listOf(
                binding.logSpecies, binding.mapSpecies, binding.catalogSpecies, binding.accAdmin
            ).forEach { it.textSize = 9f }
        }

        binding.speciesID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val speciesId = s.toString().trim()
                when {
                    speciesId.startsWith("BB") -> {
                        binding.localName.setText("Bakhaw Babae")
                        binding.scientificName.setText("Rhizophora mucronata")
                        binding.mangroveZone.setText("Mid-zone")
                        binding.mangroveDescription.setText(
                            "Upper surface and under surface are smooth; Upper surface is dark green; under surface is yellow green; stipules light green; Blades are elliptic, with mucronate apex; 11-19 cm long, 6-10 cm wide."
                        )
                    }
                    speciesId.startsWith("BTB") -> {
                        binding.localName.setText("Buta-buta")
                        binding.scientificName.setText("Excoecaria agallocha")
                        binding.mangroveZone.setText("Landward-zone")
                        binding.mangroveDescription.setText(
                            "Uppersurface is smooth; undersurface is smooth; Uppersurface is green; undersurface is light green; Blades are elliptic, with acute apex; 3-8 cm long, 2-4 cm wide."
                        )
                    }
                    speciesId.startsWith("MP") -> {
                        binding.localName.setText("Miyapi")
                        binding.scientificName.setText("Avicennia rhumpiana")
                        binding.mangroveZone.setText("Seaward-zone")
                        binding.mangroveDescription.setText(
                            "Upper surface and under surface are smooth; Upper surface is dark green; under surface is light green; Blades are obovate to round, with rounded apex; 6-12 cm long, 3-11 cm wide."
                        )
                    }
                    speciesId.startsWith("PG") -> {
                        binding.localName.setText("Pagatpat")
                        binding.scientificName.setText("Sonneratia alba")
                        binding.mangroveZone.setText("Seaward-zone")
                        binding.mangroveDescription.setText(
                            "Uppersurface is smooth; undersurface is smooth; Uppersurface is green; undersurface is light green; Blades are elliptic, with acute apex; 3-8 cm long, 2-4 cm wide."
                        )
                    }
                    speciesId.startsWith("PT") -> {
                        binding.localName.setText("Pototan")
                        binding.scientificName.setText("Bruguiera cylindrica")
                        binding.mangroveZone.setText("Mid-zone")
                        binding.mangroveDescription.setText(
                            "Upper surface is shiny; under surface is smooth; Upper surface is green; under surface is light green; stipules green; Blades are elliptic; with acute to acuminate apex; 6-17 cm long, 2-8 cm wide."
                        )
                    }
                    speciesId.startsWith("BL") -> {
                        binding.localName.setText("Bakhaw Lalaki")
                        binding.scientificName.setText("Rhizophora apiculata")
                        binding.mangroveZone.setText("Mid-zone")
                        binding.mangroveDescription.setText(
                            "Upper surface is shiny; under surface is smooth; Upper surface is green; Under surface is light green; stipules green; Blades are elliptic; with acute to acuminate apex; 6-17 cm long, 2-8 cm wide."
                        )
                    }
                    speciesId.startsWith("BKB") -> {
                        binding.localName.setText("Bakawan Bato")
                        binding.scientificName.setText("Rhizophora stylosa")
                        binding.mangroveZone.setText("Mid-zone")
                        binding.mangroveDescription.setText(
                            "Upper surface is waxy, light green; Under surface is smooth, yellow green; 11 cm long, 5cm wide; Leaves point upward, sides curling, stipless light green; Blades are elliptic."
                        )
                    }
                    speciesId.startsWith("BG") -> {
                        binding.localName.setText("Bungalon")
                        binding.scientificName.setText("Avicinnia marina")
                        binding.mangroveZone.setText("Seaward-zone")
                        binding.mangroveDescription.setText(
                            "Smooth light-grey bark made up of thin, stiff, brittle flakes; Leaves are thick, 5-8 cm long, a bright, glossy green on the upper surface, and silvery-white, or grey, with very small matted hairs on the surface below."
                        )
                    }
                    speciesId.startsWith("BS") -> {
                        binding.localName.setText("Busain")
                        binding.scientificName.setText("Bruguiera gymnorrhiza")
                        binding.mangroveZone.setText("Mid-zone")
                        binding.mangroveDescription.setText(
                            "Upper surface is shiny; under surface is smooth, light green; Blades are elliptic; Slightly to very rough, light brown to greyish. few lenticels."
                        )
                    }
                    speciesId.startsWith("LG") -> {
                        binding.localName.setText("Langarai")
                        binding.scientificName.setText("Bruguiera parviflora")
                        binding.mangroveZone.setText("Mid-zone")
                        binding.mangroveDescription.setText(
                            "Upper surface is smooth light green; Under surface is waxy, pale green; Whitish to light yellow stiples; 8 cm long, 3 cm wide; Blades are elliptic."
                        )
                    }
                    speciesId.startsWith("AP") -> {
                        binding.localName.setText("Api-api")
                        binding.scientificName.setText("Avicennia officinalis")
                        binding.mangroveZone.setText("Seaward-zone")
                        binding.mangroveDescription.setText(
                            "Upper surface is shiny dark green; Under surface is glabarous, yellow green; with salt crystal; Appear slightly convex as sides bent downwards; Blades are elliptic to oblong."
                        )
                    }
                    speciesId.startsWith("TG") -> {
                        binding.localName.setText("Tangal")
                        binding.scientificName.setText("Ceriops tagal")
                        binding.mangroveZone.setText("Mid-zone")
                        binding.mangroveDescription.setText(
                            "Upper surface is smooth, green to yellow green; Under surface is smooth, light green, leaves directed upward, brittle; Blades are mostly obovate; 8 cm long, 4 cm wide."
                        )
                    }
                    speciesId.startsWith("TB") -> {
                        binding.localName.setText("Tabigi")
                        binding.scientificName.setText("Xylocarpus granatum")
                        binding.mangroveZone.setText("Landward-zone")
                        binding.mangroveDescription.setText(
                            "Simple pinnate, spirally arranged, with 2-4 pairs of leaflets that are light green when young, dark green when old and withering orange red."
                        )
                    }
                    speciesId.startsWith("NP") -> {
                        binding.localName.setText("Nipa")
                        binding.scientificName.setText("Nypa fruticans")
                        binding.mangroveZone.setText("Landward-zone")
                        binding.mangroveDescription.setText(
                            "Horizontal trunk that grows beneath the ground and only leaves and flower stalk grow upwards above the surface; Leaves can extend up to 9 m (30 ft) in height."
                        )
                    }
                    speciesId.startsWith("SG") -> {
                        binding.localName.setText("Saging-saging")
                        binding.scientificName.setText("Aegiceras corniculatum")
                        binding.mangroveZone.setText("Mid-zone")
                        binding.mangroveDescription.setText(
                            "Leaves are alternate alternate, obovate, 30-100 mm long and 15-50 mm wide, entire, leathery and minutely dotted."
                        )
                    }
                    speciesId.startsWith("NL") -> {
                        binding.localName.setText("Nilad")
                        binding.scientificName.setText("Scyphiphora hydrophylacea")
                        binding.mangroveZone.setText("Landward-zone")
                        binding.mangroveDescription.setText(
                            "Leaf blades are broad and drop-shaped. Terminal buds and young leaves: coated with varnish-like substance."
                        )
                    }
                    speciesId.startsWith("PDD") -> {
                        binding.localName.setText("Pedada")
                        binding.scientificName.setText("Sonneratia caseolaris")
                        binding.mangroveZone.setText("Seaward-zone")
                        binding.mangroveDescription.setText(
                            "Flowers with red petals (vs. white), young branches that hang down like those of the weeping pillow; Fruit is persimmon-like wit hsepals whose tips bend away from the stalk. Found near the banks of tidal rivers in brackish water."
                        )
                    }
                    speciesId.startsWith("DL") -> {
                        binding.localName.setText("Dungon Late")
                        binding.scientificName.setText("Heritiera littoralis")
                        binding.mangroveZone.setText("Landward-zone")
                        binding.mangroveDescription.setText(
                            "The looking-glass mangrove is a large tree with wing shaped nuts, which is most easily recognised by the silvery scales on the underside of its leaves, which therefore appear green from top and white from below."
                        )
                    }
                    speciesId.startsWith("DN") -> {
                        binding.localName.setText("Dungon")
                        binding.scientificName.setText("Heritiera sylvatico Vid")
                        binding.mangroveZone.setText("Landward-zone")
                        binding.mangroveDescription.setText(
                            "Simple alternate oblong-elliptic to ovate, 15 x 4-6 cm base; Base subacute to rounded; Apex acute to acuminatete; Glossy green above, greyish scaly beneath, with about 7 pairs of lateral neves; Petiole 2 cm long."
                        )
                    }
                    speciesId.startsWith("BN") -> {
                        binding.localName.setText("Bani")
                        binding.scientificName.setText("Pongamia pinnata")
                        binding.mangroveZone.setText("Mid-zone")
                        binding.mangroveDescription.setText(
                            "Alternate, imparipinnate with long slender leafstalk, hairless, pinkish-red when young, glossy dark green above and dull green with prominent veins beneath when mature."
                        )
                    }
                    speciesId.startsWith("TI") -> {
                        binding.localName.setText("Tui")
                        binding.scientificName.setText("Dolichandron espathaeae")
                        binding.mangroveZone.setText("Landward-zone")
                        binding.mangroveDescription.setText(
                            "Compound leaves made up of 2-4 pairs of leaflets eye-shaped (6-20 cm long) this arranged opposite one another; Bark is grey to dark brown, shallowly ridged and fissured, slighty scaly."
                        )
                    }
                    speciesId.startsWith("GP") -> {
                        binding.localName.setText("Gapas-gapas")
                        binding.scientificName.setText("Camptostemon philippinense")
                        binding.mangroveZone.setText("Mid-zone")
                        binding.mangroveDescription.setText(
                            "6-5 by 2-4 cm leaves are elliptic lanceolate, scaly on both sides, have a rounded point and a narrow base."
                        )
                    }
                    speciesId.startsWith("NMS") -> {
                        binding.localName.setText("Non-Mangrove")
                        binding.scientificName.setText("Unknown")
                        binding.mangroveZone.setText("Not Applicable")
                        binding.mangroveDescription.setText(
                            "This is a distractor leaf, not a mangrove species."
                        )
                    }
                    else -> {
                        binding.localName.setText("")
                        binding.scientificName.setText("")
                        binding.mangroveZone.setText("")
                        binding.mangroveDescription.setText("")
                    }
                }
                val isNonMangrove = speciesId.startsWith("NMS", ignoreCase = true)

                binding.mangroveZone.isEnabled = !isNonMangrove
                binding.circumference.isEnabled = !isNonMangrove
                binding.height.isEnabled = !isNonMangrove
                binding.estimatedAge.isEnabled = !isNonMangrove
                binding.carbonSequestered.isEnabled = !isNonMangrove

                if (isNonMangrove) {
                    binding.mangroveZone.setText("Not Applicable")
                    binding.circumference.setText("")
                    binding.height.setText("")
                    binding.estimatedAge.setText("")
                    binding.carbonSequestered.setText("")
                    carbon = ""
                }
            }
        })

        // carbon & age
        val autoCalcWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val speciesId = binding.speciesID.text.toString().trim()

                // skip calculation if Non-Mangrove Species
                if (speciesId.startsWith("NMS", ignoreCase = true)) {
                    binding.carbonSequestered.setText("")
                    binding.estimatedAge.setText("")
                    carbon = ""
                    return
                }

                val circumference = binding.circumference.text.toString()
                if (circumference.isNotEmpty()) {
                    val cir = circumference.toDoubleOrNull() ?: return

                    val dbh = cir / 3.1416
                    val agb = 0.251 * dbh.pow(2.46)
                    val bgb = 0.4 * agb
                    val tb = agb + bgb
                    val carbonStock = 0.5 * tb
                    val co2Sequestered = carbonStock * 3.67

                    val co2 = String.format("%.2f kg CO₂", co2Sequestered)
                    binding.carbonSequestered.setText(co2)
                    carbon = co2

                    val growthFactor = 0.89
                    val estimatedAge = dbh / growthFactor
                    binding.estimatedAge.setText(String.format("%.2f year/s", estimatedAge))
                } else {
                    binding.carbonSequestered.setText("")
                    binding.estimatedAge.setText("")
                    carbon = ""
                }
            }
        }

        binding.circumference.addTextChangedListener(autoCalcWatcher)

        binding.submitCatalog.setOnClickListener {
            val speciesID = binding.speciesID.text.toString().trim()
            val localName = binding.localName.text.toString().trim()
            val scientificName = binding.scientificName.text.toString().trim()
            val zone = binding.mangroveZone.text.toString().trim()
            val characteristics = binding.mangroveDescription.text.toString().trim()
            val height = binding.height.text.toString().trim()
            val circumference = binding.circumference.text.toString().trim()
            val age = binding.estimatedAge.text.toString().trim()

            val speciesIdPattern = "^(BB|BTB|MP|PG|PT|BL|BKB|BG|BS|LG|AP|TG|TB|NP|SG|NL|PDD|DL|DN|BN|TI|GP|NMS)\\d+$".toRegex()
            val numberOnlyPattern = "^\\d+$".toRegex()

            val isNonMangrove = speciesID.startsWith("NMS", ignoreCase = true)

            if (speciesID.isEmpty()) {
                showErrorDialog("Input species ID.")
                return@setOnClickListener
            }

            // numeric fields validation (only if not NMS)
            if (!isNonMangrove) {
                if (circumference.isEmpty() || !numberOnlyPattern.matches(circumference)) {
                    showErrorDialog("Invalid Circumference.")
                    return@setOnClickListener
                }

                if (height.isNotEmpty() && !numberOnlyPattern.matches(height)) {
                    showErrorDialog("Invalid Height.")
                    return@setOnClickListener
                }
            }

            if (localName.isEmpty() || zone.isEmpty() || scientificName.isEmpty()
                || characteristics.isEmpty() || (!isNonMangrove && circumference.isEmpty())
                || imageUri == null
            ) {
                showErrorDialog("Input species data to all fields and upload mangrove image.")
                return@setOnClickListener
            }

            if (!speciesIdPattern.matches(speciesID)) {
                showErrorDialog("Species ID must be BB, BTB, MP, PG, PT, etc. (e.g., BB07, MP100).")
                return@setOnClickListener
            }

            val allowedScientificNames = listOf(
                "Rhizophora mucronata",       // BB
                "Excoecaria agallocha",       // BTB
                "Avicennia rhumpiana",        // MP
                "Sonneratia alba",            // PG
                "Bruguiera cylindrica",       // PT
                "Rhizophora apiculata",       // BL
                "Rhizophora stylosa",         // BKB
                "Avicinnia marina",           // BG
                "Bruguiera gymnorrhiza",      // BS
                "Bruguiera parviflora",       // LG
                "Avicennia officinalis",      // AP
                "Ceriops tagal",              // TG
                "Xylocarpus granatum",        // TB
                "Nypa fruticans",             // NP
                "Aegiceras corniculatum",     // SG
                "Scyphiphora hydrophylacea",  // NL
                "Sonneratia caseolaris",      // PDD
                "Heritiera littoralis",       // DL
                "Heritiera sylvatico Vid",    // DN
                "Pongamia pinnata",           // BN
                "Dolichandron espathaeae",    // T
                "Camptostemon philippinense", // GP
                "Unknown"                     // NMS
            )

            if (!isNonMangrove && !allowedScientificNames.contains(scientificName)) {
                showErrorDialog("Invalid Scientific Name.")
                return@setOnClickListener
            }

            val allowedZones = listOf("Landward-zone", "Midward-Zone", "Mid-zone", "Seaward-zone", "Not Applicable")
            if (!allowedZones.any { it.equals(zone, ignoreCase = true) }) {
                showErrorDialog("Mangrove Zone must be either Landward-zone, Midward-zone, or Seaward-zone.")
                return@setOnClickListener
            }

            MangroveInformation.child(speciesID).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        showErrorDialog("Species ID already exists!")
                    } else {
                        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                            .getReference("mangrove_image/${speciesID}.jpg")

                        // upload image and data
                        imageUri?.let { uri ->
                            val inputStream = contentResolver.openInputStream(uri)
                            storageRef.putStream(inputStream!!)
                                .addOnSuccessListener {
                                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                        val data = BakajuanSpeciesData(
                                            speciesID = speciesID,
                                            localName = localName,
                                            scientificName = scientificName,
                                            zone = zone,
                                            characteristics = characteristics,
                                            height = height,
                                            circumference = circumference,
                                            estimatedCarbonSequestered = carbon,
                                            estimatedAge = age,
                                            mangroveImage = downloadUrl.toString(),
                                            dateCatalogued = System.currentTimeMillis()
                                        )
                                        MangroveInformation.child(speciesID).setValue(data)
                                            .addOnSuccessListener {
                                                if (isNonMangrove) {
                                                    showSuccessDialog("$speciesID Submitted to Catalog")
                                                } else {
                                                    showSuccessDialog("Species $speciesID Submitted to Catalog")
                                                }
                                            }
                                            .addOnFailureListener {
                                                showErrorDialog("Failed to Submit to Catalog")
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    showErrorDialog("Failed to upload image: ${e.message}")
                                    e.printStackTrace()
                                }
                        } ?: showErrorDialog("No image selected")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showErrorDialog("Database error: ${error.message}")
                }
            })
        }

        binding.backToDashboard.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
        }

        binding.uploadMangrove.setOnClickListener { pickImageLauncher.launch("image/*") }

        binding.captureMangrove.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.pickLocation.setOnClickListener {
            val speciesID = binding.speciesID.text.toString().trim()

            if (speciesID.startsWith("NMS", ignoreCase = true)) {
                showErrorDialog("Non-Mangrove Species cannot have a location.")
                return@setOnClickListener
            }

            if (speciesID.isEmpty() || imageUri == null) {
                showErrorDialog("Input all fields and upload mangrove image before selecting location.")
                return@setOnClickListener
            }

            val intent = Intent(this, AdminMap::class.java)
            intent.putExtra("speciesId", speciesID)
            startActivity(intent)
        }

        binding.plotCoordinates.setOnClickListener {
            startActivity(Intent(this, AdminMap::class.java))
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

        binding.adminMangrove.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.adminMangrove.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.adminMangrove.rootView.height
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
        title.text = "Catalog Successful"
        text.text = message

        ok.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val layout = LayoutInflater.from(this).inflate(R.layout.acc_failed_dialog, null)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title: TextView = layout.findViewById(R.id.title)
        val text = layout.findViewById<TextView>(R.id.text)
        val ok = layout.findViewById<TextView>(R.id.ok)

        title.text = "Catalog Failed"
        text.text = message
        ok.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun openCamera() {
        try {
            val imageFile = File.createTempFile(
                "IMG_${System.currentTimeMillis()}",
                ".jpg",
                getExternalFilesDir("Pictures")
            )

            imageUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                imageFile
            )
            takePictureLauncher.launch(imageUri)
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorDialog("Failed to open camera: ${e.message}")
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            showErrorDialog("Camera permission is required to capture mangrove image.")
        }
    }
}