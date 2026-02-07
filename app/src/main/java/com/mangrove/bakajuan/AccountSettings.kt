package com.mangrove.bakajuan

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.mangrove.bakajuan.databinding.AccountSettingsBinding

class AccountSettings : AppCompatActivity() {

    private lateinit var binding: AccountSettingsBinding
    private lateinit var authentication: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backToDashboard.setOnClickListener {
            startActivity(Intent(this, AdminDashboard::class.java))
            noTransition()
        }

        authentication = FirebaseAuth.getInstance()
        val currentUser = authentication.currentUser

        var isNewVisible = false
        binding.toggleNewPass.setOnClickListener {
            val et = binding.newPassword
            val originalTypeface = et.typeface

            isNewVisible = !isNewVisible

            if (isNewVisible) {
                et.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.toggleNewPass.setImageResource(R.drawable.pass_hide)
            } else {
                et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.toggleNewPass.setImageResource(R.drawable.pass_show)
            }

            et.typeface = originalTypeface
            et.setSelection(et.text.length)
        }

        var isCurrentVisible = false
        binding.toggleCurrentPass.setOnClickListener {
            val et = binding.currentPassword
            val originalTypeface = et.typeface

            isCurrentVisible = !isCurrentVisible

            if (isCurrentVisible) {
                et.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.toggleCurrentPass.setImageResource(R.drawable.pass_hide)
            } else {
                et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.toggleCurrentPass.setImageResource(R.drawable.pass_show)
            }

            et.typeface = originalTypeface
            et.setSelection(et.text.length)
        }

        // Save Account
        binding.saveAccount.setOnClickListener {
            val currentPassword = binding.currentPassword.text.toString().trim()
            val newEmail = binding.newUsername.text.toString().trim()
            val newPassword = binding.newPassword.text.toString().trim()

            val currentUser = authentication.currentUser
            val currentEmail = currentUser?.email

            // Validation
            if (currentUser == null) {
                showDialog("No logged-in user found.", false)
                return@setOnClickListener
            }

            if (currentEmail.isNullOrEmpty()) {
                showDialog("Current user email not found. Please log in again.", false)
                return@setOnClickListener
            }

            if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                showDialog("Please fill in all fields before updating password.", false)
                return@setOnClickListener
            }

            if (!isValidPassword(newPassword)) {
                showDialog("Password must be at least 8 characters long with at least 1 special character.", false)
                return@setOnClickListener
            }

            val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
            currentUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    if (newEmail != currentUser.email) {
                        showDialog("Invalid current email.", false)
                        return@addOnCompleteListener
                    }

                    currentUser.updatePassword(newPassword).addOnCompleteListener { passTask ->
                        if (passTask.isSuccessful) {
                            showDialog("Password Updated!", true)
                        } else {
                            showDialog("Failed to update password.", false)
                        }
                    }
                } else {
                    showDialog("Reauthentication failed. Incorrect current password.", false)
                }
            }
        }

        // Add Account
        binding.addAccount.setOnClickListener {
            val newEmail = binding.newUsername.text.toString().trim()
            val newPassword = binding.newPassword.text.toString().trim()
            val currentPassword = binding.currentPassword.text.toString().trim()

            val currentUser = authentication.currentUser
            val adminEmail = currentUser?.email ?: ""

            if (newEmail.isEmpty() || newPassword.isEmpty() || currentPassword.isEmpty()) {
                showDialog("Fill in a new username, new password, and your current password to create an admin account.", false)
                return@setOnClickListener
            }

            if (!isValidPassword(newPassword)) {
                showDialog("Password must be at least 8 characters long with at least 1 special character.", false)
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                showDialog("Enter a valid email address.", false)
                return@setOnClickListener
            }

            val adminCredential = EmailAuthProvider.getCredential(adminEmail, currentPassword)
            currentUser?.reauthenticate(adminCredential)?.addOnCompleteListener { reauth ->
                if (reauth.isSuccessful) {
                    FirebaseAuth.getInstance().fetchSignInMethodsForEmail(newEmail)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val methods = task.result?.signInMethods
                                if (methods.isNullOrEmpty()) {
                                    FirebaseAuth.getInstance()
                                        .createUserWithEmailAndPassword(newEmail, newPassword)
                                        .addOnCompleteListener { createTask ->
                                            if (createTask.isSuccessful) {
                                                showDialog("New Admin Account Created!", true)
                                            } else {
                                                showDialog(
                                                    createTask.exception?.message
                                                        ?: "Failed to create account.", false
                                                )
                                            }
                                        }
                                } else {
                                    showDialog("Account already exists!", false)
                                }
                            } else {
                                showDialog("Failed to check email.", false)
                            }
                        }
                } else {
                    showDialog("Incorrect current admin password.", false)
                }
            }
        }

        binding.deleteAccount.setOnClickListener {
            val currentUser = authentication.currentUser
            val currentPassword = binding.currentPassword.text.toString().trim()

            if (currentUser == null) {
                showDialog("No logged-in user found.", false)
                return@setOnClickListener
            }

            if (currentPassword.isEmpty()) {
                showDialog("Enter your current password to delete your account.", false)
                return@setOnClickListener
            }

            val currentEmail = currentUser.email ?: ""
            if (currentEmail.isEmpty()) {
                showDialog("User email not found. Please log in again.", false)
                return@setOnClickListener
            }

            val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
            currentUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {

                    currentUser.delete().addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            showDialog("Account deleted successfully!", true)
                            startActivity(Intent(this, AdminLogin::class.java))
                            finish()
                        } else {
                            showDialog("Failed to delete account.", false)
                        }
                    }

                } else {
                    showDialog("Incorrect password. Account not deleted.", false)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)
    }

    private fun noTransition() {
        overridePendingTransition(0, 0)
    }

    private fun isValidPassword(password: String): Boolean {
        // minimum 8 characters, at least 1 special character
        val passwordPattern = Regex(
            "^(?=.*[!@#\$%^&*()_+\\[\\]{};':\"\\\\|,.<>/?\\-]).{8,}\$"
        )
        return passwordPattern.matches(password)
    }

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

        val title: TextView = layout.findViewById(R.id.title)
        val messageText: TextView = layout.findViewById(R.id.text)
        messageText.text = message

        title.text = if (isSuccess) "Success" else "Error"
        val okButton: TextView = layout.findViewById(R.id.ok)
        okButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}