package com.mangrove.bakajuan

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CaptureUploadBakajuan : AppCompatActivity() {

    private lateinit var previewCamera: PreviewView
    private lateinit var capture: ImageView
    private lateinit var gallery: ImageView
    private lateinit var back: ImageView
    private lateinit var information: ImageView
    private lateinit var focusSquare: View

    private var imageCapture: ImageCapture? = null
    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null

    private val cameraPermission = Manifest.permission.CAMERA
    private val requestCodeCamera = 10

    // gallery picker
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val intent = Intent(this, ImageBakajuan::class.java)
                intent.putExtra("image_uri", it.toString())
                startActivity(intent)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.capture_upload_bakajuan)

        previewCamera = findViewById(R.id.previewCamera)
        capture = findViewById(R.id.capture)
        gallery = findViewById(R.id.gallery)
        back = findViewById(R.id.back)
        information = findViewById(R.id.information)
        focusSquare = findViewById(R.id.focusSquare)

        // Show distance reminder dialog before using camera
        showDistanceNoteDialog()

        // check permission at startup
        if (ContextCompat.checkSelfPermission(this, cameraPermission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), requestCodeCamera)
        }

        // capture button
        capture.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, cameraPermission)
                == PackageManager.PERMISSION_GRANTED
            ) {
                takePhoto()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), requestCodeCamera)
            }
        }

        // gallery
        gallery.setOnClickListener { pickImageLauncher.launch("image/*") }

        // back
        back.setOnClickListener { finish() }

        // info screen
        information.setOnClickListener {
            val intent = Intent(this, IdentifyLeaf::class.java)
            startActivity(intent)
        }
    }

    // Handle user’s decision
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCamera) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                showErrorDialog("Camera permission is required to capture mangrove image.")
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewCamera.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo

                enableTapToFocus()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // tap to focus
    private fun enableTapToFocus() {
        previewCamera.setOnTouchListener { view, motionEvent ->

            if (motionEvent.action == MotionEvent.ACTION_DOWN) {

                // show focus square animation
                showFocusSquare(motionEvent.x, motionEvent.y)

                // trigger CameraX focus
                val factory = previewCamera.meteringPointFactory
                val point = factory.createPoint(motionEvent.x, motionEvent.y)
                val action = FocusMeteringAction.Builder(point)
                    .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                cameraControl?.startFocusAndMetering(action)
            }
            true
        }
    }

    private fun showFocusSquare(x: Float, y: Float) {
        // move square to touch position
        focusSquare.x = x - focusSquare.width / 2
        focusSquare.y = y - focusSquare.height / 2

        // animate
        focusSquare.alpha = 1f
        focusSquare.visibility = View.VISIBLE
        focusSquare.scaleX = 1.5f
        focusSquare.scaleY = 1.5f

        focusSquare.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(150)
            .withEndAction {
                focusSquare.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        focusSquare.visibility = View.GONE
                    }
            }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // optional: add small autofocus delay to ensure clear image
        lifecycleScope.launch {
            capture.isEnabled = false
            delay(500) // small delay to stabilize focus
            val photoFile = createImageFile()
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this@CaptureUploadBakajuan),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        exc.printStackTrace()
                        showErrorDialog("Failed to capture image.")
                        capture.isEnabled = true
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = Uri.fromFile(photoFile)
                        val intent = Intent(this@CaptureUploadBakajuan, ImageBakajuan::class.java)
                        intent.putExtra("image_uri", savedUri.toString())
                        startActivity(intent)
                        capture.isEnabled = true
                    }
                }
            )
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ImageBakajuan")
        if (!storageDir.exists()) storageDir.mkdirs()
        return File(storageDir, "IMG_${timeStamp}.jpg")
    }

    private fun showDistanceNoteDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val layout = LayoutInflater.from(this).inflate(R.layout.camera_note, null)
        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title: TextView = layout.findViewById(R.id.title)
        val text: TextView = layout.findViewById(R.id.text)
        val ok = layout.findViewById<TextView>(R.id.ok)

        title.text = "Camera Note"
        text.text = "Please ensure the distance between the camera and the leaf is approximately 20 cm before taking the picture for best accuracy."
        ok.text = "Got it"

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

        title.text = "Capture Error"
        text.text = message
        ok.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}