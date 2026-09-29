package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PartyType
import com.example.data.model.StockFilter
import com.example.ui.components.BarcodeScannerSheet
import com.example.ui.components.BusinessCardSheet
import com.example.ui.components.NotificationSheet
import com.example.ui.components.ThermalReceiptDialog
import com.example.ui.components.ZiaBottomNav
import com.example.ui.components.ZiaTopBar
import com.example.ui.screens.AddExpenseDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LedgersScreen
import com.example.ui.screens.PerformanceHubSheet
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UdhaarAccountPickerSheet
import com.example.ui.screens.WhatsAppBillingDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StoreViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ZiaStoreApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ZiaStoreApp(viewModel: StoreViewModel) {
    val profile by viewModel.storeProfile.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val transactionItems by viewModel.allTransactionItems.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()

    // POS State
    val posMode by viewModel.posMode.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val isWholesale by viewModel.isWholesale.collectAsStateWithLifecycle()
    val posCategory by viewModel.posCategory.collectAsStateWithLifecycle()
    val posSearchQuery by viewModel.posSearchQuery.collectAsStateWithLifecycle()

    // Products State
    val productSearchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()
    val productStockFilter by viewModel.productStockFilter.collectAsStateWithLifecycle()
    val productCategoryFilter by viewModel.productCategoryFilter.collectAsStateWithLifecycle()

    // Ledgers State
    val ledgerTab by viewModel.ledgerTab.collectAsStateWithLifecycle()
    val ledgerSearchQuery by viewModel.ledgerSearchQuery.collectAsStateWithLifecycle()
    val activePartyId by viewModel.activePartyId.collectAsStateWithLifecycle()

    // Activity State
    val activitySearch by viewModel.activitySearch.collectAsStateWithLifecycle()
    val activityTypeFilter by viewModel.activityTypeFilter.collectAsStateWithLifecycle()

    // Modals
    val showNotifications by viewModel.showNotifications.collectAsStateWithLifecycle()
    val showReviewSheet by viewModel.showReviewSheet.collectAsStateWithLifecycle()
    val showUdhaarPicker by viewModel.showUdhaarPicker.collectAsStateWithLifecycle()
    val showWhatsAppDialog by viewModel.showWhatsAppDialog.collectAsStateWithLifecycle()
    val showReceiptPreview by viewModel.showReceiptPreview.collectAsStateWithLifecycle()
    val latestTx by viewModel.latestCompletedTransaction.collectAsStateWithLifecycle()
    val latestItems by viewModel.latestCompletedItems.collectAsStateWithLifecycle()
    val showScanner by viewModel.showScannerSheet.collectAsStateWithLifecycle()
    val showAddProduct by viewModel.showAddProduct.collectAsStateWithLifecycle()
    val productToEdit by viewModel.productToEdit.collectAsStateWithLifecycle()
    val prefilledBarcode by viewModel.prefilledBarcode.collectAsStateWithLifecycle()
    val showAddParty by viewModel.showAddParty.collectAsStateWithLifecycle()
    val partyToEdit by viewModel.partyToEdit.collectAsStateWithLifecycle()
    val showAddExpense by viewModel.showAddExpense.collectAsStateWithLifecycle()
    val showPerformanceHub by viewModel.showPerformanceHub.collectAsStateWithLifecycle()
    val showBusinessCard by viewModel.showBusinessCard.collectAsStateWithLifecycle()
    val showStoreSettings by viewModel.showStoreSettings.collectAsStateWithLifecycle()
    val showStatementDialog by viewModel.showStatementDialog.collectAsStateWithLifecycle()
    val showRecoveryDialog by viewModel.showPartyRecoveryDialog.collectAsStateWithLifecycle()
    val showPaymentDialog by viewModel.showPartyPaymentDialog.collectAsStateWithLifecycle()

    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val alerts = remember(products) { viewModel.getAlerts(products) }

    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            // Only show persistent top bar if not on custom child screen or detail
            if (activePartyId == null) {
                ZiaTopBar(
                    profile = profile,
                    dateFilter = dateFilter,
                    notificationCount = alerts.size,
                    onDateFilterChange = { viewModel.setDateFilter(it) },
                    onNotificationsClick = { viewModel.openNotifications(true) },
                    showDateFilter = currentTab == "home"
                )
            }
        },
        bottomBar = {
            if (activePartyId == null) {
                ZiaBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) },
                    cartItemCount = cartItems.sumOf { it.qty }.toInt(),
                    showRomanUrdu = profile.showRomanUrdu
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "home" -> {
                    HomeScreen(
                        profile = profile,
                        products = products,
                        customers = customers,
                        suppliers = suppliers,
                        transactions = transactions,
                        expenses = expenses,
                        dateFilter = dateFilter,
                        activitySearch = activitySearch,
                        activityTypeFilter = activityTypeFilter,
                        isTimestampInFilter = { ts, df -> viewModel.isTimestampInFilter(ts, df) },
                        onActivitySearchChange = { viewModel.setActivitySearch(it) },
                        onActivityTypeFilterChange = { viewModel.setActivityTypeFilter(it) },
                        onAddExpenseClick = { viewModel.openAddExpense(true) },
                        onDrillDownProducts = { stockFilter ->
                            viewModel.setProductStockFilter(stockFilter)
                            viewModel.setTab("products")
                        },
                        onDrillDownLedgers = {
                            viewModel.setTab("ledgers")
                        },
                        onViewTransaction = { tx ->
                            viewModel.viewTransactionReceipt(tx)
                        },
                        onDeleteTransaction = { txId ->
                            viewModel.deleteTransaction(txId)
                        }
                    )
                }

                "pos" -> {
                    PosScreen(
                        profile = profile,
                        products = products,
                        posMode = posMode,
                        cartItems = cartItems,
                        isWholesale = isWholesale,
                        selectedCategory = posCategory,
                        searchQuery = posSearchQuery,
                        showReviewSheet = showReviewSheet,
                        onPosModeChange = { viewModel.setPosMode(it) },
                        onToggleWholesale = { viewModel.toggleWholesaleMode() },
                        onCategoryChange = { viewModel.setPosCategory(it) },
                        onSearchChange = { viewModel.setPosSearch(it) },
                        onAddToCart = { viewModel.addToCart(it) },
                        onUpdateCartQty = { id, delta -> viewModel.updateCartQty(id, delta) },
                        onUpdateCartPrice = { id, p -> viewModel.updateCartItemPrice(id, p) },
                        onRemoveCartItem = { viewModel.removeCartItem(it) },
                        onClearCart = { viewModel.clearCart() },
                        onOpenScanner = { viewModel.openScanner(true, target = "pos") },
                        onOpenReviewSheet = { viewModel.openReviewSheet(it) },
                        onCheckoutCash = { discount ->
                            viewModel.completeCashSale(discountAmount = discount)
                        },
                        onCheckoutWhatsApp = {
                            viewModel.openWhatsAppDialog(true)
                        },
                        onCheckoutUdhaar = {
                            viewModel.openUdhaarPicker(true)
                        },
                        onCheckoutPurchase = { paid ->
                            viewModel.completePurchase(
                                supplier = null,
                                supplierName = "General Supplier",
                                supplierPhone = "",
                                paidAmount = paid
                            )
                        },
                        onCheckoutReturn = { refund ->
                            viewModel.completeReturn(
                                party = null,
                                refundAmount = refund,
                                isCustomerReturn = true
                            )
                        }
                    )
                }

                "products" -> {
                    ProductsScreen(
                        profile = profile,
                        products = products,
                        searchQuery = productSearchQuery,
                        selectedStockFilter = productStockFilter,
                        selectedCategory = productCategoryFilter,
                        showAddProductSheet = showAddProduct,
                        productToEdit = productToEdit,
                        prefilledBarcode = prefilledBarcode,
                        onSearchChange = { viewModel.setProductSearch(it) },
                        onStockFilterChange = { viewModel.setProductStockFilter(it) },
                        onCategoryChange = { viewModel.setProductCategoryFilter(it) },
                        onOpenAddProduct = { show, prod, barcode ->
                            viewModel.openAddProduct(show, prod, barcode)
                        },
                        onOpenScanner = { viewModel.openScanner(true, target = "product") },
                        onOpenPerformanceHub = { viewModel.openPerformanceHub(true) },
                        onSaveProduct = { prod ->
                            viewModel.saveProduct(prod) {}
                        },
                        onDeleteProduct = { prod ->
                            viewModel.deleteProduct(prod)
                        }
                    )
                }

                "ledgers" -> {
                    LedgersScreen(
                        profile = profile,
                        customers = customers,
                        suppliers = suppliers,
                        selectedTab = ledgerTab,
                        searchQuery = ledgerSearchQuery,
                        activePartyId = activePartyId,
                        transactions = transactions,
                        showAddPartySheet = showAddParty,
                        partyToEdit = partyToEdit,
                        showPartyRecoveryDialog = showRecoveryDialog,
                        showPartyPaymentDialog = showPaymentDialog,
                        showStatementDialog = showStatementDialog,
                        onTabChange = { viewModel.setLedgerTab(it) },
                        onSearchChange = { viewModel.setLedgerSearch(it) },
                        onSelectParty = { viewModel.setActiveParty(it) },
                        onOpenAddParty = { show, p -> viewModel.openAddParty(show, p) },
                        onSaveParty = { viewModel.saveParty(it) },
                        onDeleteParty = { viewModel.deleteParty(it) },
                        onOpenRecoveryDialog = { viewModel.openPartyRecoveryDialog(it) },
                        onOpenPaymentDialog = { viewModel.openPartyPaymentDialog(it) },
                        onOpenStatementDialog = { viewModel.openStatementDialog(it) },
                        onRecordMaineLiye = { p, amt, notes ->
                            viewModel.recordMaineLiye(p, amt, notes)
                        },
                        onRecordMaineDiye = { p, amt, notes ->
                            viewModel.recordMaineDiye(p, amt, notes)
                        },
                        onDeleteTransaction = { txId ->
                            viewModel.deleteTransaction(txId)
                        }
                    )
                }

                "settings" -> {
                    SettingsScreen(
                        profile = profile,
                        showStoreSettingsSheet = showStoreSettings,
                        onOpenBusinessCard = { viewModel.openBusinessCard(true) },
                        onOpenStoreSettings = { viewModel.openStoreSettings(it) },
                        onToggleRomanUrdu = { viewModel.toggleRomanUrdu() },
                        onToggleWholesale = { viewModel.toggleWholesaleSetting() },
                        onToggleWhatsAppBilling = { viewModel.toggleWhatsAppBillingSetting() },
                        onTogglePrintPreview = { viewModel.togglePrintPreviewSetting() },
                        onSaveStoreProfile = { viewModel.updateStoreProfile(it) },
                        onResetData = { viewModel.resetData() }
                    )
                }
            }
        }
    }

    // Modal Overlays
    if (showNotifications) {
        NotificationSheet(
            alerts = alerts,
            onDismiss = { viewModel.openNotifications(false) },
            onAlertClick = { prodId ->
                viewModel.setTab("products")
            }
        )
    }

    if (showUdhaarPicker) {
        UdhaarAccountPickerSheet(
            customers = customers,
            onSelectCustomer = { customer, cashPaidToday ->
                viewModel.completeUdhaarSale(
                    customer = customer,
                    cashPaidToday = cashPaidToday,
                    discountAmount = 0.0
                )
            },
            onAddNewCustomer = {
                viewModel.openUdhaarPicker(false)
                viewModel.openAddParty(true, null)
            },
            onDismiss = { viewModel.openUdhaarPicker(false) }
        )
    }

    if (showWhatsAppDialog) {
        WhatsAppBillingDialog(
            profile = profile,
            cartItems = cartItems,
            discountAmount = 0.0,
            onConfirm = { phone ->
                viewModel.completeWhatsAppSale(phone = phone, discountAmount = 0.0)
            },
            onDismiss = { viewModel.openWhatsAppDialog(false) }
        )
    }

    if (showReceiptPreview && latestTx != null) {
        ThermalReceiptDialog(
            transaction = latestTx!!,
            items = latestItems,
            profile = profile,
            onDismiss = { viewModel.openReceiptPreview(false) }
        )
    }

    if (showScanner) {
        BarcodeScannerSheet(
            products = products,
            onBarcodeScanned = { barcode ->
                viewModel.handleBarcodeScanned(barcode)
            },
            onDismiss = { viewModel.openScanner(false) }
        )
    }

    if (showPerformanceHub) {
        PerformanceHubSheet(
            products = products,
            transactions = transactions,
            transactionItems = transactionItems,
            isTimestampInFilter = { ts, df -> viewModel.isTimestampInFilter(ts, df) },
            onDismiss = { viewModel.openPerformanceHub(false) }
        )
    }

    if (showBusinessCard) {
        BusinessCardSheet(
            profile = profile,
            onDismiss = { viewModel.openBusinessCard(false) }
        )
    }

    if (showAddExpense) {
        AddExpenseDialog(
            onSaveExpense = { amt, cat, desc ->
                viewModel.saveExpense(amt, cat, desc)
            },
            onDismiss = { viewModel.openAddExpense(false) }
        )
    }
}
