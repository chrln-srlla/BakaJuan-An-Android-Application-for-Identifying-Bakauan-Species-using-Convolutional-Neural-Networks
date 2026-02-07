package com.mangrove.bakajuan

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.mangrove.bakajuan.databinding.AdminCatalogBinding

class AdminCatalog : AppCompatActivity() {

    private lateinit var binding: AdminCatalogBinding
    private lateinit var MangroveInformation: DatabaseReference
    private val allMangroves = mutableListOf<BakajuanSpeciesData>()

    private val identifiedSpecies = listOf("Bakhaw Babae", "Buta-buta", "Miyapi", "Pagatpat", "Pototan")

    private var seeMoreIdentifiableClicked = false
    private var seeMoreNonIdentifiableClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminCatalogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loadingSpecies.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE

        // hide bottom nav when keyboard opens
        val rootLayout = binding.adminCatalog
        val bottomNav = binding.bottomNav

        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootLayout.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootLayout.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            bottomNav.visibility =
                if (keypadHeight > screenHeight * 0.15) View.GONE else View.VISIBLE
        }

        // search
        binding.searchButton.setOnClickListener {
            val searchID = binding.searchID.text.toString()
            if (searchID.isNotEmpty()) searchSpeciesID(searchID)
            else showDialog("Enter Species ID")
        }

        // filter
        binding.filter.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.filter_species_admin, null)
            val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setView(dialogView)
                .create()

            dialogView.findViewById<Button>(R.id.btnBakhawBabae).setOnClickListener {
                filterMangroves("Bakhaw Babae")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnBakhawLalaki).setOnClickListener {
                filterMangroves("Bakhaw Lalaki")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnButaButa).setOnClickListener {
                filterMangroves("Buta-buta")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnMiyapi).setOnClickListener {
                filterMangroves("Miyapi")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnPagatpat).setOnClickListener {
                filterMangroves("Pagatpat")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnPototan).setOnClickListener {
                filterMangroves("Pototan")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnBato).setOnClickListener {
                filterMangroves("Bakawan Bato")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnBungalon).setOnClickListener {
                filterMangroves("Bungalon")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnBusain).setOnClickListener {
                filterMangroves("Busain")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnLangarai).setOnClickListener {
                filterMangroves("Langarai")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnApiapi).setOnClickListener {
                filterMangroves("Api-api")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnTangal).setOnClickListener {
                filterMangroves("Tangal")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnTabigi).setOnClickListener {
                filterMangroves("Tabigi")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnNipa).setOnClickListener {
                filterMangroves("Nipa")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnSagingsaging).setOnClickListener {
                filterMangroves("Saging-saging")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnNilad).setOnClickListener {
                filterMangroves("Nilad")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnPedada).setOnClickListener {
                filterMangroves("Pedada")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnDungonLate).setOnClickListener {
                filterMangroves("Dungon Late")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnDungon).setOnClickListener {
                filterMangroves("Dungon")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnBani).setOnClickListener {
                filterMangroves("Bani")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnTui).setOnClickListener {
                filterMangroves("Tui")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnGapasgapas).setOnClickListener {
                filterMangroves("Gapas-gapas")
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnNonMangroveLeaf).setOnClickListener {
                // filter all species with ID starting with "NMS"
                val filteredList = allMangroves.filter { it.speciesID?.startsWith("NMS", ignoreCase = true) == true }

                // clear previous views
                binding.identifiableSpecies.removeAllViews()
                binding.nonIdentifiableSpecies.removeAllViews()

                // display results as non-identifiable
                filteredList.forEach { nonIdentifiableSpecies(it) }

                // update visibility
                binding.lineIdentifiableSpecies.visibility = View.GONE
                binding.lineNonIdentifiableSpecies.visibility =
                    if (filteredList.isNotEmpty()) View.VISIBLE else View.GONE
                binding.seeMoreIdentifiable.visibility = View.GONE
                binding.seeMoreNonIdentifiable.visibility = View.GONE

                showNoSpeciesMessage(filteredList.isEmpty())
                speciesVisibility()

                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnIdentifiable).setOnClickListener {
                filterIdentifiable(true)
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnNonIdentifiable).setOnClickListener {
                filterIdentifiable(false)
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnShowAll).setOnClickListener {
                filterMangroves("ALL")
                dialog.dismiss()
            }

            dialog.show()
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // navigation
        binding.backToDashboard.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
            noTransition()
        }
        binding.logMangrove.setOnClickListener {
            startActivity(Intent(this, AdminMangrove::class.java))
            noTransition()
        }
        binding.plotCoordinates.setOnClickListener {
            startActivity(Intent(this, AdminMap::class.java))
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

        displayMangroveCatalog()
    }

    private val updateMangroveLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        displayMangroveCatalog()
    }

    private fun noTransition() {
        overridePendingTransition(0, 0)
    }

    private fun displayMangroveCatalog() {
        MangroveInformation = FirebaseDatabase.getInstance().getReference("Mangrove Information")
        MangroveInformation.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                // hide loading
                binding.loadingSpecies.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE

                binding.identifiableSpecies.removeAllViews()
                binding.nonIdentifiableSpecies.removeAllViews()
                allMangroves.clear()

                // get all
                for (mangroveSnapshot in snapshot.children) {
                    val species = mangroveSnapshot.getValue(BakajuanSpeciesData::class.java)
                    species?.let {
                        allMangroves.add(it)
                    }
                }

                // separate non/identifiable bakajuan
                val identifiableList = allMangroves.filter { species ->
                    identifiedSpecies.any { name -> species.localName?.contains(name, true) == true }
                }

                val nonIdentifiableList = allMangroves.filter { species ->
                    !identifiedSpecies.any { name -> species.localName?.contains(name, true) == true }
                }

                // limit (10)
                val limitedIdentifiable = if (seeMoreIdentifiableClicked) identifiableList else identifiableList.take(10)
                val limitedNonIdentifiable = if (seeMoreNonIdentifiableClicked) nonIdentifiableList else nonIdentifiableList.take(10)

                binding.identifiableSpecies.removeAllViews()
                binding.nonIdentifiableSpecies.removeAllViews()

                limitedIdentifiable.forEach { identifiableSpecies(it) }
                limitedNonIdentifiable.forEach { nonIdentifiableSpecies(it) }

                binding.seeMoreIdentifiable.visibility =
                    if (identifiableList.size > 10 && !seeMoreIdentifiableClicked) View.VISIBLE else View.GONE

                binding.seeMoreNonIdentifiable.visibility =
                    if (nonIdentifiableList.size > 10 && !seeMoreNonIdentifiableClicked) View.VISIBLE else View.GONE


                binding.seeMoreIdentifiable.setOnClickListener {
                    binding.identifiableSpecies.removeAllViews()
                    allMangroves.filter { species ->
                        identifiedSpecies.any { itName -> species.localName?.contains(itName, true) == true }
                    }.forEach { identifiableSpecies(it) }

                    binding.seeMoreIdentifiable.visibility = View.GONE
                    seeMoreIdentifiableClicked = true
                }

                binding.seeMoreNonIdentifiable.setOnClickListener {
                    binding.nonIdentifiableSpecies.removeAllViews()
                    allMangroves.filter { species ->
                        !identifiedSpecies.any { itName -> species.localName?.contains(itName, true) == true }
                    }.forEach { nonIdentifiableSpecies(it) }

                    binding.seeMoreNonIdentifiable.visibility = View.GONE
                    seeMoreNonIdentifiableClicked = true
                }
                speciesVisibility()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminCatalog, "Failed to retrieve catalog", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun identifiableSpecies(species: BakajuanSpeciesData) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.mangrove_catalog_admin, binding.identifiableSpecies, false)
        catalogView(view, species)
        binding.identifiableSpecies.addView(view)
    }

    private fun nonIdentifiableSpecies(species: BakajuanSpeciesData) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.mangrove_catalog_admin, binding.nonIdentifiableSpecies, false)
        catalogView(view, species)
        binding.nonIdentifiableSpecies.addView(view)
    }

    private fun filterIdentifiable(isIdentifiable: Boolean) {

        showNoSpeciesMessage(false)

        binding.identifiableSpecies.removeAllViews()
        binding.nonIdentifiableSpecies.removeAllViews()

        val filteredList = allMangroves.filter { species ->
            val isInIdentified = identifiedSpecies.any { name ->
                species.localName?.contains(name, true) == true
            }
            isIdentifiable == isInIdentified
        }

        if (isIdentifiable) filteredList.forEach { identifiableSpecies(it) }
        else filteredList.forEach { nonIdentifiableSpecies(it) }

        binding.lineIdentifiableSpecies.visibility =
            if (isIdentifiable && binding.identifiableSpecies.childCount > 0) View.VISIBLE else View.GONE
        binding.lineNonIdentifiableSpecies.visibility =
            if (!isIdentifiable && binding.nonIdentifiableSpecies.childCount > 0) View.VISIBLE else View.GONE

        // don't show See More
        binding.seeMoreIdentifiable.visibility = View.GONE
        binding.seeMoreNonIdentifiable.visibility = View.GONE

        speciesVisibility()
    }

    private fun filterMangroves(filterName: String) {

        showNoSpeciesMessage(false)

        binding.identifiableSpecies.removeAllViews()
        binding.nonIdentifiableSpecies.removeAllViews()

        val filteredList = if (filterName == "ALL") allMangroves
        else allMangroves.filter { it.localName?.contains(filterName, true) == true }

        for (species in filteredList) {
            if (identifiedSpecies.any { species.localName?.contains(it, true) == true }) {
                identifiableSpecies(species)
            } else {
                nonIdentifiableSpecies(species)
            }
        }

        showNoSpeciesMessage(filteredList.isEmpty())

        // update line visibility
        binding.lineIdentifiableSpecies.visibility =
            if (binding.identifiableSpecies.childCount > 0) View.VISIBLE else View.GONE
        binding.lineNonIdentifiableSpecies.visibility =
            if (binding.nonIdentifiableSpecies.childCount > 0) View.VISIBLE else View.GONE

        // hide See More when filtering
        binding.seeMoreIdentifiable.visibility = View.GONE
        binding.seeMoreNonIdentifiable.visibility = View.GONE

        speciesVisibility()
    }

    private fun showNoSpeciesMessage(show: Boolean) {
        binding.noSpecies.visibility = if (show) View.VISIBLE else View.GONE
    }

    // search
    private fun searchSpeciesID(searchID: String) {
        MangroveInformation = FirebaseDatabase.getInstance().getReference("Mangrove Information")
        MangroveInformation.get().addOnSuccessListener { snapshot ->

            binding.identifiableSpecies.removeAllViews()
            binding.nonIdentifiableSpecies.removeAllViews()

            val results = snapshot.children
                .mapNotNull { it.getValue(BakajuanSpeciesData::class.java) }
                .filter { it.speciesID.equals(searchID.trim(), true) }

            if (results.isNotEmpty()) {
                Toast.makeText(this, "Result Found", Toast.LENGTH_SHORT).show()

                results.forEach {
                    if (identifiedSpecies.any { name ->
                            it.localName?.contains(name, true) == true
                        }) {
                        identifiableSpecies(it)
                    } else {
                        nonIdentifiableSpecies(it)
                    }
                }

                showNoSpeciesMessage(false)
                speciesVisibility()

            } else {
                showNoSpeciesMessage(true)
                showDialog("Species ID not found in Catalog.")

                binding.identifiableSpecies.visibility = View.GONE
                binding.nonIdentifiableSpecies.visibility = View.GONE
                binding.textIdentifiableSpecies.visibility = View.GONE
                binding.textNonIdentifiableSpecies.visibility = View.GONE
                binding.lineIdentifiableSpecies.visibility = View.GONE
                binding.lineNonIdentifiableSpecies.visibility = View.GONE
                binding.seeMoreIdentifiable.visibility = View.GONE
                binding.seeMoreNonIdentifiable.visibility = View.GONE
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun catalogView(view: View, species: BakajuanSpeciesData) {
        val mangroveImage = view.findViewById<ImageView>(R.id.mangroveImage)
        val speciesID = view.findViewById<TextView>(R.id.speciesID)
        val localName = view.findViewById<TextView>(R.id.localName)
        val scientificName = view.findViewById<TextView>(R.id.scientificName)
        val mangroveZone = view.findViewById<TextView>(R.id.mangroveZone)
        val height = view.findViewById<TextView>(R.id.height)
        val circumference = view.findViewById<TextView>(R.id.circumference)
        val carbonSequestered = view.findViewById<TextView>(R.id.carbonSequestered)
        val estimatedAge = view.findViewById<TextView>(R.id.estimatedAge)
        val dateCatalogued = view.findViewById<TextView>(R.id.dateCatalogued)
        val latitude = view.findViewById<TextView>(R.id.latitude)
        val longitude = view.findViewById<TextView>(R.id.longitude)

        speciesID.text = species.speciesID
        localName.text = species.localName
        scientificName.text = species.scientificName
        mangroveZone.text = species.zone

        height.text = if (species.height.isNullOrEmpty()) "No Ht." else "${species.height} m"
        circumference.text = if (species.circumference.isNullOrEmpty()) "No Circ." else "${species.circumference} cm"
        carbonSequestered.text = if (species.estimatedCarbonSequestered.isNullOrEmpty()) "No CO₂ Seq." else species.estimatedCarbonSequestered
        estimatedAge.text = if (species.estimatedAge.isNullOrEmpty()) "No Est. Age" else species.estimatedAge

        Glide.with(this).load(species.mangroveImage).into(mangroveImage)

        // screen adjustment
        val metrics = resources.displayMetrics
        val screenDpWidth = metrics.widthPixels / metrics.density

        val viewMapButton = view.findViewById<AppCompatButton>(R.id.viewMap)
        if (screenDpWidth <= 360) {
            viewMapButton.textSize = 11f
            viewMapButton.height = 35
        } else if (screenDpWidth <= 480) {
            viewMapButton.textSize = 12f
        } else {
            viewMapButton.textSize = 10f
        }

        // coordinates map
        species.speciesID?.let { id ->
            val coordRef = FirebaseDatabase.getInstance().getReference("Species Coordinates")
            coordRef.child(id).get().addOnSuccessListener { snapshot ->
                val lat = snapshot.child("latitude").getValue(Double::class.java)
                val lng = snapshot.child("longitude").getValue(Double::class.java)
                latitude.text = lat?.toString() ?: "No Coordinates"
                longitude.text = lng?.toString() ?: "No Coordinates"
            }
        }

        // view map
        view.findViewById<AppCompatButton>(R.id.viewMap).setOnClickListener {
            val id = species.speciesID?.trim() ?: return@setOnClickListener

            val coordRef = FirebaseDatabase.getInstance().getReference("Species Coordinates")
            coordRef.child(id).get().addOnSuccessListener { snapshot ->
                val lat = snapshot.child("latitude").getValue(Double::class.java)
                val lng = snapshot.child("longitude").getValue(Double::class.java)

                if (lat != null && lng != null) {
                    val intent = Intent(this, AdminDashboard::class.java)
                    intent.putExtra("zoomSpeciesID", id)
                    intent.putExtra("focusMap", true)
                    startActivity(intent)
                } else {
                    showDialog("Species ID $id has no coordinates.")
                }
            }
        }

        // date
        species.dateCatalogued?.let {
            val sdf = java.text.SimpleDateFormat("MM/dd/yy", java.util.Locale.getDefault())
            dateCatalogued.text = sdf.format(java.util.Date(it))
        } ?: run { dateCatalogued.text = "N/A" }

        // update
        view.findViewById<AppCompatButton>(R.id.updateMangrove).setOnClickListener {
            val intent = Intent(this, UpdateMangrove::class.java)
            intent.putExtra("speciesID", species.speciesID?.trim())
            updateMangroveLauncher.launch(intent)
        }

        // delete
        view.findViewById<AppCompatButton>(R.id.deleteMangrove).setOnClickListener {
            showDeleteDialog(species)
        }
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)
        displayMangroveCatalog()
    }

    private fun speciesVisibility() {
        binding.textIdentifiableSpecies.visibility =
            if (binding.identifiableSpecies.childCount == 0) View.GONE else View.VISIBLE
        binding.identifiableSpecies.visibility =
            if (binding.identifiableSpecies.childCount == 0) View.GONE else View.VISIBLE

        binding.textNonIdentifiableSpecies.visibility =
            if (binding.nonIdentifiableSpecies.childCount == 0) View.GONE else View.VISIBLE
        binding.nonIdentifiableSpecies.visibility =
            if (binding.nonIdentifiableSpecies.childCount == 0) View.GONE else View.VISIBLE
    }

    private fun showDeleteDialog(species: BakajuanSpeciesData) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.delete_dialog, null)
        val deleteText = dialogView.findViewById<TextView>(R.id.deleteText)
        deleteText.text = "Are you sure you want to delete Species ID ${species.speciesID}?"

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<AppCompatButton>(R.id.yesDelete).setOnClickListener {
            val dbRef = FirebaseDatabase.getInstance()
                .getReference("Mangrove Information")
                .child(species.speciesID!!.trim())

            val coordRef = FirebaseDatabase.getInstance()
                .getReference("Species Coordinates")
                .child(species.speciesID!!.trim())

            val storageRef = try {
                FirebaseStorage.getInstance().getReferenceFromUrl(species.mangroveImage!!)
            } catch (e: Exception) {
                null
            }

            // delete image if it exists
            val deleteImageTask = storageRef?.delete()?.addOnFailureListener {
                // ignore if image does not exist
            } ?: run {
                // return a completed task if storageRef is null
                com.google.android.gms.tasks.Tasks.forResult(null)
            }

            deleteImageTask.addOnCompleteListener {
                // delete database references regardless of image deletion success
                dbRef.removeValue().addOnSuccessListener {
                    coordRef.removeValue().addOnSuccessListener {
                        showSuccessDialog("Deleted ${species.speciesID}")
                        displayMangroveCatalog()
                        alertDialog.dismiss()
                    }
                }.addOnFailureListener {
                    showErrorDialog("Failed to delete database: ${it.message}")
                }
            }
        }

        dialogView.findViewById<AppCompatButton>(R.id.noDelete).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showSuccessDialog(message: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)

        val layout =
            LayoutInflater.from(this).inflate(R.layout.success_dialog, null)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        layout.findViewById<TextView>(R.id.title).text = "Delete Successful"
        layout.findViewById<TextView>(R.id.text).text = message

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 2000)
    }

    private fun showErrorDialog(message: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val layout =
            LayoutInflater.from(this).inflate(R.layout.acc_failed_dialog, null)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        layout.findViewById<TextView>(R.id.title).text = "Delete Failed"
        layout.findViewById<TextView>(R.id.text).text = message
        layout.findViewById<TextView>(R.id.ok).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showDialog(message: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val layout =
            LayoutInflater.from(this).inflate(R.layout.acc_failed_dialog, null)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        layout.findViewById<TextView>(R.id.title).text = "Error"
        layout.findViewById<TextView>(R.id.text).text = message
        layout.findViewById<TextView>(R.id.ok).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}