package com.thushalini.cashcraft.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import com.thushalini.cashcraft.R

class RegisterActivity : ComponentActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etRePassword: EditText
    private lateinit var checkTerms: CheckBox
    private lateinit var btnCreateAccount: Button
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // Initialize views
        etFullName = findViewById(R.id.etFullName)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etRePassword = findViewById(R.id.etRePassword)
        checkTerms = findViewById(R.id.checkTerms)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        tvLogin = findViewById(R.id.tvLogin)

        btnCreateAccount.setOnClickListener {
            registerUser()
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val fullName = etFullName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val rePassword = etRePassword.text.toString()

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty()
            || password.isEmpty() || rePassword.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != rePassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!checkTerms.isChecked) {
            Toast.makeText(this, "You must accept the Terms & Conditions.", Toast.LENGTH_SHORT).show()
            return
        }

        // Save to SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putString("fullName", fullName)
        editor.putString("username", username)
        editor.putString("email", email)
        editor.putString("password", password) // Consider encrypting in real apps
        editor.apply()

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show()

        // Go to Login or Profile
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
