package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNo: String,
    val type: TransactionType,
    val partyId: Long? = null,
    val partyName: String = "Walk-in Customer",
    val partyPhone: String = "",
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
