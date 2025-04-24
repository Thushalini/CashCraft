package com.thushalini.cashcraft.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.thushalini.cashcraft.R

class EditTransactionActivity : ComponentActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvDate: TextView
    private lateinit var btnSave: Button
//    private lateinit var transaction: Transaction

    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_transaction)

        val transaction = intent.getSerializableExtra("transaction", Transaction::class.java)
//        transaction = intent.getSerializableExtra("transaction") as Transaction

        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        tvDate = findViewById(R.id.tvDate)
        btnSave = findViewById(R.id.btnSave)

        val categories = listOf("Food", "Transport", "Entertainment", "Salary", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter

        if (transaction != null) {
            etAmount.setText(transaction.amount.toString())
        }
        if (transaction != null) {
            etDescription.setText(transaction.notes)
        }
        if (transaction != null) {
            spinnerCategory.setSelection(categories.indexOf(transaction.category))
        }

        btnSave.setOnClickListener {
            val updatedAmount = etAmount.text.toString().toDoubleOrNull()
            val updatedDescription = etDescription.text.toString()
            val updatedCategory = spinnerCategory.selectedItem.toString()

            if (updatedAmount != null) {
                if (transaction != null) {
                    transaction.amount = updatedAmount
                }
                if (transaction != null) {
                    transaction.notes = updatedDescription
                }
                if (transaction != null) {
                    transaction.category = updatedCategory
                }

                val resultIntent = Intent()
                resultIntent.putExtra("updated_transaction", transaction)
                if (transaction != null) {
                    Log.d("EditTransaction", "Updated Transaction: ${transaction.amount}, ${transaction.notes}, ${transaction.category}")
                }

                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
