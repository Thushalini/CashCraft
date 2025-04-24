package com.thushalini.cashcraft.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.thushalini.cashcraft.R
import org.json.JSONArray

class TransactionListActivity : ComponentActivity() {

//    private lateinit var recyclerView: RecyclerView
    private lateinit var spinner: Spinner
    private lateinit var adapter: TransactionAdapter
    private var transactionList = mutableListOf<Transaction>()
    private val categories = listOf("All", "Food", "Salary", "Transport", "Other") // Add more as needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_transaction)


//        recyclerView = findViewById(R.id.recyclerViewTransactions)
        spinner = findViewById(R.id.spinnerCategory)

        transactionList = loadTransactions()

        Log.d("TransactionListActivity", "Adapter set with list size: ${transactionList.size}")

        // Initialize RecyclerView
        val adapter = TransactionAdapter(this,transactionList, object : TransactionAdapter.OnTransactionActionListener {
            override fun onEdit(transaction: Transaction) {
                // Handle edit transaction
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDelete(transaction: Transaction) {
                // Remove transaction
                transactionList.remove(transaction)

                // Save updated list to SharedPreferences (convert to JSON)
                val sharedPref = getSharedPreferences("transactionList", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                val gson = Gson()
                val json = gson.toJson(transactionList)
                editor.putString("transactionList", json)
                editor.apply()

                // Update RecyclerView
                adapter.notifyDataSetChanged()

                Toast.makeText(this@TransactionListActivity, "Transaction is deleted", Toast.LENGTH_SHORT).show()
            }

        })

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTransactions)
        Log.d("TransactionListActivity", "RecyclerView: ${recyclerView}")

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        Log.d("TransactionList", transactionList.toString())

        if (transactionList.isNotEmpty()) {

            adapter.updateData(transactionList)
        } else {
            Log.d("TransactionList", "No transactions to display")
        }

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

        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val child = rv.findChildViewUnder(e.x, e.y)
                child?.let {
                    val position = rv.getChildAdapterPosition(it)
                    val selectedTransaction = transactionList[position]
                    val intent = Intent(this@TransactionListActivity, TransactionDetailActivity::class.java)
                    intent.putExtra("transactionList", selectedTransaction)
                    startActivity(intent)
                }
                return false
            }
        })
    }

    private fun loadTransactions(): MutableList<Transaction> {
        val sharedPref = getSharedPreferences("transactionList", Context.MODE_PRIVATE)
        val jsonString = sharedPref.getString("transactionList", null)

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

                transactionList.add(Transaction(id, title, amount, category, date, notes))

                if (id > maxId) {
                    maxId = id
                }
            }

            sharedPref.edit().putLong("last_transaction_id", maxId).apply()
            Log.d("TransactionList", "Transactions loaded: ${transactionList.size}")
//            Log.d("TransactionList", "Adapter data: $transactionList")
        } else {
            Log.d("TransactionList", "No transactions found in shared preferences.")
        }
        return transactionList
    }
}
