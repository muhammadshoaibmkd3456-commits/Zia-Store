package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ZiaStoreDatabase
import com.example.data.model.CartItem
import com.example.data.model.DateFilter
import com.example.data.model.ExpenseEntity
import com.example.data.model.PartyEntity
import com.example.data.model.PartyType
import com.example.data.model.PaymentMode
import com.example.data.model.PosMode
import com.example.data.model.ProductEntity
import com.example.data.model.StockFilter
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionType
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class NotificationAlert(
    val id: String,
    val title: String,
    val message: String,
    val type: String, // "OUT_OF_STOCK", "LOW_STOCK", "NEAR_EXPIRY"
    val productId: Long
)

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ZiaStoreDatabase.getDatabase(application)
    private val repository = StoreRepository(
        database.storeProfileDao(),
        database.productDao(),
        database.partyDao(),
        database.transactionDao(),
        database.expenseDao()
    )

    // Store Profile
    val storeProfile: StateFlow<StoreProfile> = repository.storeProfile
        .map { it ?: StoreProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StoreProfile()
        )

    // All Products
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Parties
    val customers: StateFlow<List<PartyEntity>> = repository.customers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val suppliers: StateFlow<List<PartyEntity>> = repository.suppliers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Transactions
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactionItems: StateFlow<List<TransactionItemEntity>> = repository.allTransactionItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Expenses
    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI Navigation State
    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Home Date Filter
    private val _dateFilter = MutableStateFlow(DateFilter.TODAY)
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    // POS Cart & Mode
    private val _posMode = MutableStateFlow(PosMode.SALE)
    val posMode: StateFlow<PosMode> = _posMode.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isWholesale = MutableStateFlow(false)
    val isWholesale: StateFlow<Boolean> = _isWholesale.asStateFlow()

    private val _posCategory = MutableStateFlow("All Items")
    val posCategory: StateFlow<String> = _posCategory.asStateFlow()

    private val _posSearchQuery = MutableStateFlow("")
    val posSearchQuery: StateFlow<String> = _posSearchQuery.asStateFlow()

    // Products Screen State
    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    private val _productStockFilter = MutableStateFlow(StockFilter.ALL)
    val productStockFilter: StateFlow<StockFilter> = _productStockFilter.asStateFlow()

    private val _productCategoryFilter = MutableStateFlow("All Items")
    val productCategoryFilter: StateFlow<String> = _productCategoryFilter.asStateFlow()

    // Ledgers Screen State
    private val _ledgerTab = MutableStateFlow(PartyType.CUSTOMER)
    val ledgerTab: StateFlow<PartyType> = _ledgerTab.asStateFlow()

    private val _ledgerSearchQuery = MutableStateFlow("")
    val ledgerSearchQuery: StateFlow<String> = _ledgerSearchQuery.asStateFlow()

    private val _activePartyId = MutableStateFlow<Long?>(null)
    val activePartyId: StateFlow<Long?> = _activePartyId.asStateFlow()

    // Activity Filter
    private val _activitySearch = MutableStateFlow("")
    val activitySearch: StateFlow<String> = _activitySearch.asStateFlow()

    private val _activityTypeFilter = MutableStateFlow<String>("ALL")
    val activityTypeFilter: StateFlow<String> = _activityTypeFilter.asStateFlow()

    // Modals and Dialogs State
    private val _showNotifications = MutableStateFlow(false)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _showReviewSheet = MutableStateFlow(false)
    val showReviewSheet: StateFlow<Boolean> = _showReviewSheet.asStateFlow()

    private val _showUdhaarPicker = MutableStateFlow(false)
    val showUdhaarPicker: StateFlow<Boolean> = _showUdhaarPicker.asStateFlow()

    private val _showWhatsAppDialog = MutableStateFlow(false)
    val showWhatsAppDialog: StateFlow<Boolean> = _showWhatsAppDialog.asStateFlow()

    private val _showReceiptPreview = MutableStateFlow(false)
    val showReceiptPreview: StateFlow<Boolean> = _showReceiptPreview.asStateFlow()

    private val _latestCompletedTransaction = MutableStateFlow<TransactionEntity?>(null)
    val latestCompletedTransaction: StateFlow<TransactionEntity?> = _latestCompletedTransaction.asStateFlow()

    private val _latestCompletedItems = MutableStateFlow<List<TransactionItemEntity>>(emptyList())
    val latestCompletedItems: StateFlow<List<TransactionItemEntity>> = _latestCompletedItems.asStateFlow()

    private val _showScannerSheet = MutableStateFlow(false)
    val showScannerSheet: StateFlow<Boolean> = _showScannerSheet.asStateFlow()

    private val _scannerTarget = MutableStateFlow("pos") // "pos" or "product"
    val scannerTarget: StateFlow<String> = _scannerTarget.asStateFlow()

    private val _showAddProduct = MutableStateFlow(false)
    val showAddProduct: StateFlow<Boolean> = _showAddProduct.asStateFlow()

    private val _productToEdit = MutableStateFlow<ProductEntity?>(null)
    val productToEdit: StateFlow<ProductEntity?> = _productToEdit.asStateFlow()

    private val _showAddParty = MutableStateFlow(false)
    val showAddParty: StateFlow<Boolean> = _showAddParty.asStateFlow()

    private val _partyToEdit = MutableStateFlow<PartyEntity?>(null)
    val partyToEdit: StateFlow<PartyEntity?> = _partyToEdit.asStateFlow()

    private val _showAddExpense = MutableStateFlow(false)
    val showAddExpense: StateFlow<Boolean> = _showAddExpense.asStateFlow()

    private val _showPerformanceHub = MutableStateFlow(false)
    val showPerformanceHub: StateFlow<Boolean> = _showPerformanceHub.asStateFlow()

    private val _showBusinessCard = MutableStateFlow(false)
    val showBusinessCard: StateFlow<Boolean> = _showBusinessCard.asStateFlow()

    private val _showStoreSettings = MutableStateFlow(false)
    val showStoreSettings: StateFlow<Boolean> = _showStoreSettings.asStateFlow()

    private val _showStatementDialog = MutableStateFlow(false)
    val showStatementDialog: StateFlow<Boolean> = _showStatementDialog.asStateFlow()

    private val _showPartyPaymentDialog = MutableStateFlow(false) // Maine Diye
    val showPartyPaymentDialog: StateFlow<Boolean> = _showPartyPaymentDialog.asStateFlow()

    private val _showPartyRecoveryDialog = MutableStateFlow(false) // Maine Liye
    val showPartyRecoveryDialog: StateFlow<Boolean> = _showPartyRecoveryDialog.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _prefilledBarcode = MutableStateFlow<String?>(null)
    val prefilledBarcode: StateFlow<String?> = _prefilledBarcode.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
    }

    fun setPosMode(mode: PosMode) {
        _posMode.value = mode
        clearCart()
    }

    fun toggleWholesaleMode(enabled: Boolean? = null) {
        _isWholesale.value = enabled ?: !_isWholesale.value
        // Update custom price for items already in cart
        val currentItems = _cartItems.value.map { item ->
            val price = if (_isWholesale.value) item.product.wholesalePrice else item.product.salePrice
            item.copy(customPrice = price)
        }
        _cartItems.value = currentItems
    }

    fun setPosCategory(category: String) {
        _posCategory.value = category
    }

    fun setPosSearch(query: String) {
        _posSearchQuery.value = query
    }

    fun setProductSearch(query: String) {
        _productSearchQuery.value = query
    }

    fun setProductStockFilter(filter: StockFilter) {
        _productStockFilter.value = filter
    }

    fun setProductCategoryFilter(category: String) {
        _productCategoryFilter.value = category
    }

    fun setLedgerTab(type: PartyType) {
        _ledgerTab.value = type
    }

    fun setLedgerSearch(query: String) {
        _ledgerSearchQuery.value = query
    }

    fun setActiveParty(partyId: Long?) {
        _activePartyId.value = partyId
    }

    fun setActivitySearch(query: String) {
        _activitySearch.value = query
    }

    fun setActivityTypeFilter(type: String) {
        _activityTypeFilter.value = type
    }

    // Modal Control
    fun openNotifications(show: Boolean) { _showNotifications.value = show }
    fun openReviewSheet(show: Boolean) { _showReviewSheet.value = show }
    fun openUdhaarPicker(show: Boolean) { _showUdhaarPicker.value = show }
    fun openWhatsAppDialog(show: Boolean) { _showWhatsAppDialog.value = show }
    fun openReceiptPreview(show: Boolean) { _showReceiptPreview.value = show }
    fun openScanner(show: Boolean, target: String = "pos") {
        _scannerTarget.value = target
        _showScannerSheet.value = show
    }
    fun openAddProduct(show: Boolean, product: ProductEntity? = null, prefilledBarcode: String? = null) {
        _productToEdit.value = product
        _prefilledBarcode.value = prefilledBarcode
        _showAddProduct.value = show
    }
    fun openAddParty(show: Boolean, party: PartyEntity? = null) {
        _partyToEdit.value = party
        _showAddParty.value = show
    }
    fun openAddExpense(show: Boolean) { _showAddExpense.value = show }
    fun openPerformanceHub(show: Boolean) { _showPerformanceHub.value = show }
    fun openBusinessCard(show: Boolean) { _showBusinessCard.value = show }
    fun openStoreSettings(show: Boolean) { _showStoreSettings.value = show }
    fun openStatementDialog(show: Boolean) { _showStatementDialog.value = show }
    fun openPartyPaymentDialog(show: Boolean) { _showPartyPaymentDialog.value = show }
    fun openPartyRecoveryDialog(show: Boolean) { _showPartyRecoveryDialog.value = show }

    // POS Cart Operations
    fun addToCart(product: ProductEntity) {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
        val price = if (_posMode.value == PosMode.PURCHASE) {
            product.costPrice
        } else if (_isWholesale.value) {
            product.wholesalePrice
        } else {
            product.salePrice
        }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(qty = existing.qty + 1.0)
        } else {
            currentList.add(CartItem(product = product, qty = 1.0, customPrice = price))
        }
        _cartItems.value = currentList
        if (_posMode.value == PosMode.SALE && product.stockQty <= 0) {
            showToast("Khabardar: ${product.name} out of stock hai, lekin cart me shamil kardiya gaya!")
        } else {
            showToast("${product.name} cart me add hogaya!")
        }
    }

    fun updateCartQty(productId: Long, delta: Double) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentList[index]
            val newQty = item.qty + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = item.copy(qty = newQty)
            }
            _cartItems.value = currentList
        }
    }

    fun updateCartItemPrice(productId: Long, newPrice: Double) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentList[index]
            currentList[index] = item.copy(customPrice = newPrice.coerceAtLeast(0.0))
            _cartItems.value = currentList
        }
    }

    fun removeCartItem(productId: Long) {
        val currentList = _cartItems.value.filterNot { it.product.id == productId }
        _cartItems.value = currentList
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Process Complete Cash Sale
    fun completeCashSale(discountAmount: Double, walkInPhone: String = "", notes: String = "") {
        viewModelScope.launch {
            val items = _cartItems.value
            if (items.isEmpty()) return@launch
            val subtotal = items.sumOf { it.lineTotal }
            val netTotal = (subtotal - discountAmount).coerceAtLeast(0.0)

            val tx = repository.processSale(
                cartItems = items,
                party = null,
                walkInPhone = walkInPhone,
                discountAmount = discountAmount,
                paidAmount = netTotal,
                paymentMode = PaymentMode.CASH,
                notes = notes
            )
            _latestCompletedTransaction.value = tx
            _latestCompletedItems.value = repository.getTransactionItems(tx.id)
            clearCart()
            _showReviewSheet.value = false
            if (storeProfile.value.printPreview) {
                _showReceiptPreview.value = true
            }
            showToast("Sale mukammal! Bill #${tx.invoiceNo} ban gaya.")
        }
    }

    // Complete WhatsApp Cash Sale
    fun completeWhatsAppSale(phone: String, discountAmount: Double, notes: String = "") {
        viewModelScope.launch {
            val items = _cartItems.value
            if (items.isEmpty()) return@launch
            val subtotal = items.sumOf { it.lineTotal }
            val netTotal = (subtotal - discountAmount).coerceAtLeast(0.0)

            val tx = repository.processSale(
                cartItems = items,
                party = null,
                walkInPhone = phone,
                discountAmount = discountAmount,
                paidAmount = netTotal,
                paymentMode = PaymentMode.WHATSAPP_CASH,
                notes = notes
            )
            _latestCompletedTransaction.value = tx
            _latestCompletedItems.value = repository.getTransactionItems(tx.id)
            clearCart()
            _showReviewSheet.value = false
            _showWhatsAppDialog.value = false
            if (storeProfile.value.printPreview) {
                _showReceiptPreview.value = true
            }
            showToast("WhatsApp Bill record hogaya!")
        }
    }

    // Complete Udhaar Sale
    fun completeUdhaarSale(customer: PartyEntity, cashPaidToday: Double, discountAmount: Double, notes: String = "") {
        viewModelScope.launch {
            val items = _cartItems.value
            if (items.isEmpty()) return@launch
            val tx = repository.processSale(
                cartItems = items,
                party = customer,
                walkInPhone = customer.phone,
                discountAmount = discountAmount,
                paidAmount = cashPaidToday,
                paymentMode = if (cashPaidToday > 0.0) PaymentMode.MIXED else PaymentMode.UDHAAR,
                notes = notes
            )
            _latestCompletedTransaction.value = tx
            _latestCompletedItems.value = repository.getTransactionItems(tx.id)
            clearCart()
            _showReviewSheet.value = false
            _showUdhaarPicker.value = false
            if (storeProfile.value.printPreview) {
                _showReceiptPreview.value = true
            }
            showToast("Udhaar Sale darj hogyi! ${customer.name} ka khata update hogaya.")
        }
    }

    // Process Purchase Mode Checkout
    fun completePurchase(supplier: PartyEntity?, supplierName: String, supplierPhone: String, paidAmount: Double, notes: String = "") {
        viewModelScope.launch {
            val items = _cartItems.value
            if (items.isEmpty()) return@launch
            val tx = repository.processPurchase(
                cartItems = items,
                supplier = supplier,
                supplierName = supplierName,
                supplierPhone = supplierPhone,
                paidAmount = paidAmount,
                paymentMode = if (supplier != null && paidAmount < items.sumOf { it.lineTotal }) PaymentMode.UDHAAR else PaymentMode.CASH,
                notes = notes
            )
            _latestCompletedTransaction.value = tx
            _latestCompletedItems.value = repository.getTransactionItems(tx.id)
            clearCart()
            _showReviewSheet.value = false
            showToast("Khareedari darj hogayi! Maal stock me shamil hogaya.")
        }
    }

    // Process Return Mode Checkout
    fun completeReturn(party: PartyEntity?, refundAmount: Double, isCustomerReturn: Boolean, notes: String = "") {
        viewModelScope.launch {
            val items = _cartItems.value
            if (items.isEmpty()) return@launch
            val tx = repository.processReturn(
                cartItems = items,
                party = party,
                refundAmount = refundAmount,
                isCustomerReturn = isCustomerReturn,
                notes = notes
            )
            _latestCompletedTransaction.value = tx
            _latestCompletedItems.value = repository.getTransactionItems(tx.id)
            clearCart()
            _showReviewSheet.value = false
            showToast("Wapsi record hogayi aur hisaab barabar hogaya.")
        }
    }

    // View specific past transaction in 58mm preview
    fun viewTransactionReceipt(tx: TransactionEntity) {
        viewModelScope.launch {
            _latestCompletedTransaction.value = tx
            _latestCompletedItems.value = repository.getTransactionItems(tx.id)
            _showReceiptPreview.value = true
        }
    }

    // Handle Barcode Scanned
    fun handleBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            if (_scannerTarget.value == "pos") {
                val matched = repository.findProductByBarcode(barcode)
                if (matched != null) {
                    addToCart(matched)
                } else {
                    // Not found: prompt to add product with this barcode
                    _showScannerSheet.value = false
                    openAddProduct(show = true, prefilledBarcode = barcode)
                    showToast("Naya item: Barcode $barcode ke sath item shamil karein")
                }
            } else {
                // For Add/Edit product
                _prefilledBarcode.value = barcode
                _showScannerSheet.value = false
                showToast("Barcode lag gaya: $barcode")
            }
        }
    }

    // Product CRUD
    fun saveProduct(product: ProductEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            val duplicate = repository.checkBarcodeExists(product.barcode, product.id)
            if (duplicate) {
                showToast("Ghalti: Yeh Barcode pehle se kisi aur item me mojood hai!")
                return@launch
            }
            repository.saveProduct(product)
            _showAddProduct.value = false
            _productToEdit.value = null
            _prefilledBarcode.value = null
            showToast("${product.name} mehfooz hogaya!")
            onDone()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.archiveProduct(product.id)
            showToast("${product.name} delete hogaya.")
        }
    }

    // Party CRUD
    fun saveParty(party: PartyEntity) {
        viewModelScope.launch {
            repository.saveParty(party)
            _showAddParty.value = false
            _partyToEdit.value = null
            showToast("${party.name} ka khata mehfooz hogaya!")
        }
    }

    fun deleteParty(party: PartyEntity) {
        viewModelScope.launch {
            repository.deleteParty(party)
            if (_activePartyId.value == party.id) {
                _activePartyId.value = null
            }
            showToast("${party.name} delete hogaya.")
        }
    }

    // Expense CRUD
    fun saveExpense(amount: Double, category: String, description: String) {
        viewModelScope.launch {
            val exp = ExpenseEntity(
                amount = amount,
                category = category,
                description = description,
                timestamp = System.currentTimeMillis()
            )
            repository.saveExpense(exp)
            _showAddExpense.value = false
            showToast("Kharcha Rs. $amount darj hogaya.")
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            showToast("Kharcha delete hogaya.")
        }
    }

    // Ledger Actions: Maine Liye & Maine Diye
    fun recordMaineLiye(party: PartyEntity, amount: Double, notes: String = "") {
        viewModelScope.launch {
            repository.recordRecovery(party, amount, notes)
            _showPartyRecoveryDialog.value = false
            showToast("Wasooli Rs. $amount record hogyi! Hisaab kam hogaya.")
        }
    }

    fun recordMaineDiye(party: PartyEntity, amount: Double, notes: String = "") {
        viewModelScope.launch {
            repository.recordPayment(party, amount, notes)
            _showPartyPaymentDialog.value = false
            showToast("Adaigi Rs. $amount darj hogyi!")
        }
    }

    fun deleteTransaction(txId: Long) {
        viewModelScope.launch {
            repository.softDeleteTransaction(txId)
            showToast("Transaction delete hogayi.")
        }
    }

    // Store Profile Update
    fun updateStoreProfile(profile: StoreProfile) {
        viewModelScope.launch {
            repository.saveStoreProfile(profile)
            _showStoreSettings.value = false
            showToast("Dukan ki tafseelat mehfooz hogayi!")
        }
    }

    fun toggleRomanUrdu() {
        val curr = storeProfile.value
        updateStoreProfile(curr.copy(showRomanUrdu = !curr.showRomanUrdu))
    }

    fun toggleWholesaleSetting() {
        val curr = storeProfile.value
        updateStoreProfile(curr.copy(wholesaleMode = !curr.wholesaleMode))
        _isWholesale.value = !curr.wholesaleMode
    }

    fun toggleWhatsAppBillingSetting() {
        val curr = storeProfile.value
        updateStoreProfile(curr.copy(whatsappBilling = !curr.whatsappBilling))
    }

    fun togglePrintPreviewSetting() {
        val curr = storeProfile.value
        updateStoreProfile(curr.copy(printPreview = !curr.printPreview))
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
            clearCart()
            showToast("Sample data dobara restore hogaya!")
        }
    }

    // Dynamic Notifications
    fun getAlerts(products: List<ProductEntity>): List<NotificationAlert> {
        val alerts = mutableListOf<NotificationAlert>()
        val now = System.currentTimeMillis()
        val fifteenDays = 15 * 86400000L

        for (p in products) {
            if (p.stockQty <= 0) {
                alerts.add(
                    NotificationAlert(
                        id = "out_${p.id}",
                        title = "Out of Stock: ${p.name}",
                        message = "Yeh item mukammal khatam hai (0 ${p.unit}). Foran order karein!",
                        type = "OUT_OF_STOCK",
                        productId = p.id
                    )
                )
            } else if (p.stockQty <= p.lowStockThreshold) {
                alerts.add(
                    NotificationAlert(
                        id = "low_${p.id}",
                        title = "Low Stock: ${p.name}",
                        message = "Sirf ${p.stockQty.toInt()} ${p.unit} baaqi hain (Hadd: ${p.lowStockThreshold.toInt()}).",
                        type = "LOW_STOCK",
                        productId = p.id
                    )
                )
            }

            p.expiryDate?.let { exp ->
                if (exp in now..(now + fifteenDays)) {
                    val daysLeft = ((exp - now) / 86400000L).coerceAtLeast(0)
                    alerts.add(
                        NotificationAlert(
                            id = "exp_${p.id}",
                            title = "Near Expiry: ${p.name}",
                            message = "Sirf $daysLeft din baaqi hain expiry me!",
                            type = "NEAR_EXPIRY",
                            productId = p.id
                        )
                    )
                }
            }
        }
        return alerts
    }

    // Helper: Filter by date
    fun isTimestampInFilter(timestamp: Long, filter: DateFilter): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val targetDay = cal.get(Calendar.DAY_OF_YEAR)
        val targetYear = cal.get(Calendar.YEAR)
        val targetMonth = cal.get(Calendar.MONTH)

        val nowCal = Calendar.getInstance()
        val currentDay = nowCal.get(Calendar.DAY_OF_YEAR)
        val currentYear = nowCal.get(Calendar.YEAR)
        val currentMonth = nowCal.get(Calendar.MONTH)

        return when (filter) {
            DateFilter.TODAY -> targetDay == currentDay && targetYear == currentYear
            DateFilter.YESTERDAY -> {
                nowCal.add(Calendar.DAY_OF_YEAR, -1)
                targetDay == nowCal.get(Calendar.DAY_OF_YEAR) && targetYear == nowCal.get(Calendar.YEAR)
            }
            DateFilter.THIS_MONTH -> targetMonth == currentMonth && targetYear == currentYear
            DateFilter.ALL_TIME -> true
        }
    }
}
