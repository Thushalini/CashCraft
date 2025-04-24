package com.thushalini.cashcraft.ui.main

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.thushalini.cashcraft.R
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent

class AddTransactionActivity : ComponentActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvDate: TextView
    private lateinit var btnSave: Button
    private var selectedDate: String = ""
    private var transactionType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_transaction)

        transactionType = intent.getStringExtra("transaction_type") ?: ""

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

            val id = System.currentTimeMillis()
            val transaction = Transaction(
                id = id,
                title = transactionType,
                amount = amount,
                category = category,
                date = selectedDate,
                notes = description
            )

            // Save the transaction
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

    private fun saveTransaction(context: Context, newTransaction: Transaction) {
        val sharedPref = context.getSharedPreferences("transactionList", Context.MODE_PRIVATE)
        val existing = sharedPref.getString("transaction_list", "[]")
        val jsonArray = JSONArray(existing)

        val jsonObj = JSONObject().apply {
            put("id", newTransaction.id.toString())
            put("title", newTransaction.title)
            put("amount", newTransaction.amount)
            put("category", newTransaction.category)
            put("date", newTransaction.date)
            put("notes", newTransaction.notes)
        }

        // Add the new transaction to the list
        jsonArray.put(jsonObj)

        // Save the updated list to SharedPreferences
        sharedPref.edit().putString("transaction_list", jsonArray.toString()).apply()
    }

    private fun sendTransactionNotification(type: String, amount: Double) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "transaction_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Transaction Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
            .setContentTitle("New $type")
            .setContentText("₹$amount has been added.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(Random().nextInt(), notification)
    }
}
