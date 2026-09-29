package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val barcode: String,
    val category: String = "General",
    val salePrice: Double,
    val wholesalePrice: Double = salePrice,
    val costPrice: Double,
    val stockQty: Double = 0.0,
    val lowStockThreshold: Double = 5.0,
    val unit: String = "piece", // piece, packet, kg, litre, carton
    val expiryDate: Long? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
