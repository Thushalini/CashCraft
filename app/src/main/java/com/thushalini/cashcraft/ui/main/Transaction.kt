package com.thushalini.cashcraft.ui.main

import java.io.Serializable

data class Transaction(
    val id: Long,
    val title: String,
    var amount: Double,
    var category: String,
    val date: String,
    var notes: String,
) : Serializable
