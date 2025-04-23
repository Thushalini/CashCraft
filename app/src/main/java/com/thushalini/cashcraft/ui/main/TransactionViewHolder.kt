package com.thushalini.cashcraft.ui.main

import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thushalini.cashcraft.R

class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvTitle: TextView = view.findViewById(R.id.tvTitle)
    val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    val tvDate: TextView = view.findViewById(R.id.tvDate)
    val tvNotes: TextView = view.findViewById(R.id.tvNotes)
    val detailLayout: LinearLayout = view.findViewById(R.id.detailLayout)
    val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
    val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
}
