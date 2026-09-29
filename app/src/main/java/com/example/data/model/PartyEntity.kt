package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: PartyType,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
