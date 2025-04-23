package com.thushalini.cashcraft.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
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

    // Registering the ActivityResultLauncher
    private val editTransactionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val updatedTransaction = data.getSerializableExtra("updated_transaction") as? Transaction
                    if (updatedTransaction != null) {
                        transaction = updatedTransaction
                        displayTransactionDetails()
                        updateTransactionInPrefs(updatedTransaction)
                    }
                }
            }
        }

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
        val passedTransaction = intent.getSerializableExtra("transaction") as? Transaction
        if (passedTransaction == null) {
            Toast.makeText(this, "Transaction not found!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        transaction = passedTransaction

        // Display the transaction details
        displayTransactionDetails()

        // Set click listener for Edit button
        btnEdit.setOnClickListener {
            val intent = Intent(this, EditTransactionActivity::class.java)
            intent.putExtra("transaction", transaction)
            editTransactionLauncher.launch(intent)
        }

        // Set click listener for Delete button
        btnDelete.setOnClickListener {
            deleteTransaction(transaction)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayTransactionDetails() {
        tvTitle.text = transaction.title
        tvAmount.text = "₹%.2f".format(transaction.amount)
        tvCategory.text = "Category: ${transaction.category}"
        tvDate.text = "Date: ${transaction.date}"
        tvNotes.text = "Notes: ${transaction.notes}"
    }

    private fun deleteTransaction(transaction: Transaction) {
        val sharedPref = getSharedPreferences("transactionList", Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString("transaction_list", "[]")
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getString("id") == transaction.id.toString()) {
                jsonArray.remove(i)
                break
            }
        }

        sharedPref.edit().putString("transaction_list", jsonArray.toString()).apply()
        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateTransactionInPrefs(updatedTransaction: Transaction) {
        val sharedPref = getSharedPreferences("transactionList", Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString("transaction_list", "[]")
        val jsonArray = JSONArray(jsonString)

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

        sharedPref.edit().putString("transaction_list", jsonArray.toString()).apply()
        Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
    }
}
