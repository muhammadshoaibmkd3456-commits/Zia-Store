package com.example.data.model

data class CartItem(
    val product: ProductEntity,
    var qty: Double = 1.0,
    var customPrice: Double = product.salePrice
) {
    val lineTotal: Double
        get() = qty * customPrice

    val costTotal: Double
        get() = qty * product.costPrice
}
