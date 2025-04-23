package com.thushalini.cashcraft.ui.main

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import com.thushalini.cashcraft.R
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : ComponentActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvDate: TextView
    private lateinit var btnSave: Button
    private var selectedDate: String = ""
    private var transactionType: String = ""
//    private var transactionId: String? = null // For storing the ID of the transaction being edited

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_transaction)

        transactionType = intent.getStringExtra("transaction_type") ?: ""

        // Get transaction ID if in edit mode
//        transactionId = intent.getStringExtra("transaction_id")

        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        tvDate = findViewById(R.id.tvDate)
        btnSave = findViewById(R.id.btnSaveTransaction)

        val categories = listOf("Food", "Transport", "Entertainment", "Salary", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(calendar.time)
        tvDate.text = selectedDate

        tvDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                val pickedCalendar = Calendar.getInstance()
                pickedCalendar.set(y, m, d)
                selectedDate = sdf.format(pickedCalendar.time)
                tvDate.text = selectedDate
            }, year, month, day).show()
        }

        // If we're editing an existing transaction, load its details
//        if (transactionId != null) {
//            loadTransactionDetails(transactionId!!)
//        }

        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString().trim()
            if (amountText.isEmpty()) {
                etAmount.error = "Enter amount"
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                etAmount.error = "Enter valid amount"
                return@setOnClickListener
            }

            sendTransactionNotification(transactionType, amount)

            val description = etDescription.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()

            val transaction = Transaction(
                id = System.currentTimeMillis(),
                title = transactionType,
                amount = amount,
                category = category,
                date = selectedDate,
                notes = description,
            )

            // Save or update the transaction
            saveTransaction(this, transaction)

            Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show()

            val resultIntent = Intent().apply {
                putExtra("amount", amount)
                putExtra("title", transactionType)
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

//    private fun loadTransactionDetails(transactionId: String) {
//        val sharedPref = getSharedPreferences("transactions", Context.MODE_PRIVATE)
//        val transactionList = sharedPref.getString("transaction_list", "[]")
//        val jsonArray = JSONArray(transactionList)
//
//        for (i in 0 until jsonArray.length()) {
//            val transactionJson = jsonArray.getJSONObject(i)
//            if (transactionJson.getString("id") == transactionId) {
//                etAmount.setText(transactionJson.getString("amount"))
//                etDescription.setText(transactionJson.getString("notes"))
//                val category = transactionJson.getString("category")
//                val categories = listOf("Food", "Transport", "Entertainment", "Salary", "Other")
//                val position = categories.indexOf(category)
//                spinnerCategory.setSelection(position)
//                selectedDate = transactionJson.getString("date")
//                tvDate.text = selectedDate
//                break
//            }
//        }
//    }

    private fun saveTransaction(context: Context, newTransaction: Transaction) {
        val sharedPref = context.getSharedPreferences("transactionList", Context.MODE_PRIVATE)
        val existing = sharedPref.getString("transaction_list", "[]")
        val jsonArray = JSONArray(existing)

        val jsonObj = JSONObject().apply {
            put("id", newTransaction.id.toString())
//            put("id", transactionId ?: System.currentTimeMillis().toString()) // Use transactionId if editing, or generate a new ID
            put("title", newTransaction.title)
            put("amount", newTransaction.amount)
            put("category", newTransaction.category)
            put("date", newTransaction.date)
            put("notes", newTransaction.notes)
        }

//        if (transactionId != null) {
//            // Edit existing transaction
//            for (i in 0 until jsonArray.length()) {
//                if (jsonArray.getJSONObject(i).getString("id") == transactionId) {
//                    jsonArray.put(i, jsonObj) // Update the transaction
//                    break
//                }
//            }
//        } else {
//            // Add new transaction
//            jsonArray.put(jsonObj)
//        }

        jsonArray.put(jsonObj)
        sharedPref.edit().putString("transaction_list", jsonArray.toString()).apply()
    }

    private fun sendTransactionNotification(type: String, amount: Double) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "transaction_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Transaction Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Transaction Saved")
            .setContentText("$type of ₹%.2f added.".format(amount))
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

