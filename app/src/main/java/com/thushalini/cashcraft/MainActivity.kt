package com.thushalini.cashcraft

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thushalini.cashcraft.ui.main.AddTransactionActivity
import com.thushalini.cashcraft.ui.main.Transaction
import com.thushalini.cashcraft.ui.main.TransactionListActivity

class MainActivity : ComponentActivity() {

    private lateinit var tvBalance: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var btnAddExpense: Button
    private lateinit var btnAddIncome: Button
    private lateinit var btnViewTransactions: Button
    private lateinit var btnSetBudget: Button
    private lateinit var etMonthlyBudget: EditText  // EditText for Monthly Budget

    private var balance: Double = 0.0
    private var monthlyBudget: Double = 50000.0

    private lateinit var addTransactionLauncher: ActivityResultLauncher<Intent>
    private lateinit var transactionList: MutableList<Transaction>

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // Initialize views
        tvBalance = findViewById(R.id.tvBalance)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        btnAddExpense = findViewById(R.id.btnAddExpense)
        btnAddIncome = findViewById(R.id.btnAddIncome)
        btnViewTransactions = findViewById(R.id.btnViewTransactions)
        btnSetBudget = findViewById(R.id.btnSetBudget)
        etMonthlyBudget = findViewById(R.id.etMonthlyBudget)  // EditText for monthly budget

        // Initialize the transaction list
        transactionList = mutableListOf()

        // Load saved balance and transactions
        loadTransactions()

        // Update balance and transaction summary
        updateSummary()

        // Load saved balance and monthly budget from SharedPreferences
        val sharedPreferences = getSharedPreferences("transactionList", MODE_PRIVATE)
//        balance = sharedPreferences.getFloat("balance", 0.0f).toDouble()
        monthlyBudget = sharedPreferences.getFloat("monthly_budget", 50000.0f).toDouble()

        updateBalanceText()

        // Set the monthly budget in EditText
        etMonthlyBudget.setText(monthlyBudget.toString())

        if (etMonthlyBudget.text.toString().isNotEmpty()) {
            monthlyBudget = etMonthlyBudget.text.toString().toDouble()
            saveMonthlyBudget()
            updateSummary()
        } else {
            etMonthlyBudget.setText(monthlyBudget.toString())
        }

        btnSetBudget.setOnClickListener {
            val budgetText = etMonthlyBudget.text.toString()
            if (budgetText.isNotEmpty()) {
                try {
                    monthlyBudget = budgetText.toDouble()
                    saveMonthlyBudget()
                    updateSummary()
                } catch (e: NumberFormatException) {
                    Log.e("MainActivity", "Invalid budget input")
                }
            }
        }

        // Register ActivityResultLauncher
        addTransactionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val data = result.data!!
                val amount = data.getDoubleExtra("amount", 0.0)
                val type = data.getStringExtra("title")

                // Update balance based on type (Income or Expense)
                balance = if (type == "Income") balance + amount else balance - amount
                updateBalanceText()

                // Save new balance to SharedPreferences
//                sharedPreferences.edit().putFloat("balance", balance.toFloat()).apply()

                // Create and add new transaction
                val transaction = Transaction(
                    id = System.currentTimeMillis(),
                    title = type ?: "Unknown",
                    amount = amount,
                    category = data.getStringExtra("category") ?: "",
                    date = data.getStringExtra("date") ?: "",
                    notes = data.getStringExtra("notes") ?: ""
                )

                transactionList.add(transaction)
                saveTransactions() // Save updated transactions list
            }
        }

        // Button click listeners
        btnAddExpense.setOnClickListener { openAddTransactionActivity("Expense") }
        btnAddIncome.setOnClickListener { openAddTransactionActivity("Income") }
        btnViewTransactions.setOnClickListener {
            val intent = Intent(this, TransactionListActivity::class.java)
            startActivity(intent)
        }

        // Save Monthly Budget to SharedPreferences when updated
        etMonthlyBudget.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                try {
                    monthlyBudget = etMonthlyBudget.text.toString().toDouble()
                    saveMonthlyBudget()
                    updateSummary()
                } catch (e: NumberFormatException) {
                    Log.e("MainActivity", "Invalid budget input")
                }
            }
        }
    }

    private fun loadTransactions() {
        val sharedPref = getSharedPreferences("transactionList", Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString("transaction_list", null)

        if (!jsonString.isNullOrEmpty()) {
            try {
                val gson = Gson()
                val transactionListType = object : TypeToken<List<Transaction>>() {}.type
                transactionList = gson.fromJson(jsonString, transactionListType)
                Log.d("TransactionList", "Transactions loaded in main: ${transactionList.size}")
            } catch (e: Exception) {
                Log.e("TransactionList", "Error loading transactions: ${e.message}")
            }
        } else {
            Log.d("TransactionList", "No transactions found in shared preferences.")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSummary() {
        var totalIncome = 0.0
        var totalExpense = 0.0

        for (transaction in transactionList) {
            if (transaction.title == "Income") {
                totalIncome += transaction.amount
            } else {
                totalExpense += transaction.amount
            }
        }

        balance = totalIncome - totalExpense
        if (balance < 0) {
            tvBalance.setTextColor(getColor(R.color.red))
        }
        updateBalanceText()

        tvTotalIncome.text = "₹%.2f".format(totalIncome)
        tvTotalExpense.text = "₹%.2f".format(totalExpense)
        tvBalance.text = "₹%.2f".format(balance)

//        val remainingBudget = monthlyBudget - totalExpense
//        tvTotalExpense.text = "₹%.2f (Spent)Remaining Budget: ₹%.2f".format(totalExpense, remainingBudget)

//        tvBalance.text = "₹%.2f".format(balance)
        val remainingBudget = monthlyBudget - totalExpense
        val tvRemainingBudget: TextView = findViewById(R.id.tvRemainingBudget)
        tvRemainingBudget.text = "Remaining Budget: ₹%.2f".format(remainingBudget)

        if (remainingBudget < 0) {
            tvRemainingBudget.setTextColor(getColor(R.color.red))

            // Show alert to customer
            AlertDialog.Builder(this)
                .setTitle("Budget Exceeded!")
                .setMessage("Your expenses have exceeded your budget. Please review your spending.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()

        } else {
            tvRemainingBudget.setTextColor(getColor(R.color.green))
        }


        tvTotalExpense.text = "₹%.2f (Spent)".format(totalExpense)
        tvBalance.text = "₹%.2f (Balance)".format(balance)
        Log.d("Budget", "Remaining Budget: ₹${remainingBudget}")
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

    // Save transactions to SharedPreferences
    private fun saveTransactions() {
        val sharedPreferences = getSharedPreferences("transactionList", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val transactionsJson = gson.toJson(transactionList)
        editor.putString("transaction_list", transactionsJson)
        editor.apply()
    }

    // Save the monthly budget to SharedPreferences
    private fun saveMonthlyBudget() {
        val sharedPreferences = getSharedPreferences("transactionList", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("monthly_budget", monthlyBudget.toFloat())
        editor.apply()
    }
}
