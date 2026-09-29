package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.data.model.StockFilter
import com.example.data.model.StoreProfile
import com.example.ui.theme.ZiaAmberWarning
import com.example.ui.theme.ZiaAmberWarningBg
import com.example.ui.theme.ZiaCashGreen
import com.example.ui.theme.ZiaGreenPrimary
import com.example.ui.theme.ZiaUdhaarRed
import com.example.ui.theme.ZiaUdhaarRedBg
import com.example.ui.theme.ZiaWholesaleCrimson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    profile: StoreProfile,
    products: List<ProductEntity>,
    searchQuery: String,
    selectedStockFilter: StockFilter,
    selectedCategory: String,
    showAddProductSheet: Boolean,
    productToEdit: ProductEntity?,
    prefilledBarcode: String?,
    onSearchChange: (String) -> Unit,
    onStockFilterChange: (StockFilter) -> Unit,
    onCategoryChange: (String) -> Unit,
    onOpenAddProduct: (Boolean, ProductEntity?, String?) -> Unit,
    onOpenScanner: () -> Unit,
    onOpenPerformanceHub: () -> Unit,
    onSaveProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit
) {
    val showUrdu = profile.showRomanUrdu
    val now = System.currentTimeMillis()
    val fifteenDays = 15 * 86400000L

    val categories = remember(products) {
        listOf("All Items") + products.map { it.category }.distinct()
    }

    // Filter products
    val filteredProducts = remember(products, searchQuery, selectedStockFilter, selectedCategory) {
        products.filter { p ->
            val matchSearch = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true)

            val matchCategory = selectedCategory == "All Items" || p.category.equals(selectedCategory, ignoreCase = true)

            val matchStock = when (selectedStockFilter) {
                StockFilter.ALL -> true
                StockFilter.OUT_OF_STOCK -> p.stockQty <= 0
                StockFilter.LOW_STOCK -> p.stockQty > 0 && p.stockQty <= p.lowStockThreshold
                StockFilter.NEAR_EXPIRY -> p.expiryDate != null && p.expiryDate in now..(now + fifteenDays)
            }

            matchSearch && matchCategory && matchStock
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Products Header: Title & Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Products (${products.size})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (showUrdu) {
                    Text(
                        text = "Maal ki Fehrist",
                        style = MaterialTheme.typography.bodySmall.copy(color = ZiaGreenPrimary, fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Performance Hub button
                OutlinedButton(
                    onClick = onOpenPerformanceHub,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("performance_hub_button")
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hub", fontSize = 12.sp)
                }

                // Add Item button
                Button(
                    onClick = { onOpenAddProduct(true, null, null) },
                    colors = ButtonDefaults.buttonColors(containerColor = ZiaGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_product_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (showUrdu) "+ Naya Item" else "+ Add Item", fontSize = 12.sp)
                }
            }
        }

        // Search Field with Scanner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search product name or barcode...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(
                        onClick = onOpenScanner,
                        modifier = Modifier.testTag("product_scanner_icon")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = ZiaGreenPrimary)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_search_input")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stock Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(StockFilter.values()) { filter ->
                FilterChip(
                    selected = selectedStockFilter == filter,
                    onClick = { onStockFilterChange(filter) },
                    label = {
                        Text(
                            text = if (showUrdu) "${filter.label} (${filter.urduLabel})" else filter.label,
                            fontSize = 11.sp
                        )
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Category Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(category) },
                    label = { Text(category, fontSize = 11.sp) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Product Rows List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Koi item nahi mila.", color = Color.Gray)
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    val isNearExpiry = product.expiryDate != null && product.expiryDate in now..(now + fifteenDays)

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_row_${product.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar Icon
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ZiaGreenPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = product.name.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = ZiaGreenPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Product details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "Barcode: ${product.barcode} • ${product.category}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Price & Stock Tags
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Sale Price
                                    Surface(
                                        color = ZiaCashGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Sale: Rs. ${product.salePrice.toInt()}",
                                            color = ZiaCashGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Wholesale
                                    Surface(
                                        color = ZiaWholesaleCrimson.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Thok: Rs. ${product.wholesalePrice.toInt()}",
                                            color = ZiaWholesaleCrimson,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Cost
                                    Surface(
                                        color = Color.LightGray.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Cost: ${product.costPrice.toInt()}",
                                            color = Color.DarkGray,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Stock tag & Expiry
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val (stockColor, stockLabel) = when {
                                        product.stockQty <= 0 -> Pair(ZiaUdhaarRed, "Out of Stock (0)")
                                        product.stockQty <= product.lowStockThreshold -> Pair(Color(0xFFEA580C), "Low: ${product.stockQty.toInt()} ${product.unit}")
                                        else -> Pair(ZiaGreenPrimary, "Stock: ${product.stockQty.toInt()} ${product.unit}")
                                    }

                                    Text(
                                        text = stockLabel,
                                        color = stockColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    if (isNearExpiry) {
                                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        Text(
                                            text = "Expiry: ${dateFormat.format(Date(product.expiryDate ?: 0))}",
                                            color = ZiaAmberWarning,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Edit & Delete actions
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onOpenAddProduct(true, product, null) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { onDeleteProduct(product) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Product Bottom Sheet
    if (showAddProductSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val isEditing = productToEdit != null

        var name by remember { mutableStateOf(productToEdit?.name ?: "") }
        var barcode by remember {
            mutableStateOf(
                productToEdit?.barcode ?: prefilledBarcode ?: "896400" + (100000..999999).random()
            )
        }
        var category by remember { mutableStateOf(productToEdit?.category ?: "Kiryana") }
        var unit by remember { mutableStateOf(productToEdit?.unit ?: "piece") }
        var salePriceInput by remember { mutableStateOf(productToEdit?.salePrice?.toInt()?.toString() ?: "") }
        var wholesalePriceInput by remember { mutableStateOf(productToEdit?.wholesalePrice?.toInt()?.toString() ?: "") }
        var costPriceInput by remember { mutableStateOf(productToEdit?.costPrice?.toInt()?.toString() ?: "") }
        var stockQtyInput by remember { mutableStateOf(productToEdit?.stockQty?.toInt()?.toString() ?: "10") }
        var lowThresholdInput by remember { mutableStateOf(productToEdit?.lowStockThreshold?.toInt()?.toString() ?: "5") }

        ModalBottomSheet(
            onDismissRequest = { onOpenAddProduct(false, null, null) },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Item" else "Add New Product",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name (Item ka Naam)") },
                    placeholder = { Text("e.g. Brufen 200mg or Tapal Tea") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode / SKU") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_barcode_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onOpenScanner) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode", tint = ZiaGreenPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_category_input")
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        placeholder = { Text("piece/kg/packet") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Prices: Sale, Wholesale, Cost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = salePriceInput,
                        onValueChange = { salePriceInput = it },
                        label = { Text("Sale Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_sale_price_input")
                    )

                    OutlinedTextField(
                        value = wholesalePriceInput,
                        onValueChange = { wholesalePriceInput = it },
                        label = { Text("Wholesale") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_wholesale_price_input")
                    )

                    OutlinedTextField(
                        value = costPriceInput,
                        onValueChange = { costPriceInput = it },
                        label = { Text("Cost Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_cost_price_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stock & Low stock threshold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockQtyInput,
                        onValueChange = { stockQtyInput = it },
                        label = { Text("Current Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_stock_qty_input")
                    )

                    OutlinedTextField(
                        value = lowThresholdInput,
                        onValueChange = { lowThresholdInput = it },
                        label = { Text("Low Stock Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_low_threshold_input")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val sPrice = salePriceInput.toDoubleOrNull() ?: 0.0
                        val wPrice = wholesalePriceInput.toDoubleOrNull() ?: sPrice
                        val cPrice = costPriceInput.toDoubleOrNull() ?: (sPrice * 0.8)
                        val sQty = stockQtyInput.toDoubleOrNull() ?: 0.0
                        val lThresh = lowThresholdInput.toDoubleOrNull() ?: 5.0

                        val toSave = ProductEntity(
                            id = productToEdit?.id ?: 0L,
                            name = name.ifBlank { "Item ${barcode.takeLast(4)}" },
                            barcode = barcode.trim(),
                            category = category.ifBlank { "General" },
                            salePrice = sPrice,
                            wholesalePrice = wPrice,
                            costPrice = cPrice,
                            stockQty = sQty,
                            lowStockThreshold = lThresh,
                            unit = unit.ifBlank { "piece" },
                            expiryDate = productToEdit?.expiryDate
                        )
                        onSaveProduct(toSave)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZiaGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_product_button")
                ) {
                    Text(
                        text = if (isEditing) "Update Item" else "Save Product (Mehfooz Karein)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
