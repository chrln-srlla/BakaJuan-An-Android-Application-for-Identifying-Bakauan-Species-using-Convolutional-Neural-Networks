package com.mangrove.bakajuan

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mangrove.bakajuan.databinding.MangroveInformationBinding
import org.json.JSONArray
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MangroveInformation : AppCompatActivity() {

    private lateinit var localNameText: TextView
    private lateinit var scientificNameText: TextView
    private lateinit var zoneText: TextView
    private lateinit var characteristicsText: TextView
    private lateinit var mangroveImage: ImageView
    private lateinit var confidenceText: TextView

    private lateinit var tflite: Interpreter
    private lateinit var classNames: List<String>
    private lateinit var binding: MangroveInformationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mangrove_information)
        binding = MangroveInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navHome.setOnClickListener {
            startActivity(Intent(this, HomeBakajuan::class.java))
        }
        binding.navGeomap.setOnClickListener {
            startActivity(Intent(this, GeomapBakajuan::class.java))
        }
        binding.navCatalog.setOnClickListener {
            startActivity(Intent(this, CatalogBakajuan::class.java))
        }
        binding.backCaptureUpload.setOnClickListener {
            finish()
        }
        binding.mangroveCaptureUpload.setOnClickListener {
            startActivity(Intent(this, CaptureUploadBakajuan::class.java))
        }

        localNameText = findViewById(R.id.localName)
        scientificNameText = findViewById(R.id.scientificName)
        zoneText = findViewById(R.id.mangroveZone)
        characteristicsText = findViewById(R.id.characteristics)
        mangroveImage = findViewById(R.id.imageBakajuan)
        confidenceText = findViewById(R.id.confidence)

        // model
        tflite = Interpreter(FileUtil.loadMappedFile(this, "BKJ_Model.tflite"))
        classNames = loadClassNames("class_names.json")

        val imageUriString = intent.getStringExtra("image_uri")
        imageUriString?.let { uriStr ->
            val imageUri = Uri.parse(uriStr)

            val bmp = contentResolver.openInputStream(imageUri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }

            bmp?.let {
                mangroveImage.setImageBitmap(it)

                val (predictedClass, confidence) = classifyImage(it)
                val className = predictedClass.trim()
                val info =
                    mangroveData.entries.find { it.key.equals(className, ignoreCase = true) }?.value

                if (info != null) {
                    localNameText.text = info.localName
                    scientificNameText.text = info.scientificName
                    zoneText.text = info.zone
                    characteristicsText.text = info.characteristics
                } else {
                    localNameText.text = className
                    scientificNameText.text = "Unknown"
                    zoneText.text = "Unknown"
                    characteristicsText.text = "The image is not recognized as a mangrove species."
                }

                confidenceText.text = "%.2f%%".format(confidence * 100)
            }
        }
    }

    private fun loadClassNames(filename: String): List<String> {
        val jsonString = assets.open(filename).bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        val labels = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            labels.add(jsonArray.getString(i))
        }
        return labels
    }
    
    private fun classifyImage(bitmap: Bitmap): Pair<String, Float> {
        val inputSize = 224
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputSize * inputSize)
        resized.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in intValues) {
            val r = ((pixel shr 16) and 0xFF).toFloat()
            val g = ((pixel shr 8) and 0xFF).toFloat()
            val b = (pixel and 0xFF).toFloat()
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        val inputArray = arrayOf(byteBuffer)
        val outputBuffer = Array(1) { FloatArray(classNames.size) }
        val outputMap = mapOf(0 to outputBuffer)
        tflite.runForMultipleInputsOutputs(inputArray, outputMap)

        val confidences = outputBuffer[0]
        val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1

        return if (maxIndex != -1) {
            classNames[maxIndex] to confidences[maxIndex]
        } else {
            "Unknown" to 0f
        }
    }
}