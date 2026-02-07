package com.mangrove.bakajuan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ImageBakajuan : AppCompatActivity() {

    private lateinit var imageBakajuan: ImageView
    private lateinit var identify: ImageView
    private lateinit var gallery: ImageView
    private lateinit var back: ImageView
    private lateinit var information: ImageView

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                imageBakajuan.setImageURI(it)
                imageBakajuan.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_bakajuan)

        imageBakajuan = findViewById(R.id.imageBakajuan)
        identify = findViewById(R.id.identify)
        gallery = findViewById(R.id.gallery)
        back = findViewById(R.id.back)
        information = findViewById(R.id.information)

        // load image from intent if passed
        intent.getStringExtra("image_uri")?.let {
            val uriObj = Uri.parse(it)
            selectedImageUri = uriObj
            imageBakajuan.setImageURI(uriObj)
            imageBakajuan.visibility = View.VISIBLE
        }

        // identify button go to MangroveInformation
        identify.setOnClickListener {
            selectedImageUri?.let { uri ->
                val intent = Intent(this, MangroveInformation::class.java)
                intent.putExtra("image_uri", uri.toString())
                startActivity(intent)
            }
        }

        // open gallery
        gallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // back
        back.setOnClickListener { finish() }

        // IdentifyLeafCamera with the same image
        information.setOnClickListener {
            val intent = Intent(this, IdentifyLeaf::class.java)
            selectedImageUri?.let { uri ->
                intent.putExtra("image_uri", uri.toString())
            }
            startActivity(intent)
        }
    }
}