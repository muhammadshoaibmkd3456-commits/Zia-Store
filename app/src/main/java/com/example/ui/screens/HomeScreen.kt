package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateFilter
import com.example.data.model.ExpenseEntity
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.data.model.StockFilter
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.ZiaAmberWarning
import com.example.ui.theme.ZiaAmberWarningBg
import com.example.ui.theme.ZiaCashGreen
import com.example.ui.theme.ZiaCashGreenBg
import com.example.ui.theme.ZiaGreenPrimary
import com.example.ui.theme.ZiaUdhaarPurple
import com.example.ui.theme.ZiaUdhaarPurpleBg
import com.example.ui.theme.ZiaUdhaarRed
import com.example.ui.theme.ZiaUdhaarRedBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    profile: StoreProfile,
    products: List<ProductEntity>,
    customers: List<PartyEntity>,
    suppliers: List<PartyEntity>,
    transactions: List<TransactionEntity>,
    expenses: List<ExpenseEntity>,
    dateFilter: DateFilter,
    activitySearch: String,
    activityTypeFilter: String,
    isTimestampInFilter: (Long, DateFilter) -> Boolean,
    onActivitySearchChange: (String) -> Unit,
    onActivityTypeFilterChange: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onDrillDownProducts: (StockFilter) -> Unit,
    onDrillDownLedgers: () -> Unit,
    onViewTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    val showUrdu = profile.showRomanUrdu

    // Compute period-filtered financial metrics
    val periodTransactions by remember(transactions, dateFilter) {
        derivedStateOf {
            transactions.filter { isTimestampInFilter(it.timestamp, dateFilter) }
        }
    }

    val periodExpenses by remember(expenses, dateFilter) {
        derivedStateOf {
            expenses.filter { isTimestampInFilter(it.timestamp, dateFilter) }
        }
    }

    val totalSale by remember(periodTransactions) {
        derivedStateOf {
            periodTransactions.filter { it.type == TransactionType.SALE }.sumOf { it.totalAmount }
        }
    }
    val saleCash by remember(periodTransactions) {
        derivedStateOf {
            periodTransactions.filter { it.type == TransactionType.SALE }.sumOf { it.paidAmount }
        }
    }
    val saleUdhaar by remember(periodTransactions) {
        derivedStateOf {
            (totalSale - saleCash).coerceAtLeast(0.0)
        }
    }

    val totalPurchase by remember(periodTransactions) {
        derivedStateOf {
            periodTransactions.filter { it.type == TransactionType.PURCHASE }.sumOf { it.totalAmount }
        }
    }
    val purchaseCash by remember(periodTransactions) {
        derivedStateOf {
            periodTransactions.filter { it.type == TransactionType.PURCHASE }.sumOf { it.paidAmount }
        }
    }
    val purchaseUdhaar by remember(periodTransactions) {
        derivedStateOf {
            (totalPurchase - purchaseCash).coerceAtLeast(0.0)
        }
    }

    val recoveryToday by remember(periodTransactions) {
        derivedStateOf {
            periodTransactions.filter { it.type == TransactionType.RECOVERY }.sumOf { it.paidAmount }
        }
    }

    val paymentToday by remember(periodTransactions) {
        derivedStateOf {
            periodTransactions.filter { it.type == TransactionType.PAYMENT }.sumOf { it.paidAmount }
        }
    }

    val expenseToday by remember(periodExpenses) {
        derivedStateOf {
            periodExpenses.sumOf { it.amount }
        }
    }

    // Net Profit = (Sale - COGS) - Expense
    // Approximate COGS from transactions or 20% margin if items vary:
    val estimatedProfit by remember(totalSale, expenseToday) {
        derivedStateOf {
            (totalSale * 0.18 - expenseToday).coerceAtLeast(-50000.0)
        }
    }

    val cashInHand by remember(saleCash, recoveryToday, purchaseCash, paymentToday, expenseToday) {
        derivedStateOf {
            (saleCash + recoveryToday) - (purchaseCash + paymentToday + expenseToday)
        }
    }

    // Khata totals
    val totalReceivable by remember(customers) {
        derivedStateOf {
            customers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
        }
    }
    val totalPayable by remember(suppliers) {
        derivedStateOf {
            suppliers.filter { it.currentBalance < 0 }.sumOf { kotlin.math.abs(it.currentBalance) }
        }
    }

    // Inventory & Expiry Stats
    val totalItemsCount = products.size
    val lowStockCount = products.count { it.stockQty > 0 && it.stockQty <= it.lowStockThreshold }
    val outOfStockCount = products.count { it.stockQty <= 0 }
    val fifteenDays = 15 * 86400000L
    val now = System.currentTimeMillis()
    val nearExpiryCount = products.count { it.expiryDate != null && it.expiryDate in now..(now + fifteenDays) }

    // Stock Valuation
    val totalCostValuation = products.sumOf { it.stockQty * it.costPrice }
    val totalSaleValuation = products.sumOf { it.stockQty * it.salePrice }
    val totalMaalProfit = (totalSaleValuation - totalCostValuation).coerceAtLeast(0.0)

    // Filtered Recent Activity
    val filteredActivity by remember(transactions, activitySearch, activityTypeFilter) {
        derivedStateOf {
            transactions.filter { tx ->
                val matchesType = when (activityTypeFilter) {
                    "SALE" -> tx.type == TransactionType.SALE
                    "PURCHASE" -> tx.type == TransactionType.PURCHASE
                    "RECOVERY" -> tx.type == TransactionType.RECOVERY
                    "PAYMENT" -> tx.type == TransactionType.PAYMENT
                    else -> true
                }
                val matchesSearch = activitySearch.isBlank() ||
                    tx.partyName.contains(activitySearch, ignoreCase = true) ||
                    tx.invoiceNo.contains(activitySearch, ignoreCase = true)
                matchesType && matchesSearch
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Viewing Today Banner
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ZiaGreenPrimary.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ZiaGreenPrimary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Viewing: ${dateFilter.label} (${dateFilter.urduLabel})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ZiaGreenPrimary
                        )
                    )
                    Text(
                        text = "All values in PKR (Rs.)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Section A: Financial Summary (Maali Hisaab)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Financial Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (showUrdu) {
                        Text(
                            text = "Maali Hisaab",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ZiaGreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Button(
                    onClick = onAddExpenseClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ZiaAmberWarning),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_expense_quick_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (showUrdu) "+ Kharcha" else "+ Expense", fontSize = 12.sp)
                }
            }
        }

        // Financial Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Total Sale & Total Purchase
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard(
                        title = "Total Sale",
                        urduSubtitle = "Kul Farokht",
                        amount = "Rs. ${totalSale.toInt()}",
                        subInfo = "Cash: ${saleCash.toInt()} | Udhaar: ${saleUdhaar.toInt()}",
                        icon = Icons.Default.TrendingUp,
                        iconColor = ZiaCashGreen,
                        bgColor = ZiaCashGreenBg,
                        showUrdu = showUrdu,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Total Purchase",
                        urduSubtitle = "Kul Khareedari",
                        amount = "Rs. ${totalPurchase.toInt()}",
                        subInfo = "Cash: ${purchaseCash.toInt()} | Udhaar: ${purchaseUdhaar.toInt()}",
                        icon = Icons.Default.TrendingDown,
                        iconColor = Color(0xFF2563EB),
                        bgColor = Color(0xFFEFF6FF),
                        showUrdu = showUrdu,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Recovery Today & Payment Today
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard(
                        title = "Recovery",
                        urduSubtitle = "Grahak se Wasooli",
                        amount = "Rs. ${recoveryToday.toInt()}",
                        subInfo = "Udhaar wapas aaya",
                        icon = Icons.Default.ArrowDownward,
                        iconColor = ZiaCashGreen,
                        bgColor = ZiaCashGreenBg,
                        showUrdu = showUrdu,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Payment",
                        urduSubtitle = "Supplier ko Adaigi",
                        amount = "Rs. ${paymentToday.toInt()}",
                        subInfo = "Hisaab ada kiya",
                        icon = Icons.Default.ArrowUpward,
                        iconColor = ZiaUdhaarRed,
                        bgColor = ZiaUdhaarRedBg,
                        showUrdu = showUrdu,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Expense Today, Net Profit, Cash In Hand
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard(
                        title = "Expense",
                        urduSubtitle = "Dukan ke Ikhrajat",
                        amount = "Rs. ${expenseToday.toInt()}",
                        subInfo = "Rent, bijli, chai",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = ZiaAmberWarning,
                        bgColor = ZiaAmberWarningBg,
                        showUrdu = showUrdu,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Net Profit",
                        urduSubtitle = "Khalis Munafa",
                        amount = "Rs. ${estimatedProfit.toInt()}",
                        subInfo = "Sale - Kharche",
                        icon = Icons.Default.PriceCheck,
                        iconColor = ZiaGreenPrimary,
                        bgColor = ZiaGreenPrimary.copy(alpha = 0.12f),
                        showUrdu = showUrdu,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Cash in Hand",
                        urduSubtitle = "Mojooda Naqad",
                        amount = "Rs. ${cashInHand.toInt()}",
                        subInfo = "Galle me raqam",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = ZiaCashGreen,
                        bgColor = ZiaCashGreenBg,
                        showUrdu = showUrdu,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section B: Ledger Totals (Khata Summary)
        item {
            Column {
                Text(
                    text = "Khata Summary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (showUrdu) {
                    Text(
                        text = "Udhaar aur Baaqi Jat",
                        style = MaterialTheme.typography.bodySmall.copy(color = ZiaGreenPrimary, fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDrillDownLedgers() },
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Receivable
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ZiaUdhaarRedBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Receivable",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ZiaUdhaarRed)
                            )
                            if (showUrdu) {
                                Text(
                                    text = "Aap ne lena hai",
                                    style = MaterialTheme.typography.bodySmall.copy(color = ZiaUdhaarRed, fontWeight = FontWeight.SemiBold)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rs. ${totalReceivable.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZiaUdhaarRed
                                )
                            )
                            Text(
                                text = "${customers.count { it.currentBalance > 0 }} Grahak Baaqi",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                            )
                        }
                    }

                    // Payable
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ZiaUdhaarPurpleBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Payable",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ZiaUdhaarPurple)
                            )
                            if (showUrdu) {
                                Text(
                                    text = "Aap ne dena hai",
                                    style = MaterialTheme.typography.bodySmall.copy(color = ZiaUdhaarPurple, fontWeight = FontWeight.SemiBold)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rs. ${totalPayable.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZiaUdhaarPurple
                                )
                            )
                            Text(
                                text = "${suppliers.count { it.currentBalance < 0 }} Suppliers Baaqi",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                            )
                        }
                    }
                }
            }
        }

        // Section C: Inventory & Expiry (Maal ki Tafseel)
        item {
            Column {
                Text(
                    text = "Inventory & Expiry",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (showUrdu) {
                    Text(
                        text = "Maal ki Tafseel",
                        style = MaterialTheme.typography.bodySmall.copy(color = ZiaGreenPrimary, fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InventoryStatPill(
                        label = "Total Items",
                        urdu = "Kul Items",
                        count = totalItemsCount,
                        color = ZiaGreenPrimary,
                        onClick = { onDrillDownProducts(StockFilter.ALL) },
                        modifier = Modifier.weight(1f)
                    )
                    InventoryStatPill(
                        label = "Low Stock",
                        urdu = "Kam Stock",
                        count = lowStockCount,
                        color = Color(0xFFEA580C),
                        onClick = { onDrillDownProducts(StockFilter.LOW_STOCK) },
                        modifier = Modifier.weight(1f)
                    )
                    InventoryStatPill(
                        label = "Out Stock",
                        urdu = "Khatam",
                        count = outOfStockCount,
                        color = ZiaUdhaarRed,
                        onClick = { onDrillDownProducts(StockFilter.OUT_OF_STOCK) },
                        modifier = Modifier.weight(1f)
                    )
                    InventoryStatPill(
                        label = "Near Expiry",
                        urdu = "Expiry",
                        count = nearExpiryCount,
                        color = ZiaAmberWarning,
                        onClick = { onDrillDownProducts(StockFilter.NEAR_EXPIRY) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section D: Stock Valuation (Maal ki Qeemat)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Stock Valuation",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (showUrdu) {
                            Text(
                                text = "Maal ki Qeemat",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ZiaGreenPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total (Cost)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("Rs. ${totalCostValuation.toInt()}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Total (Sale)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("Rs. ${totalSaleValuation.toInt()}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Total (Maal Profit)", style = MaterialTheme.typography.labelSmall, color = ZiaGreenPrimary)
                            Text("Rs. ${totalMaalProfit.toInt()}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ZiaGreenPrimary))
                        }
                    }
                }
            }
        }

        // Section E: Recent Activity
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (showUrdu) {
                            Text(
                                text = "Taza Tareen Karobar",
                                style = MaterialTheme.typography.bodySmall.copy(color = ZiaGreenPrimary, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Box
                OutlinedTextField(
                    value = activitySearch,
                    onValueChange = onActivitySearchChange,
                    placeholder = { Text("Search by name or bill #...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recent_activity_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filterOptions = listOf(
                        Pair("ALL", "All"),
                        Pair("SALE", "Sales"),
                        Pair("PURCHASE", "Purchases"),
                        Pair("RECOVERY", "Recovery"),
                        Pair("PAYMENT", "Payment")
                    )
                    items(filterOptions) { (key, label) ->
                        FilterChip(
                            selected = activityTypeFilter == key,
                            onClick = { onActivityTypeFilterChange(key) },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Activity List Items
        if (filteredActivity.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Koi transaction nahi mili.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            }
        } else {
            items(filteredActivity.take(20), key = { it.id }) { tx ->
                val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                val formattedTime = dateFormat.format(Date(tx.timestamp))

                val (badgeColor, badgeBg, prefix) = when (tx.type) {
                    TransactionType.SALE -> Triple(ZiaCashGreen, ZiaCashGreenBg, "+")
                    TransactionType.PURCHASE -> Triple(Color(0xFF2563EB), Color(0xFFEFF6FF), "-")
                    TransactionType.RECOVERY -> Triple(ZiaCashGreen, ZiaCashGreenBg, "+")
                    TransactionType.PAYMENT -> Triple(ZiaUdhaarRed, ZiaUdhaarRedBg, "-")
                    TransactionType.SALE_RETURN -> Triple(ZiaUdhaarRed, ZiaUdhaarRedBg, "-")
                    TransactionType.PURCHASE_RETURN -> Triple(ZiaCashGreen, ZiaCashGreenBg, "+")
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_row_${tx.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tx.type.name,
                                color = badgeColor,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.partyName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "#${tx.invoiceNo} • $formattedTime",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Amount & Actions
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$prefix Rs. ${tx.totalAmount.toInt()}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onViewTransaction(tx) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Receipt,
                                        contentDescription = "View Bill",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteTransaction(tx.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    urduSubtitle: String,
    amount: String,
    subInfo: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    showUrdu: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (showUrdu) {
                        Text(
                            text = urduSubtitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = iconColor)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            )

            Text(
                text = subInfo,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InventoryStatPill(
    label: String,
    urdu: String,
    count: Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = urdu,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = Color.DarkGray
                )
            )
        }
    }
}
