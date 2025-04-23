package com.thushalini.cashcraft

import android.annotation.SuppressLint
import android.app.AlertDialog
//import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thushalini.cashcraft.ui.main.AddTransactionActivity
import com.thushalini.cashcraft.ui.main.Transaction
import com.thushalini.cashcraft.ui.main.TransactionListActivity
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import com.thushalini.cashcraft.ui.main.EditTransactionActivity
import com.thushalini.cashcraft.ui.main.TransactionAdapter


class MainActivity : ComponentActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var btnAddExpense: Button
    private lateinit var btnAddIncome: Button
    private lateinit var btnViewTransactions: Button

    private var balance: Double = 0.0
    private var monthlyBudget: Double = 0.0

    private lateinit var addTransactionLauncher: ActivityResultLauncher<Intent>
    private lateinit var transactionList: MutableList<Transaction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        tvBalance = findViewById(R.id.tvBalance)
        btnAddExpense = findViewById(R.id.btnAddExpense)
        btnAddIncome = findViewById(R.id.btnAddIncome)
        btnViewTransactions = findViewById(R.id.btnViewTransactions)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewTransactions)


        loadTransactions()

        // Declare the adapter first
        val adapter = TransactionAdapter(this@MainActivity, transactionList, object : TransactionAdapter.OnTransactionActionListener {
            override fun onEdit(transaction: Transaction) {
                val intent = Intent(this@MainActivity, EditTransactionActivity::class.java)
                intent.putExtra("transaction_id", transaction.id)
                startActivity(intent)
            }

            override fun onDelete(transaction: Transaction) {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Remove the transaction and notify the adapter
                        transactionList.remove(transaction)
//                        adapter.notifyDataSetChanged() // Now this works since adapter is in scope
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
    })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Load saved balance and monthly budget
        val sharedPreferences = getSharedPreferences("CashCraftPrefs", MODE_PRIVATE)
        balance = sharedPreferences.getFloat("balance", 0.0f).toDouble()
        monthlyBudget = sharedPreferences.getFloat("monthly_budget", 0.0f).toDouble()

        updateBalanceText()

        // Register ActivityResultLauncher
        addTransactionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val data = result.data!!
                val amount = data.getDoubleExtra("amount", 0.0)
                val type = data.getStringExtra("title")

                balance = if (type == "Income") balance + amount else balance - amount
                updateBalanceText()

                // Save new balance to SharedPreferences
//                val sharedPreferences = getSharedPreferences("CashCraftPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putFloat("balance", balance.toFloat()).apply()
//
//                // Add new transaction and save
//                val transaction = Transaction(title = type, amount = amount, category = "", date = "", notes = "")
//                transactionList.add(transaction)
//                saveTransactions()
            }
        }

        btnAddExpense.setOnClickListener {
            openAddTransactionActivity("Expense")
        }

        btnAddIncome.setOnClickListener {
            openAddTransactionActivity("Income")
        }

        btnViewTransactions.setOnClickListener {
            Toast.makeText(this, "Opening Transactions", Toast.LENGTH_SHORT).show()
            Log.d("MainActivity", "Attempting to open TransactionListActivity")
            val intent = Intent(this, TransactionListActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateBalanceText() {
        tvBalance.text = "₹%.2f".format(balance)
    }

    private fun openAddTransactionActivity(type: String) {
        val intent = Intent(this, AddTransactionActivity::class.java)
        intent.putExtra("transaction_type", type)
        addTransactionLauncher.launch(intent)
    }

    // Load transactions from SharedPreferences
    private fun loadTransactions() {
        val sharedPreferences = getSharedPreferences("CashCraftPrefs", MODE_PRIVATE)
        val transactionsJson = sharedPreferences.getString("transactions", null)
        if (!transactionsJson.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            transactionList = gson.fromJson(transactionsJson, type)
        } else {
            transactionList = mutableListOf()
        }
    }

    // Save transactions to SharedPreferences
//    private fun saveTransactions() {
//        val sharedPreferences = getSharedPreferences("CashCraftPrefs", MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        val gson = Gson()
//        val transactionsJson = gson.toJson(transactionList)
//        editor.putString("transactions", transactionsJson)
//        editor.apply()
//    }
}
