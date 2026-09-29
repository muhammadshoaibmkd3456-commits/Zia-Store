package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateFilter
import com.example.data.model.ProductEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.ZiaCashGreen
import com.example.ui.theme.ZiaGreenPrimary

data class ProductAnalytics(
    val productId: Long,
    val productName: String,
    val totalQtySold: Double,
    val totalSaleRevenue: Double,
    val totalProfit: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceHubSheet(
    products: List<ProductEntity>,
    transactions: List<TransactionEntity>,
    transactionItems: List<TransactionItemEntity>,
    isTimestampInFilter: (Long, DateFilter) -> Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPeriod by remember { mutableStateOf(DateFilter.ALL_TIME) }

    // Filter transactions by selected period
    val periodTransactions by remember(transactions, selectedPeriod) {
        derivedStateOf {
            transactions.filter {
                it.type == TransactionType.SALE && isTimestampInFilter(it.timestamp, selectedPeriod)
            }
        }
    }

    val periodTxIds by remember(periodTransactions) {
        derivedStateOf { periodTransactions.map { it.id }.toSet() }
    }

    val rankedAnalytics by remember(transactionItems, periodTxIds, products) {
        derivedStateOf {
            val relevantItems = transactionItems.filter { it.transactionId in periodTxIds }
            val map = mutableMapOf<Long, ProductAnalytics>()

            for (item in relevantItems) {
                val existing = map[item.productId]
                val qty = item.qty
                val revenue = item.qty * item.unitPrice
                val profit = item.qty * (item.unitPrice - item.costAtSale)

                if (existing != null) {
                    map[item.productId] = existing.copy(
                        totalQtySold = existing.totalQtySold + qty,
                        totalSaleRevenue = existing.totalSaleRevenue + revenue,
                        totalProfit = existing.totalProfit + profit
                    )
                } else {
                    map[item.productId] = ProductAnalytics(
                        productId = item.productId,
                        productName = item.productName,
                        totalQtySold = qty,
                        totalSaleRevenue = revenue,
                        totalProfit = profit
                    )
                }
            }

            // Fallback for products if not sold yet
            for (p in products) {
                if (!map.containsKey(p.id)) {
                    map[p.id] = ProductAnalytics(
                        productId = p.id,
                        productName = p.name,
                        totalQtySold = 0.0,
                        totalSaleRevenue = 0.0,
                        totalProfit = 0.0
                    )
                }
            }

            map.values.toList()
        }
    }

    val topSelling = remember(rankedAnalytics) {
        rankedAnalytics.sortedByDescending { it.totalQtySold }
    }

    val topProfitable = remember(rankedAnalytics) {
        rankedAnalytics.sortedByDescending { it.totalProfit }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = null,
                        tint = ZiaGreenPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Performance Hub",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Period chips: All Time, Today, Yesterday, This Month
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DateFilter.values()) { filter ->
                    FilterChip(
                        selected = selectedPeriod == filter,
                        onClick = { selectedPeriod = filter },
                        label = { Text(filter.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
            ) {
                // Section 1: Most Selling Items (Blue Card)
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Most Selling Items (Zyada Bikne Wala)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D4ED8)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            topSelling.take(4).forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2563EB)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${index + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                                        Text("Sale: Rs. ${item.totalSaleRevenue.toInt()} • Profit: Rs. ${item.totalProfit.toInt()}", fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                    Text("${item.totalQtySold.toInt()} sold", fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // Section 2: Most Profitable Items (Green Card)
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Most Profitable Items (Bara Munafa)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            topProfitable.take(4).forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(ZiaCashGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${index + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                                        Text("Revenue: Rs. ${item.totalSaleRevenue.toInt()}", fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                    Text("+ Rs. ${item.totalProfit.toInt()}", fontWeight = FontWeight.Bold, color = ZiaCashGreen, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
