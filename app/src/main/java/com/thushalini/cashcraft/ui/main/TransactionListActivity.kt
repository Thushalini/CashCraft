package com.thushalini.cashcraft.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.thushalini.cashcraft.R
import org.json.JSONArray

class TransactionListActivity : ComponentActivity() {

    private lateinit var listView: ListView
    private lateinit var spinner: Spinner
    private lateinit var adapter: TransactionAdapter
    private val transactionList = mutableListOf<Transaction>()
    private val categories = listOf("All", "Food", "Salary", "Transport", "Other") // Add more as needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_transaction)

        listView = findViewById(R.id.listViewTransactions)
        spinner = findViewById(R.id.spinnerCategory)

        loadTransactions()

//        adapter = TransactionAdapter(this, transactionList)
        adapter = TransactionAdapter(this, transactionList, object : TransactionAdapter.OnTransactionActionListener {
            override fun onEdit(transaction: Transaction) {
                // Handle edit transaction
            }

            override fun onDelete(transaction: Transaction) {
                // Handle delete transaction
            }
        })
        listView.adapter = adapter

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                val filteredTransactions = if (selectedCategory == "All") {
                    transactionList
                } else {
                    transactionList.filter { it.category == selectedCategory }
                }
                adapter.updateData(filteredTransactions)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedTransaction = adapter.getItem(position) as Transaction
            val intent = Intent(this, TransactionDetailActivity::class.java)
            intent.putExtra("transaction", selectedTransaction)
            startActivity(intent)
        }
    }

    private fun loadTransactions() {
        val sharedPref = getSharedPreferences("transactionList", Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString("transaction_list", null)

        if (!jsonString.isNullOrEmpty()) {
            val jsonArray = JSONArray(jsonString)
            var maxId = 0L

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getLong("id")
                val title = obj.getString("title")
                val amount = obj.getDouble("amount")
                val category = obj.getString("category")
                val date = obj.getString("date")
                val notes = obj.getString("notes")
                val type = obj.getString("type")

                transactionList.add(Transaction(id, title, amount, category, date, notes, type))

                if (id > maxId) {
                    maxId = id
                }
            }

            sharedPref.edit().putLong("last_transaction_id", maxId).apply()
            Log.d("TransactionList", "Transactions loaded: ${transactionList.size}")
        } else {
            Log.d("TransactionList", "No transactions found in shared preferences.")
        }
    }

}
