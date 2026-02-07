package com.mangrove.bakajuan

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.mangrove.bakajuan.databinding.AdminLoginBinding

class AdminLogin : AppCompatActivity() {

    private lateinit var binding: AdminLoginBinding
    private lateinit var firebaseAuthentication: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "BakajuanPrefs"
    private val KEY_FIRST_LAUNCH = "isFirstLaunch"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuthentication = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        val topGuide = findViewById<Guideline>(R.id.topGuide)
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidth = displayMetrics.widthPixels / displayMetrics.density

        if (screenWidth <= 360) {
            val params = topGuide.layoutParams as ConstraintLayout.LayoutParams
            params.guideBegin = (80 * resources.displayMetrics.density).toInt()
            topGuide.layoutParams = params
        } else {
            val params = topGuide.layoutParams as ConstraintLayout.LayoutParams
            params.guideBegin = (130 * resources.displayMetrics.density).toInt()
            topGuide.layoutParams = params
        }

        // Password Toggle
        val passwordEditText = binding.password
        val togglePassword = binding.showHidePassword
        var isPasswordVisible = false
        val originalTypeface = passwordEditText.typeface

        togglePassword.setOnClickListener {
            if (isPasswordVisible) {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.pass_show)
            } else {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.pass_hide)
            }

            passwordEditText.typeface = originalTypeface
            passwordEditText.setSelection(passwordEditText.text.length)
            isPasswordVisible = !isPasswordVisible
        }

        binding.loginAsAdmin.setOnClickListener {
            val email = binding.username.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showDialog("Enter your email and password credentials.", false)
                return@setOnClickListener
            }

            firebaseAuthentication
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
                        val intent = Intent(this, AdminDashboard::class.java)
                        intent.putExtra("showSuccessDialog", true)
                        startActivity(intent)
                        finish()

                    } else {
                        val exception = task.exception
                        val errorMessage = when (exception) {
                            is FirebaseAuthInvalidUserException ->
                                "No admin account found."

                            is FirebaseAuthInvalidCredentialsException -> {
                                val msg = exception.message ?: ""

                                when {
                                    msg.contains("email", true) ->
                                        "Invalid email format."
                                    else ->
                                        "Incorrect email or password."
                                }
                            }

                            else -> exception?.message ?: "Login failed. Please try again."
                        }

                        showDialog(errorMessage, false)
                    }
                }
        }

        // FORGOT PASSWORD
        binding.forgotPassword.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.forgot_password, null)
            val emailInput = dialogView.findViewById<EditText>(R.id.username)
            val sendEmailButton = dialogView.findViewById<AppCompatButton>(R.id.sendEmail)
            val backLogin = dialogView.findViewById<LinearLayout>(R.id.backLogin)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            sendEmailButton.setOnClickListener {
                val email = emailInput.text.toString().trim()
                if (email.isNotEmpty()) {
                    firebaseAuthentication.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                showDialogEmail("A reset link was sent to $email.", true)
                                dialog.dismiss()
                            } else {
                                val exception = task.exception
                                val errorMsg = when (exception) {
                                    is FirebaseAuthInvalidCredentialsException -> "Invalid email."
                                    else -> exception?.localizedMessage ?: "Failed to send reset email."
                                }
                                showDialogEmail(errorMsg, false)
                            }
                        }
                } else {
                    showDialogEmail("Please enter your email.", false)
                }
            }

            backLogin.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }

        binding.backSplashscreen.setOnClickListener {
            startActivity(Intent(this, AdminUser::class.java))
        }
    }

    // SUCCESS / ERROR DIALOG
    private fun showDialog(message: String, isSuccess: Boolean) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val layout = if (isSuccess)
            LayoutInflater.from(this).inflate(R.layout.acc_success_dialog, null)
        else
            LayoutInflater.from(this).inflate(R.layout.acc_failed_dialog, null)

        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val titleText: TextView = layout.findViewById(R.id.title)
        val messageText: TextView = layout.findViewById(R.id.text)
        titleText.text = if (isSuccess) "Login Success" else "Login Error"
        messageText.text = message

        val okButton: TextView = layout.findViewById(R.id.ok)
        okButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDialogEmail(message: String, isSuccess: Boolean) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val layout = if (isSuccess)
            LayoutInflater.from(this).inflate(R.layout.acc_success_dialog, null)
        else
            LayoutInflater.from(this).inflate(R.layout.acc_failed_dialog, null)

        dialog.setContentView(layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val titleText: TextView = layout.findViewById(R.id.title)
        val messageText: TextView = layout.findViewById(R.id.text)
        titleText.text = if (isSuccess) "Email Reset Link Sent" else "Email Reset Link Failed"
        messageText.text = message

        val okButton: TextView = layout.findViewById(R.id.ok)
        okButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
