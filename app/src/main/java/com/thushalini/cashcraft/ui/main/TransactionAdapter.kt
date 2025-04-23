package com.thushalini.cashcraft.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thushalini.cashcraft.R

class TransactionAdapter(
    private val context: Context,
    private val transactionList: MutableList<Transaction>,
    private val listener: OnTransactionActionListener
) : RecyclerView.Adapter<TransactionViewHolder>() {

    interface OnTransactionActionListener {
        fun onEdit(transaction: Transaction)
        fun onDelete(transaction: Transaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.single_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]

        holder.tvTitle.text = transaction.title
        holder.tvAmount.text = "₹${transaction.amount}"
        holder.tvAmount.setTextColor(
            if (transaction.amount >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        )
        holder.tvCategory.text = "Category: ${transaction.category}"
        holder.tvDate.text = "Date: ${transaction.date}"
        holder.tvNotes.text = "Notes: ${transaction.notes}"

        holder.itemView.setOnClickListener {
            holder.detailLayout.visibility = if (holder.detailLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        holder.btnEdit.setOnClickListener {
            listener.onEdit(transaction)
        }
        holder.btnDelete.setOnClickListener {
            listener.onDelete(transaction)
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Transaction>) {
        transactionList.clear()
        transactionList.addAll(newData)
        notifyDataSetChanged()
    }
}

