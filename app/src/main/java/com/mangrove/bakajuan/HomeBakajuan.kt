package com.mangrove.bakajuan

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.mangrove.bakajuan.databinding.HomeBakajuanBinding

class HomeBakajuan : AppCompatActivity() {

    private lateinit var binding: HomeBakajuanBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeBakajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val metrics = resources.displayMetrics
        val screenDpWidth = metrics.widthPixels / metrics.density

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        if (screenDpWidth <= 360) {
            binding.welcomeIcon.layoutParams.width = 62.dpToPx(this)
            binding.welcomeIcon.layoutParams.height = 62.dpToPx(this)
            binding.welcomeIcon.requestLayout()

            binding.userTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            binding.descriptionSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }

        // Show dialog when logged in as guest
        val showDialog = intent.getBooleanExtra("showSuccessDialog", false)
        if (showDialog) {
            showDialogSuccess("You have successfully logged in as Guest")
        }

        // Logout as Guest
        binding.logoutUser.setOnClickListener {
            showLogoutDialog()
        }

        binding.navGeomap.setOnClickListener {
            startActivity(Intent(this, GeomapBakajuan::class.java))
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

        binding.identifyLeaf.setOnClickListener {
            startActivity(Intent(this, IdentifyLeaf::class.java))
            noTransition()
        }

        binding.learnMoreMangroveTree.setOnClickListener {
            startActivity(Intent(this, FindMangrove::class.java))
            noTransition()
        }

        // Load species images from Firebase
        loadSpeciesImages()

        // Catalog buttons open with filter
        binding.catalogBabae.setOnClickListener { openCatalog("Bakhaw Babae") }
        binding.catalogButaButa.setOnClickListener { openCatalog("Buta-buta") }
        binding.catalogMiyapi.setOnClickListener { openCatalog("Miyapi") }
        binding.catalogPagatpat.setOnClickListener { openCatalog("Pagatpat") }
        binding.catalogPototan.setOnClickListener { openCatalog("Pototan") }
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)
    }

    private fun noTransition() {
        overridePendingTransition(0, 0)
    }

    private fun loadSpeciesImages() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Mangrove Information")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val babaeImages = mutableListOf<String>()
                val butaButaImages = mutableListOf<String>()
                val miyapiImages = mutableListOf<String>()
                val pagatpatImages = mutableListOf<String>()
                val pototanImages = mutableListOf<String>()

                for (mangroveSnapshot in snapshot.children) {
                    val species = mangroveSnapshot.getValue(BakajuanSpeciesData::class.java)
                    species?.let {
                        when (it.localName?.trim()) {
                            "Bakhaw Babae" -> babaeImages.add(it.mangroveImage ?: "")
                            "Buta-buta" -> butaButaImages.add(it.mangroveImage ?: "")
                            "Miyapi" -> miyapiImages.add(it.mangroveImage ?: "")
                            "Pagatpat" -> pagatpatImages.add(it.mangroveImage ?: "")
                            "Pototan" -> pototanImages.add(it.mangroveImage ?: "")
                            else -> {
                            }
                        }
                    }
                }

                // Start slideshow for each species
                startImageSlideshow(babaeImages, binding.babaeImage)
                startImageSlideshow(butaButaImages, binding.butaButaImage)
                startImageSlideshow(miyapiImages, binding.miyapiImage)
                startImageSlideshow(pagatpatImages, binding.pagatpatImage)
                startImageSlideshow(pototanImages, binding.pototanImage)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeBakajuan, "Failed to load images", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private var slideshowHandler: Handler? = null
    private var slideshowRunnable: Runnable? = null

    private fun startImageSlideshow(images: List<String>, imageView: android.widget.ImageView) {
        if (images.isEmpty()) return

        var index = 0
        slideshowHandler = Handler(mainLooper)
        slideshowRunnable = object : Runnable {
            override fun run() {
                // check if activity is finishing
                if (isFinishing) return

                Glide.with(this@HomeBakajuan)
                    .load(images[index])
                    .into(imageView)
                index = (index + 1) % images.size
                slideshowHandler?.postDelayed(this, 2000)
            }
        }
        slideshowHandler?.post(slideshowRunnable!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        // stop slideshow
        slideshowHandler?.removeCallbacks(slideshowRunnable!!)
    }

    private fun openCatalog(localName: String) {
        val intent = Intent(this, CatalogBakajuan::class.java)
        intent.putExtra("filterLocalName", localName)
        startActivity(intent)
        noTransition()
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
                // Clear all preferences to reset login/first launch
                val prefs = getSharedPreferences("BakajuanPrefs", MODE_PRIVATE)
                prefs.edit().clear().apply()

                // Clear activity stack and start AdminUser fresh
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
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val layout = layoutInflater.inflate(R.layout.success_dialog, null)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val titleText = layout.findViewById<TextView>(R.id.title)
        val messageText = layout.findViewById<TextView>(R.id.text)

        titleText.text = "Login Success"
        messageText.text = message

        dialog.show()

        Handler(mainLooper).postDelayed({
            dialog.dismiss()
        }, 2000)
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