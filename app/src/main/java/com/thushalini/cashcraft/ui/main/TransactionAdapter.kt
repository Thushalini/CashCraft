package com.thushalini.cashcraft.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.thushalini.cashcraft.R

class TransactionAdapter(
    private val context: Context,
    private val transactionList: MutableList<Transaction>,
    private val listener: OnTransactionActionListener
) : BaseAdapter() {

    interface OnTransactionActionListener {
        fun onEdit(transaction: Transaction)
        fun onDelete(transaction: Transaction)
    }

    override fun getCount(): Int = transactionList.size
    override fun getItem(position: Int): Any = transactionList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.single_transaction, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val transaction = transactionList[position]

        // Bind data
        viewHolder.tvTitle.text = transaction.title
        viewHolder.tvAmount.text = "₹${transaction.amount}"
        viewHolder.tvAmount.setTextColor(
            if (transaction.amount >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        )
        viewHolder.tvCategory.text = "Category: ${transaction.category}"
        viewHolder.tvDate.text = "Date: ${transaction.date}"
        viewHolder.tvNotes.text = "Notes: ${transaction.notes}"

        view.setOnClickListener {
            viewHolder.detailLayout.visibility =
                if (viewHolder.detailLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        viewHolder.btnEdit.setOnClickListener {
            listener.onEdit(transaction)
        }
        viewHolder.btnDelete.setOnClickListener {
            listener.onDelete(transaction)
        }

        return view
    }

    private class ViewHolder(view: View) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvNotes: TextView = view.findViewById(R.id.tvNotes)
        val detailLayout: LinearLayout = view.findViewById(R.id.detailLayout)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }


    fun updateData(newData: List<Transaction>) {
        transactionList.clear()
        transactionList.addAll(newData)
        notifyDataSetChanged()
    }
}
