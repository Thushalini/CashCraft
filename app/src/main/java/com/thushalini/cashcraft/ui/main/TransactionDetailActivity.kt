package com.thushalini.cashcraft.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import com.thushalini.cashcraft.R
import org.json.JSONArray

class TransactionDetailActivity : ComponentActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvAmount: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvNotes: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var transaction: Transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_detail)

        // Initialize views
        tvTitle = findViewById(R.id.tvTitle)
        tvAmount = findViewById(R.id.tvAmount)
        tvCategory = findViewById(R.id.tvCategory)
        tvDate = findViewById(R.id.tvDate)
        tvNotes = findViewById(R.id.tvNotes)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        // Get transaction data from intent
        transaction = intent.getSerializableExtra("transaction") as Transaction

        // Display the transaction details
        displayTransactionDetails()

        // Set click listener for Edit button
        btnEdit.setOnClickListener {
            // Start EditTransactionActivity to edit this transaction
            val intent = Intent(this, EditTransactionActivity::class.java)
            intent.putExtra("transaction", transaction)
            startActivityForResult(intent, 200)
        }

        // Set click listener for Delete button
        btnDelete.setOnClickListener {
            deleteTransaction(transaction)
        }
    }

    private fun displayTransactionDetails() {
        tvTitle.text = transaction.title
        tvAmount.text = "₹%.2f".format(transaction.amount)
        tvCategory.text = "Category: ${transaction.category}"
        tvDate.text = "Date: ${transaction.date}"
        tvNotes.text = "Notes: ${transaction.notes}"
    }

    private fun deleteTransaction(transaction: Transaction) {
        // Remove transaction from SharedPreferences
        val sharedPref = getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString("transaction_list", "[]")
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getString("id") == transaction.id.toString()) {
                jsonArray.remove(i)
                break
            }
        }

        // Save the updated list back to SharedPreferences
        sharedPref.edit().putString("transaction_list", jsonArray.toString()).apply()

        // Show a toast and finish the activity
        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
        finish()
    }

    // Handle result from EditTransactionActivity (if edited)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            val updatedTransaction = data.getSerializableExtra("updated_transaction") as Transaction
            transaction = updatedTransaction
            displayTransactionDetails()

            // Update the transaction in SharedPreferences
            updateTransactionInPrefs(updatedTransaction)
        }
    }

    private fun updateTransactionInPrefs(updatedTransaction: Transaction) {
        val sharedPref = getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString("transaction_list", "[]")
        val jsonArray = JSONArray(jsonString)

        // Find and update the transaction in the list
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getString("id") == updatedTransaction.id.toString()) {
                obj.put("title", updatedTransaction.title)
                obj.put("amount", updatedTransaction.amount)
                obj.put("category", updatedTransaction.category)
                obj.put("date", updatedTransaction.date)
                obj.put("notes", updatedTransaction.notes)
                break
            }
        }

        // Save the updated list back to SharedPreferences
        sharedPref.edit().putString("transaction_list", jsonArray.toString()).apply()

        // Notify user
        Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
    }
}
