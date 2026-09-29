package com.example.data.repository

import com.example.data.local.ExpenseDao
import com.example.data.local.PartyDao
import com.example.data.local.ProductDao
import com.example.data.local.StoreProfileDao
import com.example.data.local.TransactionDao
import com.example.data.model.CartItem
import com.example.data.model.ExpenseEntity
import com.example.data.model.PartyEntity
import com.example.data.model.PartyType
import com.example.data.model.PaymentMode
import com.example.data.model.ProductEntity
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class StoreRepository(
    private val storeProfileDao: StoreProfileDao,
    private val productDao: ProductDao,
    private val partyDao: PartyDao,
    private val transactionDao: TransactionDao,
    private val expenseDao: ExpenseDao
) {
    val storeProfile: Flow<StoreProfile?> = storeProfileDao.getStoreProfile()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val outOfStockProducts: Flow<List<ProductEntity>> = productDao.getOutOfStockProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val allCategories: Flow<List<String>> = productDao.getAllCategories()
    val customers: Flow<List<PartyEntity>> = partyDao.getPartiesByType(PartyType.CUSTOMER)
    val suppliers: Flow<List<PartyEntity>> = partyDao.getPartiesByType(PartyType.SUPPLIER)
    val allParties: Flow<List<PartyEntity>> = partyDao.getAllParties()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allTransactionItems: Flow<List<TransactionItemEntity>> = transactionDao.getAllTransactionItems()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getNearExpiryProducts(thresholdTime: Long): Flow<List<ProductEntity>> {
        return productDao.getNearExpiryProducts(thresholdTime)
    }

    fun getPartyTransactions(partyId: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByParty(partyId)
    }

    fun getPartyByIdFlow(partyId: Long): Flow<PartyEntity?> {
        return partyDao.getPartyByIdFlow(partyId)
    }

    suspend fun getStoreProfileDirect(): StoreProfile {
        return storeProfileDao.getStoreProfileDirect() ?: StoreProfile()
    }

    suspend fun saveStoreProfile(profile: StoreProfile) {
        storeProfileDao.insertOrUpdate(profile)
    }

    suspend fun saveProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }

    suspend fun findProductByBarcode(barcode: String): ProductEntity? {
        return productDao.getProductByBarcode(barcode)
    }

    suspend fun checkBarcodeExists(barcode: String, excludeId: Long = 0): Boolean {
        val existing = productDao.findAnyByBarcode(barcode) ?: return false
        return existing.id != excludeId
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    suspend fun archiveProduct(id: Long) {
        productDao.archiveProduct(id)
    }

    suspend fun saveParty(party: PartyEntity): Long {
        return partyDao.insertParty(party)
    }

    suspend fun deleteParty(party: PartyEntity) {
        partyDao.deleteParty(party)
    }

    suspend fun saveExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    // Complete Checkout Flow for Sale
    suspend fun processSale(
        cartItems: List<CartItem>,
        party: PartyEntity?,
        walkInPhone: String,
        discountAmount: Double,
        paidAmount: Double,
        paymentMode: PaymentMode,
        notes: String = ""
    ): TransactionEntity {
        val subtotal = cartItems.sumOf { it.lineTotal }
        val netTotal = (subtotal - discountAmount).coerceAtLeast(0.0)
        val invoiceNumber = generateInvoiceNumber()

        val transaction = TransactionEntity(
            invoiceNo = invoiceNumber,
            type = TransactionType.SALE,
            partyId = party?.id,
            partyName = party?.name ?: if (walkInPhone.isNotEmpty()) "Walk-in ($walkInPhone)" else "Walk-in Customer",
            partyPhone = party?.phone ?: walkInPhone,
            subtotal = subtotal,
            discountAmount = discountAmount,
            totalAmount = netTotal,
            paidAmount = paidAmount,
            paymentMode = paymentMode,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )

        val txId = transactionDao.insertTransaction(transaction)

        val lineItems = cartItems.map {
            TransactionItemEntity(
                transactionId = txId,
                productId = it.product.id,
                productName = it.product.name,
                unit = it.product.unit,
                qty = it.qty,
                unitPrice = it.customPrice,
                costAtSale = it.product.costPrice
            )
        }
        transactionDao.insertTransactionItems(lineItems)

        // Reduce stock
        for (item in cartItems) {
            productDao.adjustStock(item.product.id, -item.qty)
        }

        // If Udhaar or partial payment, update customer's balance
        if (party != null) {
            val unpaidCredit = netTotal - paidAmount
            if (unpaidCredit > 0.0) {
                // Customer owes more
                partyDao.adjustPartyBalance(party.id, unpaidCredit)
            }
        }

        return transaction.copy(id = txId)
    }

    // Process Purchase from Supplier
    suspend fun processPurchase(
        cartItems: List<CartItem>,
        supplier: PartyEntity?,
        supplierName: String,
        supplierPhone: String,
        paidAmount: Double,
        paymentMode: PaymentMode,
        notes: String = ""
    ): TransactionEntity {
        val subtotal = cartItems.sumOf { it.lineTotal }
        val invoiceNumber = generateInvoiceNumber(prefix = "PUR-")

        val transaction = TransactionEntity(
            invoiceNo = invoiceNumber,
            type = TransactionType.PURCHASE,
            partyId = supplier?.id,
            partyName = supplier?.name ?: supplierName.ifEmpty { "Cash Supplier" },
            partyPhone = supplier?.phone ?: supplierPhone,
            subtotal = subtotal,
            discountAmount = 0.0,
            totalAmount = subtotal,
            paidAmount = paidAmount,
            paymentMode = paymentMode,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )

        val txId = transactionDao.insertTransaction(transaction)

        val lineItems = cartItems.map {
            TransactionItemEntity(
                transactionId = txId,
                productId = it.product.id,
                productName = it.product.name,
                unit = it.product.unit,
                qty = it.qty,
                unitPrice = it.customPrice,
                costAtSale = it.customPrice
            )
        }
        transactionDao.insertTransactionItems(lineItems)

        // Increase inventory
        for (item in cartItems) {
            productDao.adjustStock(item.product.id, item.qty)
        }

        // If credit purchase, increase payable to supplier (negative balance)
        if (supplier != null) {
            val unpaidPayable = subtotal - paidAmount
            if (unpaidPayable > 0.0) {
                // We owe supplier more (supplier balance becomes more negative)
                partyDao.adjustPartyBalance(supplier.id, -unpaidPayable)
            }
        }

        return transaction.copy(id = txId)
    }

    // Process Return Mode
    suspend fun processReturn(
        cartItems: List<CartItem>,
        party: PartyEntity?,
        refundAmount: Double,
        isCustomerReturn: Boolean,
        notes: String = ""
    ): TransactionEntity {
        val subtotal = cartItems.sumOf { it.lineTotal }
        val invoiceNumber = generateInvoiceNumber(prefix = if (isCustomerReturn) "RET-" else "PRET-")

        val transaction = TransactionEntity(
            invoiceNo = invoiceNumber,
            type = if (isCustomerReturn) TransactionType.SALE_RETURN else TransactionType.PURCHASE_RETURN,
            partyId = party?.id,
            partyName = party?.name ?: "Walk-in Return",
            partyPhone = party?.phone ?: "",
            subtotal = subtotal,
            discountAmount = 0.0,
            totalAmount = subtotal,
            paidAmount = refundAmount,
            paymentMode = PaymentMode.CASH,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )

        val txId = transactionDao.insertTransaction(transaction)

        val lineItems = cartItems.map {
            TransactionItemEntity(
                transactionId = txId,
                productId = it.product.id,
                productName = it.product.name,
                unit = it.product.unit,
                qty = it.qty,
                unitPrice = it.customPrice,
                costAtSale = it.product.costPrice
            )
        }
        transactionDao.insertTransactionItems(lineItems)

        if (isCustomerReturn) {
            // Customer returned goods to store -> inventory increases
            for (item in cartItems) {
                productDao.adjustStock(item.product.id, item.qty)
            }
            if (party != null && refundAmount < subtotal) {
                // Udhaar reduced
                val balanceReduction = subtotal - refundAmount
                partyDao.adjustPartyBalance(party.id, -balanceReduction)
            }
        } else {
            // Store returned goods to supplier -> inventory decreases
            for (item in cartItems) {
                productDao.adjustStock(item.product.id, -item.qty)
            }
            if (party != null && refundAmount < subtotal) {
                val payableReduction = subtotal - refundAmount
                partyDao.adjustPartyBalance(party.id, payableReduction)
            }
        }

        return transaction.copy(id = txId)
    }

    // "Maine Liye" (Record Recovery from Customer)
    suspend fun recordRecovery(
        party: PartyEntity,
        amount: Double,
        notes: String = ""
    ): TransactionEntity {
        val invoiceNo = generateInvoiceNumber(prefix = "REC-")
        val transaction = TransactionEntity(
            invoiceNo = invoiceNo,
            type = TransactionType.RECOVERY,
            partyId = party.id,
            partyName = party.name,
            partyPhone = party.phone,
            subtotal = amount,
            totalAmount = amount,
            paidAmount = amount,
            paymentMode = PaymentMode.CASH,
            notes = notes.ifEmpty { "Wasooli (Recovery)" },
            timestamp = System.currentTimeMillis()
        )
        val id = transactionDao.insertTransaction(transaction)
        // Customer owed us, now pays -> balance decreases
        partyDao.adjustPartyBalance(party.id, -amount)
        return transaction.copy(id = id)
    }

    // "Maine Diye" (Record Payment to Supplier or Credit given to Customer)
    suspend fun recordPayment(
        party: PartyEntity,
        amount: Double,
        notes: String = ""
    ): TransactionEntity {
        val invoiceNo = generateInvoiceNumber(prefix = "PAY-")
        val transaction = TransactionEntity(
            invoiceNo = invoiceNo,
            type = TransactionType.PAYMENT,
            partyId = party.id,
            partyName = party.name,
            partyPhone = party.phone,
            subtotal = amount,
            totalAmount = amount,
            paidAmount = amount,
            paymentMode = PaymentMode.CASH,
            notes = notes.ifEmpty { "Adaigi (Payment)" },
            timestamp = System.currentTimeMillis()
        )
        val id = transactionDao.insertTransaction(transaction)
        if (party.type == PartyType.SUPPLIER) {
            // We owed supplier (-ve balance), now we pay them -> balance becomes less negative (add amount)
            partyDao.adjustPartyBalance(party.id, amount)
        } else {
            // Gave cash/credit to customer -> customer owes us more (+ve)
            partyDao.adjustPartyBalance(party.id, amount)
        }
        return transaction.copy(id = id)
    }

    suspend fun softDeleteTransaction(id: Long) {
        transactionDao.softDeleteTransaction(id)
    }

    suspend fun getTransactionItems(txId: Long): List<TransactionItemEntity> {
        return transactionDao.getItemsForTransactionDirect(txId)
    }

    private suspend fun generateInvoiceNumber(prefix: String = "ZS-"): String {
        val latest = transactionDao.getLatestTransaction()
        val nextNum = (latest?.id ?: 0L) + 1001L
        return "$prefix$nextNum"
    }

    // Seed realistic Pakistani Kiryana & Pharmacy data if empty
    suspend fun seedInitialDataIfNeeded() {
        if (productDao.getProductCount() == 0) {
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L

            storeProfileDao.insertOrUpdate(
                StoreProfile(
                    id = 1,
                    storeName = "AL-MAKKAH TRADERS",
                    ownerName = "Muhammad Zia",
                    phone = "0300-7654321",
                    address = "Shop #14, Main Bazaar, Lahore",
                    paymentDetails = "JazzCash / EasyPaisa: 0300-7654321 | Bank Alfalah: 0142-10048291",
                    tagline = "Apni dukan ko behtar banayein",
                    showRomanUrdu = true,
                    wholesaleMode = false,
                    whatsappBilling = true,
                    printPreview = true
                )
            )

            val sampleProducts = listOf(
                ProductEntity(
                    name = "Brufen 200mg (Strip of 10)",
                    barcode = "896400100101",
                    category = "Pharmacy",
                    salePrice = 95.0,
                    wholesalePrice = 85.0,
                    costPrice = 75.0,
                    stockQty = 8.0,
                    lowStockThreshold = 10.0,
                    unit = "packet",
                    expiryDate = now + (11 * dayMillis) // 11 days (Near Expiry!)
                ),
                ProductEntity(
                    name = "Tapal Danedar Tea 475g",
                    barcode = "896400100102",
                    category = "Kiryana",
                    salePrice = 680.0,
                    wholesalePrice = 650.0,
                    costPrice = 620.0,
                    stockQty = 26.0,
                    lowStockThreshold = 5.0,
                    unit = "packet"
                ),
                ProductEntity(
                    name = "Dalda Banaspati Ghee 1kg",
                    barcode = "896400100103",
                    category = "Kiryana",
                    salePrice = 540.0,
                    wholesalePrice = 515.0,
                    costPrice = 490.0,
                    stockQty = 18.0,
                    lowStockThreshold = 5.0,
                    unit = "packet"
                ),
                ProductEntity(
                    name = "Olper's Full Cream Milk 1L",
                    barcode = "896400100104",
                    category = "Dairy",
                    salePrice = 290.0,
                    wholesalePrice = 275.0,
                    costPrice = 260.0,
                    stockQty = 4.0, // Low stock
                    lowStockThreshold = 6.0,
                    unit = "piece",
                    expiryDate = now + (35 * dayMillis)
                ),
                ProductEntity(
                    name = "Shan Bombay Biryani 50g",
                    barcode = "896400100105",
                    category = "Spices",
                    salePrice = 130.0,
                    wholesalePrice = 120.0,
                    costPrice = 110.0,
                    stockQty = 32.0,
                    lowStockThreshold = 8.0,
                    unit = "packet"
                ),
                ProductEntity(
                    name = "Coca Cola 1.5L Bottle",
                    barcode = "896400100106",
                    category = "Beverages",
                    salePrice = 180.0,
                    wholesalePrice = 165.0,
                    costPrice = 155.0,
                    stockQty = 0.0, // Out of Stock!
                    lowStockThreshold = 10.0,
                    unit = "piece"
                ),
                ProductEntity(
                    name = "Rooh Afza Sharbat 800ml",
                    barcode = "896400100107",
                    category = "Beverages",
                    salePrice = 420.0,
                    wholesalePrice = 395.0,
                    costPrice = 370.0,
                    stockQty = 15.0,
                    lowStockThreshold = 4.0,
                    unit = "piece"
                ),
                ProductEntity(
                    name = "Super Kernel Basmati Rice 1kg",
                    barcode = "896400100108",
                    category = "Kiryana",
                    salePrice = 380.0,
                    wholesalePrice = 360.0,
                    costPrice = 340.0,
                    stockQty = 50.0,
                    lowStockThreshold = 10.0,
                    unit = "kg"
                ),
                ProductEntity(
                    name = "Panadol Extra 500mg Box",
                    barcode = "896400100109",
                    category = "Pharmacy",
                    salePrice = 350.0,
                    wholesalePrice = 320.0,
                    costPrice = 295.0,
                    stockQty = 20.0,
                    lowStockThreshold = 5.0,
                    unit = "piece",
                    expiryDate = now + (180 * dayMillis)
                ),
                ProductEntity(
                    name = "Lipton Yellow Label Tea 95g",
                    barcode = "896400100110",
                    category = "Kiryana",
                    salePrice = 210.0,
                    wholesalePrice = 195.0,
                    costPrice = 180.0,
                    stockQty = 14.0,
                    lowStockThreshold = 5.0,
                    unit = "packet"
                )
            )
            productDao.insertAll(sampleProducts)

            val sampleParties = listOf(
                PartyEntity(
                    type = PartyType.CUSTOMER,
                    name = "Haji Abdul Rasheed",
                    phone = "0301-7654321",
                    address = "Mohallah Islam Pura",
                    openingBalance = 4500.0,
                    currentBalance = 4500.0
                ),
                PartyEntity(
                    type = PartyType.CUSTOMER,
                    name = "Chaudhry Kashif",
                    phone = "0322-8765432",
                    address = "Street #4, Saddar",
                    openingBalance = 1850.0,
                    currentBalance = 1850.0
                ),
                PartyEntity(
                    type = PartyType.CUSTOMER,
                    name = "Imran Fast Food",
                    phone = "0333-1122334",
                    address = "Main Chowk Market",
                    openingBalance = 8200.0,
                    currentBalance = 8200.0
                ),
                PartyEntity(
                    type = PartyType.CUSTOMER,
                    name = "Bhai Bilal (Regular Walk-in)",
                    phone = "0345-9988776",
                    address = "Near Jamia Masjid",
                    openingBalance = 0.0,
                    currentBalance = 0.0
                ),
                PartyEntity(
                    type = PartyType.SUPPLIER,
                    name = "Bismillah General Wholesale",
                    phone = "0300-5544332",
                    address = "Akbari Mandi, Lahore",
                    openingBalance = -14000.0,
                    currentBalance = -14000.0
                ),
                PartyEntity(
                    type = PartyType.SUPPLIER,
                    name = "Al-Rehman Pharma Distributors",
                    phone = "0312-3344556",
                    address = "Circular Road, Lahore",
                    openingBalance = -6500.0,
                    currentBalance = -6500.0
                )
            )
            partyDao.insertAll(sampleParties)

            // Seed initial activity (Sale, Recovery, Expense)
            val tx1 = TransactionEntity(
                invoiceNo = "ZS-1001",
                type = TransactionType.SALE,
                partyId = 1,
                partyName = "Haji Abdul Rasheed",
                partyPhone = "0301-7654321",
                subtotal = 1220.0,
                discountAmount = 20.0,
                totalAmount = 1200.0,
                paidAmount = 500.0,
                paymentMode = PaymentMode.UDHAAR,
                notes = "Tapal Danedar + Dalda",
                timestamp = now - (2 * 3600000L)
            )
            val txId1 = transactionDao.insertTransaction(tx1)
            transactionDao.insertTransactionItems(
                listOf(
                    TransactionItemEntity(
                        transactionId = txId1,
                        productId = 2,
                        productName = "Tapal Danedar Tea 475g",
                        unit = "packet",
                        qty = 1.0,
                        unitPrice = 680.0,
                        costAtSale = 620.0
                    ),
                    TransactionItemEntity(
                        transactionId = txId1,
                        productId = 3,
                        productName = "Dalda Banaspati Ghee 1kg",
                        unit = "packet",
                        qty = 1.0,
                        unitPrice = 540.0,
                        costAtSale = 490.0
                    )
                )
            )

            val tx2 = TransactionEntity(
                invoiceNo = "REC-1002",
                type = TransactionType.RECOVERY,
                partyId = 2,
                partyName = "Chaudhry Kashif",
                partyPhone = "0322-8765432",
                subtotal = 1000.0,
                totalAmount = 1000.0,
                paidAmount = 1000.0,
                paymentMode = PaymentMode.CASH,
                notes = "Wasooli (Udhaar recovery)",
                timestamp = now - (5 * 3600000L)
            )
            transactionDao.insertTransaction(tx2)

            val exp1 = ExpenseEntity(
                amount = 350.0,
                category = "Chai Pani",
                description = "Tea & biscuits for shop staff",
                timestamp = now - (3 * 3600000L)
            )
            val exp2 = ExpenseEntity(
                amount = 1200.0,
                category = "Shop Rent",
                description = "Shop maintenance contribution",
                timestamp = now - (10 * 3600000L)
            )
            expenseDao.insertExpense(exp1)
            expenseDao.insertExpense(exp2)
        }
    }

    // Export local encrypted/plain JSON backup for on-device export
    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val profile = storeProfileDao.getStoreProfileDirect() ?: StoreProfile()
        val profJson = JSONObject().apply {
            put("storeName", profile.storeName)
            put("ownerName", profile.ownerName)
            put("phone", profile.phone)
            put("address", profile.address)
            put("paymentDetails", profile.paymentDetails)
            put("tagline", profile.tagline)
            put("showRomanUrdu", profile.showRomanUrdu)
            put("wholesaleMode", profile.wholesaleMode)
        }
        root.put("profile", profJson)

        val expensesJson = JSONArray()
        // could export expenses if needed
        root.put("expenses", expensesJson)

        return root.toString(2)
    }

    suspend fun resetAllData() {
        transactionDao.deleteAllItems()
        transactionDao.deleteAll()
        expenseDao.deleteAll()
        productDao.deleteAll()
        partyDao.deleteAll()
        seedInitialDataIfNeeded()
    }
}
