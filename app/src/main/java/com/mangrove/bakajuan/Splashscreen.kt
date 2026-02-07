package com.mangrove.bakajuan

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {

    private lateinit var treeImage: ImageView
    private lateinit var typeWriterText: TextView
    private lateinit var scanLine: View
    private lateinit var centerContainer: LinearLayout

    private val message = "Identify Mangrove Species with AI"
    private var index = 0
    private val delay: Long = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)

        treeImage = findViewById(R.id.treeImage)
        typeWriterText = findViewById(R.id.typeWriterText)
        scanLine = findViewById(R.id.scanLine)
        centerContainer = findViewById(R.id.centerContainer)

        val growRotate = AnimationUtils.loadAnimation(this, R.anim.grow_rotate)
        treeImage.startAnimation(growRotate)

        // Type
        Handler(Looper.getMainLooper()).postDelayed({
            typeWriterText.text = ""
            startTypeWriter {
                startScanAnimation()
            }
        }, 1500)
    }

    private fun startTypeWriter(onComplete: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (index < message.length) {
                    typeWriterText.append(message[index].toString())
                    index++
                    handler.postDelayed(this, delay)
                } else {
                    onComplete()
                }
            }
        }, 0)
    }

    private fun startScanAnimation() {
        centerContainer.post {
            scanLine.visibility = View.VISIBLE

            val containerWidth = centerContainer.width.toFloat()
            val containerHeight = centerContainer.height.toFloat()

            val startX = centerContainer.x - containerWidth
            val endX = centerContainer.x + containerWidth

            val startY = centerContainer.y - containerHeight
            val endY = centerContainer.y + containerHeight

            scanLine.translationX = startX
            scanLine.translationY = startY

            val animatorX = ObjectAnimator.ofFloat(scanLine, "translationX", startX, endX)
            val animatorY = ObjectAnimator.ofFloat(scanLine, "translationY", startY, endY)

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(animatorX, animatorY)
            animatorSet.duration = 4000

            animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    val intent = Intent(this@SplashScreen, AdminUser::class.java)
                    startActivity(intent)
                    finish()
                }
            })

            animatorSet.start()
        }
    }
}