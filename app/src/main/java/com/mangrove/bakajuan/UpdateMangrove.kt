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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.mangrove.bakajuan.databinding.AdminUpdMangroveBinding
import java.io.File
import kotlin.math.pow

class UpdateMangrove : AppCompatActivity() {

    private lateinit var binding: AdminUpdMangroveBinding
    private lateinit var databaseReference: DatabaseReference
    private var imageUri: Uri? = null
    private var existingImageUrl: String? = null
    private var carbon: String = ""

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                binding.mangroveImage.setImageURI(it)
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && imageUri != null) {
                binding.mangroveImage.setImageURI(imageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminUpdMangroveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val metrics = resources.displayMetrics
        val screenDpWidth = metrics.widthPixels / metrics.density

        val logSpecies = findViewById<TextView>(R.id.logSpecies)
        val mapSpecies = findViewById<TextView>(R.id.mapSpecies)
        val catalogSpecies = findViewById<TextView>(R.id.catalogSpecies)
        val logoutAdmin = findViewById<TextView>(R.id.accAdmin)

        if (screenDpWidth <= 360) {
            logSpecies.textSize = 9f
            mapSpecies.textSize = 9f
            catalogSpecies.textSize = 9f
            logoutAdmin.textSize = 9f
        }

        // bottom nav visibility handling
        val rootLayout = binding.adminUpdateMangrove
        val bottomNav = binding.bottomNav

        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootLayout.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootLayout.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            bottomNav.visibility =
                if (keypadHeight > screenHeight * 0.15) View.GONE else View.VISIBLE
        }

        // auto calculate carbon and estimated age
        val autoCalcWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val speciesId = binding.speciesID.text.toString().trim()
                val isNonMangrove = speciesId.startsWith("NMS", ignoreCase = true)

                if (isNonMangrove) {
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

        // get speciesID and fetch data
        val speciesID = intent.getStringExtra("speciesID")
        if (speciesID != null) {
            fetchSpeciesData(speciesID)
        } else {
            Toast.makeText(this, "Species ID not found", Toast.LENGTH_SHORT).show()
        }

        // buttons and navigation
        binding.backToDashboard.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
        }

        binding.uploadMangrove.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.captureMangrove.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.updateCatalog.setOnClickListener {
            updateSpeciesData()
        }

        binding.viewCatalog.setOnClickListener {
            startActivity(Intent(this, AdminCatalog::class.java))
        }

        binding.accountSettings.setOnClickListener {
            startActivity(Intent(this, AccountSettings::class.java))
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

    private fun fetchSpeciesData(speciesID: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Mangrove Information")
        databaseReference.child(speciesID).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val species = snapshot.getValue(BakajuanSpeciesData::class.java)
                    species?.let {
                        binding.speciesID.setText(it.speciesID)
                        binding.localName.setText(it.localName)
                        binding.scientificName.setText(it.scientificName)
                        binding.mangroveZone.setText(it.zone)
                        binding.mangroveDescription.setText(it.characteristics)
                        binding.height.setText(it.height)
                        binding.circumference.setText(it.circumference)
                        binding.carbonSequestered.setText(it.estimatedCarbonSequestered)
                        binding.estimatedAge.setText(it.estimatedAge)
                        existingImageUrl = it.mangroveImage
                        if (!existingImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(existingImageUrl)
                                .into(binding.mangroveImage)
                        }

                        val isNonMangrove = speciesID?.startsWith("NMS", ignoreCase = true) == true

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
                } else {
                    Toast.makeText(this, "No data found for ID: $speciesID", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch species data", Toast.LENGTH_SHORT).show()
            }
    }

    // update species data without latitude/longitude
    private fun updateSpeciesData() {
        val speciesID = binding.speciesID.text.toString().trim()

        if (speciesID.isEmpty()) {
            showErrorDialog("Species ID is required to update!")
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.update_dialog, null)
        val updateText = dialogView.findViewById<TextView>(R.id.updateText)
        updateText.text = "Are you sure you want to update Species ID $speciesID?"

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnYes = dialogView.findViewById<AppCompatButton>(R.id.yesUpdate)
        val btnNo = dialogView.findViewById<AppCompatButton>(R.id.noUpdate)

        btnYes.setOnClickListener {
            alertDialog.dismiss()

            val speciesID = binding.speciesID.text.toString().trim()
            val localName = binding.localName.text.toString().trim()
            val scientificName = binding.scientificName.text.toString().trim()
            val zone = binding.mangroveZone.text.toString().trim()
            val characteristics = binding.mangroveDescription.text.toString().trim()
            val height = binding.height.text.toString().trim()
            val circumference = binding.circumference.text.toString().trim()
            val imageRequired = imageUri != null || !existingImageUrl.isNullOrEmpty()

            val speciesIdPattern = "^(BB|BTB|MP|PG|PT|BL|BKB|BG|BS|LG|AP|TG|TB|NP|SG|NL|PDD|DL|DN|BN|TI|GP|NMS)\\d+$".toRegex()
            val numberOnlyPattern = "^\\d+$".toRegex()

            val isNonMangrove = speciesID.startsWith("NMS", ignoreCase = true)

            if (speciesID.isEmpty()) {
                showErrorDialog("Input species ID.")
                return@setOnClickListener
            }

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

            if (localName.isEmpty() || zone.isEmpty() || scientificName.isEmpty() || characteristics.isEmpty() || !imageRequired) {
                showErrorDialog("Input all required fields and upload species image.")
                return@setOnClickListener
            }

            if (!speciesIdPattern.matches(speciesID)) {
                showErrorDialog("Species ID must be BB, BTB, MP, PG, PT, etc. (e.g., BB07, MP100).")
                return@setOnClickListener
            }

            val allowedScientificNames = listOf(
                "Rhizophora mucronata", "Excoecaria agallocha", "Avicennia rhumpiana", "Sonneratia alba",
                "Bruguiera cylindrica", "Rhizophora apiculata", "Rhizophora stylosa", "Avicinnia marina",
                "Bruguiera gymnorrhiza", "Bruguiera parviflora", "Avicennia officinalis", "Ceriops tagal",
                "Xylocarpus granatum", "Nypa fruticans", "Aegiceras corniculatum", "Scyphiphora hydrophylacea",
                "Sonneratia caseolaris", "Heritiera littoralis", "Heritiera sylvatico Vid", "Pongamia pinnata",
                "Dolichandron espathaeae", "Camptostemon philippinense", "Unknown"
            )

            if (!isNonMangrove && !allowedScientificNames.contains(scientificName)) {
                showErrorDialog("Invalid Scientific Name.")
                return@setOnClickListener
            }

            val allowedZones = listOf("Landward-zone", "Midward-zone", "Mid-zone", "Seaward-zone", "Not Applicable")
            if (!allowedZones.any { it.equals(zone, ignoreCase = true) }) {
                showErrorDialog("Mangrove Zone must be either Landward-zone, Midward-zone, or Seaward-zone.")
                return@setOnClickListener
            }

            // passed validation → handle image upload or just save
            if (imageUri != null) {
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("MangroveImage/$speciesID.jpg")

                try {
                    val inputStream = contentResolver.openInputStream(imageUri!!)
                    inputStream?.let { stream ->
                        storageRef.putStream(stream)
                            .addOnSuccessListener {
                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    // Delete old image if different
                                    if (!existingImageUrl.isNullOrEmpty() && existingImageUrl != uri.toString()) {
                                        try {
                                            val oldImageRef = FirebaseStorage.getInstance()
                                                .getReferenceFromUrl(existingImageUrl!!)
                                            oldImageRef.delete()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    saveUpdatedSpecies(speciesID, uri.toString())
                                }
                            }
                            .addOnFailureListener { e ->
                                showErrorDialog("Image upload failed: ${e.message}")
                                e.printStackTrace()
                            }
                    } ?: showErrorDialog("Unable to open image stream")
                } catch (e: Exception) {
                    e.printStackTrace()
                    showErrorDialog("Error reading image: ${e.message}")
                }
            } else {
                saveUpdatedSpecies(speciesID, existingImageUrl)
            }
        }

        btnNo.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun saveUpdatedSpecies(speciesID: String, imageUrl: String?) {
        // fetch existing dateCatalogued from Firebase first
        val ref = FirebaseDatabase.getInstance()
            .getReference("Mangrove Information")
            .child(speciesID)

        ref.get().addOnSuccessListener { snapshot ->
            val existingSpecies = snapshot.getValue(BakajuanSpeciesData::class.java)
            val dateCatalogued = existingSpecies?.dateCatalogued ?: System.currentTimeMillis()

            val updatedSpecies = BakajuanSpeciesData(
                speciesID = speciesID,
                localName = binding.localName.text.toString(),
                scientificName = binding.scientificName.text.toString(),
                zone = binding.mangroveZone.text.toString(),
                characteristics = binding.mangroveDescription.text.toString(),
                height = binding.height.text.toString(),
                circumference = binding.circumference.text.toString(),
                estimatedCarbonSequestered = binding.carbonSequestered.text.toString(),
                estimatedAge = binding.estimatedAge.text.toString(),
                mangroveImage = imageUrl,
                dateCatalogued = System.currentTimeMillis()
            )
            ref.setValue(updatedSpecies)
                .addOnSuccessListener {
                    val isNonMangrove = speciesID.startsWith("NMS", ignoreCase = true)
                    if (isNonMangrove) {
                        showSuccessDialog("$speciesID was successfully updated.")
                    } else {
                        showSuccessDialog("Mangrove $speciesID was successfully updated.")
                    }
                }
                .addOnFailureListener { e ->
                    showErrorDialog("Failed to update species.\nError: ${e.message}")
                }
        }.addOnFailureListener { e ->
            showErrorDialog("Failed to fetch existing data.\nError: ${e.message}")
        }
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
        title.text = "Update Successful"
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
        val text: TextView = layout.findViewById(R.id.text)
        val ok = layout.findViewById<TextView>(R.id.ok)

        title.text = "Update Failed"
        text.text = message
        ok.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}