package com.mangrove.bakajuan

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AdminUser : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "BakajuanPrefs"
    private val KEY_FIRST_LAUNCH = "isFirstLaunch"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Check if user already logged in as admin
        val currentAdmin = FirebaseAuth.getInstance().currentUser
        val isFirstLaunch = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)

        if (currentAdmin != null) {
            // Admin already logged
            startActivity(Intent(this, AdminDashboard::class.java))
            finish()
            return
        } else if (!isFirstLaunch) {
            // Guest already continued
            startActivity(Intent(this, HomeBakajuan::class.java))
            finish()
            return
        }

        // Show admin/guest selection screen if first launch
        setContentView(R.layout.admin_user)

        val loginAsAdmin = findViewById<Button>(R.id.loginAsAdmin)
        loginAsAdmin.setOnClickListener {
            val intent = Intent(this, AdminLogin::class.java)
            startActivity(intent)
        }

        val continueAsUser = findViewById<Button>(R.id.continueAsUser)
        continueAsUser.setOnClickListener {
            // Mark that guest already launched app
            sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()

            val intent = Intent(this, HomeBakajuan::class.java)
            intent.putExtra("showSuccessDialog", true)
            startActivity(intent)
            finish()
        }

        val aboutUs = findViewById<LinearLayout>(R.id.aboutUs)
        aboutUs.setOnClickListener {
            startActivity(Intent(this, AboutUs::class.java))
        }
    }
}