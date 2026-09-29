package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_profile")
data class StoreProfile(
    @PrimaryKey val id: Int = 1,
    val storeName: String = "AL-MAKKAH TRADERS",
    val ownerName: String = "Muhammad Zia",
    val phone: String = "0300-7654321",
    val address: String = "Shop #14, Main Bazaar, Lahore",
    val paymentDetails: String = "JazzCash / EasyPaisa: 0300-7654321 | Bank Alfalah: 0142-10048291",
    val tagline: String = "Apni dukan ko behtar banayein",
    val isPinEnabled: Boolean = false,
    val pinCode: String = "",
    val showRomanUrdu: Boolean = true,
    val wholesaleMode: Boolean = false,
    val whatsappBilling: Boolean = true,
    val printPreview: Boolean = true,
    val subscriptionPlan: String = "Pro Lifetime Store",
    val subscriptionExpiry: String = "31 Dec 2029"
)
