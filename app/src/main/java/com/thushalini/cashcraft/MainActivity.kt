package com.thushalini.cashcraft

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.thushalini.cashcraft.ui.main.AddTransactionActivity
import com.thushalini.cashcraft.ui.main.TransactionListActivity

class MainActivity : ComponentActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var btnAddExpense: Button
    private lateinit var btnAddIncome: Button
    private lateinit var btnViewTransactions: Button

    private var balance: Double = 0.0
    private var monthlyBudget: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        tvBalance = findViewById(R.id.tvBalance)
        btnAddExpense = findViewById(R.id.btnAddExpense)
        btnAddIncome = findViewById(R.id.btnAddIncome)
        btnViewTransactions = findViewById(R.id.btnViewTransactions)

        // Load saved balance and monthly budget
        val sharedPreferences = getSharedPreferences("CashCraftPrefs", Context.MODE_PRIVATE)
        balance = sharedPreferences.getFloat("balance", 0.0f).toDouble()
        monthlyBudget = sharedPreferences.getFloat("monthly_budget", 0.0f).toDouble()

        updateBalanceText()

        btnAddExpense.setOnClickListener {
            openAddTransactionActivity("Expense")
        }

        btnAddIncome.setOnClickListener {
            openAddTransactionActivity("Income")
        }

        btnViewTransactions.setOnClickListener {
            Toast.makeText(this, "Opening Transactions", Toast.LENGTH_SHORT).show()
            Log.d("MainActivity", "Attempting to open TransactionListActivity") // Add this
            val intent = Intent(this, TransactionListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateBalanceText() {
        tvBalance.text = "₹%.2f".format(balance)
    }

    private fun openAddTransactionActivity(type: String) {
        val intent = Intent(this, AddTransactionActivity::class.java)
        intent.putExtra("transaction_type", type)
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            val amount = data.getDoubleExtra("amount", 0.0)
            val type = data.getStringExtra("type")

            balance = if (type == "Income") balance + amount else balance - amount
            updateBalanceText()

            // Save new balance and update SharedPreferences
            val sharedPreferences = getSharedPreferences("CashCraftPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putFloat("balance", balance.toFloat()).apply()
        }
    }
}
