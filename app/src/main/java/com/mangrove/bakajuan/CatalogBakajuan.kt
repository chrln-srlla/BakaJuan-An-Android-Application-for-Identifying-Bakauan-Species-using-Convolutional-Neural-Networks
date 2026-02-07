package com.mangrove.bakajuan

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.mangrove.bakajuan.databinding.CatalogBakajuanBinding

class CatalogBakajuan : AppCompatActivity() {

    private lateinit var binding: CatalogBakajuanBinding
    private lateinit var databaseReference: DatabaseReference
    private val allMangroves = mutableListOf<BakajuanSpeciesData>()

    private val identifiedSpecies = listOf("Bakhaw Babae", "Buta-buta", "Miyapi", "Pagatpat", "Pototan")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CatalogBakajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loadingSpecies.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE

        binding.searchButton.setOnClickListener {
            val searchID = binding.searchID.text.toString()
            if (searchID.isNotEmpty()) {
                searchSpeciesID(searchID)
            } else {
                showDialog("Enter Species ID")
            }
        }

        binding.filter.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.filter_species, null)
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
                filterMangroves("Non-Mangrove")
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
            dialogView.findViewById<Button>(R.id.btnNonMangroveLeaf).setOnClickListener {

                val filteredList = allMangroves.filter { it.speciesID?.startsWith("NMS", ignoreCase = true) == true }

                binding.identifiableSpecies.removeAllViews()
                binding.nonIdentifiableSpecies.removeAllViews()

                filteredList.forEach { nonIdentifiableSpecies(it) }

                binding.lineIdentifiableSpecies.visibility = View.GONE
                binding.lineNonIdentifiableSpecies.visibility =
                    if (filteredList.isNotEmpty()) View.VISIBLE else View.GONE
                binding.seeMoreIdentifiable.visibility = View.GONE
                binding.seeMoreNonIdentifiable.visibility = View.GONE

                showNoSpeciesMessage(filteredList.isEmpty())
                speciesVisibility()

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

        binding.navHome.setOnClickListener {
            startActivity(Intent(this, HomeBakajuan::class.java))
            noTransition()
        }

        binding.navGeomap.setOnClickListener {
            startActivity(Intent(this, GeomapBakajuan::class.java))
            noTransition()
        }

        binding.mangroveCaptureUpload.setOnClickListener {
            startActivity(Intent(this, CaptureUploadBakajuan::class.java))
            noTransition()
        }

        displayMangroveCatalog()
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)
    }

    private fun noTransition() {
        overridePendingTransition(0, 0)
    }

    private fun displayMangroveCatalog() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Mangrove Information")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                // hide loading
                binding.loadingSpecies.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE

                binding.identifiableSpecies.removeAllViews()
                binding.nonIdentifiableSpecies.removeAllViews()
                allMangroves.clear()

                // load all
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
                val limitedIdentifiable = identifiableList.take(10)
                val limitedNonIdentifiable = nonIdentifiableList.take(10)

                binding.identifiableSpecies.removeAllViews()
                binding.nonIdentifiableSpecies.removeAllViews()

                limitedIdentifiable.forEach { identifiableSpecies(it) }
                limitedNonIdentifiable.forEach { nonIdentifiableSpecies(it) }

                // show See More if more than 10 species
                binding.seeMoreIdentifiable.visibility =
                    if (identifiableList.size > 10) View.VISIBLE else View.GONE

                binding.seeMoreNonIdentifiable.visibility =
                    if (nonIdentifiableList.size > 10) View.VISIBLE else View.GONE

                // see more text
                binding.seeMoreIdentifiable.setOnClickListener {
                    binding.identifiableSpecies.removeAllViews()
                    identifiableList.forEach { identifiableSpecies(it) }
                    binding.seeMoreIdentifiable.visibility = View.GONE
                }

                binding.seeMoreNonIdentifiable.setOnClickListener {
                    binding.nonIdentifiableSpecies.removeAllViews()
                    nonIdentifiableList.forEach { nonIdentifiableSpecies(it) }
                    binding.seeMoreNonIdentifiable.visibility = View.GONE
                }
                speciesVisibility()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CatalogBakajuan, "Failed to retrieve catalog", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun identifiableSpecies(species: BakajuanSpeciesData) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.mangrove_catalog_user, binding.identifiableSpecies, false)
        catalogView(view, species)
        binding.identifiableSpecies.addView(view)
    }

    private fun nonIdentifiableSpecies(species: BakajuanSpeciesData) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.mangrove_catalog_user, binding.nonIdentifiableSpecies, false)
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

        if (filteredList.isEmpty()) {
            showNoSpeciesMessage(true)
        } else {
            showNoSpeciesMessage(false)
        }

        if (isIdentifiable) filteredList.forEach { identifiableSpecies(it) }
        else filteredList.forEach { nonIdentifiableSpecies(it) }

        binding.lineIdentifiableSpecies.visibility =
            if (isIdentifiable && binding.identifiableSpecies.childCount > 0) View.VISIBLE else View.GONE
        binding.lineNonIdentifiableSpecies.visibility =
            if (!isIdentifiable && binding.nonIdentifiableSpecies.childCount > 0) View.VISIBLE else View.GONE

        // hide See More when filtering
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


    private fun searchSpeciesID(searchID: String) {
        databaseReference.get().addOnSuccessListener { snapshot ->

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
        val localName = view.findViewById<TextView>(R.id.localName)
        val speciesID = view.findViewById<TextView>(R.id.speciesID)
        val mangroveDescription = view.findViewById<TextView>(R.id.mangroveDescription)
        val speciesIDLabel = view.findViewById<TextView>(R.id.speciesIDLabel)

        Glide.with(this).load(species.mangroveImage).into(mangroveImage)
        localName.text = species.localName
        speciesID.text = species.speciesID
        mangroveDescription.text = species.characteristics

        val screenWidthDp = resources.displayMetrics.run { widthPixels / density }
        localName.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (screenWidthDp <= 360) 12f else 14f)
        speciesIDLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (screenWidthDp <= 360) 11f else 14f)
        speciesID.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (screenWidthDp <= 360) 11f else 14f)
        mangroveDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (screenWidthDp <= 360) 11f else 11f)

        val mangroveInformation = view.findViewById<LinearLayout>(R.id.mangroveInformation)
        mangroveInformation.setOnClickListener {
            val intent = Intent(this, CatalogInformation::class.java)
            intent.putExtra("speciesID", species.speciesID?.trim())
            startActivity(intent)
        }
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

    private fun showDialog(message: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val layout = LayoutInflater.from(this).inflate(R.layout.acc_failed_dialog, null)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        layout.findViewById<TextView>(R.id.title).text = "Error"
        layout.findViewById<TextView>(R.id.text).text = message
        layout.findViewById<TextView>(R.id.ok).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}