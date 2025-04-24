package com.thushalini.cashcraft.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.thushalini.cashcraft.R

class TransactionAdapter(
    private val context: Context,
    private var transactionList: List<Transaction>,
    private val listener: OnTransactionActionListener
) : RecyclerView.Adapter<TransactionViewHolder>() {

    interface OnTransactionActionListener {
        fun onEdit(transaction: Transaction)
        fun onDelete(transaction: Transaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        Log.d("AdapterDebug", "onCreateViewHolder called")
        val view = LayoutInflater.from(context).inflate(R.layout.single_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        Log.d("AdapterDebug", "onBindViewHolder called for position $position")
        val transaction = transactionList[position]

        holder.itemView.setBackgroundColor(Color.LTGRAY)
        Log.d("TransactionAdapter", "Binding transaction at position $position: $transaction")

        holder.tvTitle.text = transaction.title
        holder.tvCategory.text = "Category: ${transaction.category}"
        holder.tvDate.text = "Date: ${transaction.date}"
        holder.tvNotes.text = "Notes: ${transaction.notes}"

        // Determine color based on title
        val lowerTitle = transaction.title.lowercase()
        val isIncome = lowerTitle.contains("income")
        val isExpense = lowerTitle.contains("expense")

        holder.tvAmount.text = "₹${transaction.amount}"
        holder.tvAmount.setTextColor(
            when {
                isIncome -> Color.parseColor("#4CAF50") // Green
                isExpense -> Color.parseColor("#F44336") // Red
                else -> Color.BLACK // Default fallback
            }
        )

        // Handle expansion state
        holder.detailLayout.visibility = if (transaction.isExpanded) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            transaction.isExpanded = !transaction.isExpanded
            notifyItemChanged(position)
        }

        holder.btnEdit.setOnClickListener {
            listener.onEdit(transaction)
        }
        holder.btnDelete.setOnClickListener {
            listener.onDelete(transaction)
        }
    }



    override fun getItemCount(): Int {
        Log.d("Adapter", "Returning item count: ${transactionList.size}")
        return transactionList.size
    }

//    @SuppressLint("NotifyDataSetChanged")
//    fun updateData(newData: List<Transaction>) {
//        transactionList.clear()
//        transactionList.addAll(newData.toMutableList())
//        notifyDataSetChanged()
//    }
fun updateData(newData: List<Transaction>) {
    val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun getOldListSize() = transactionList.size
        override fun getNewListSize() = newData.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return transactionList[oldItemPosition].id == newData[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return transactionList[oldItemPosition] == newData[newItemPosition]
        }
    })
    transactionList = newData
    diffResult.dispatchUpdatesTo(this)
}

}

