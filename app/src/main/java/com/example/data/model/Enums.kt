package com.example.data.model

enum class PartyType {
    CUSTOMER,
    SUPPLIER
}

enum class TransactionType {
    SALE,
    PURCHASE,
    SALE_RETURN,
    PURCHASE_RETURN,
    RECOVERY, // Money received from customer
    PAYMENT   // Money paid to supplier
}

enum class PaymentMode {
    CASH,
    UDHAAR,
    WHATSAPP_CASH,
    MIXED
}

enum class DateFilter(val label: String, val urduLabel: String) {
    TODAY("Today", "Aaj"),
    YESTERDAY("Yesterday", "Kal"),
    THIS_MONTH("This Month", "Is Maah"),
    ALL_TIME("All Time", "Mukammal")
}

enum class StockFilter(val label: String, val urduLabel: String) {
    ALL("All Items", "Tamam Maal"),
    OUT_OF_STOCK("Out of Stock", "Khatam Shuda"),
    LOW_STOCK("Low Stock", "Kam Maal"),
    NEAR_EXPIRY("Near Expiry", "Qareeb Tareen Expiry")
}

enum class PosMode(val label: String, val urduLabel: String) {
    SALE("Sale Mode", "Farokht"),
    PURCHASE("Purchase Mode", "Khareedari"),
    RETURN("Return Mode", "Wapsi")
}
