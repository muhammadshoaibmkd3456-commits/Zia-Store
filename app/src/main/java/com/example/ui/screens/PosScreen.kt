package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.PosMode
import com.example.data.model.ProductEntity
import com.example.data.model.StoreProfile
import com.example.ui.theme.ZiaCashGreen
import com.example.ui.theme.ZiaGreenPrimary
import com.example.ui.theme.ZiaUdhaarPurple
import com.example.ui.theme.ZiaUdhaarRed
import com.example.ui.theme.ZiaWholesaleCrimson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    profile: StoreProfile,
    products: List<ProductEntity>,
    posMode: PosMode,
    cartItems: List<CartItem>,
    isWholesale: Boolean,
    selectedCategory: String,
    searchQuery: String,
    showReviewSheet: Boolean,
    onPosModeChange: (PosMode) -> Unit,
    onToggleWholesale: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onUpdateCartQty: (Long, Double) -> Unit,
    onUpdateCartPrice: (Long, Double) -> Unit,
    onRemoveCartItem: (Long) -> Unit,
    onClearCart: () -> Unit,
    onOpenScanner: () -> Unit,
    onOpenReviewSheet: (Boolean) -> Unit,
    onCheckoutCash: (Double) -> Unit,
    onCheckoutWhatsApp: () -> Unit,
    onCheckoutUdhaar: () -> Unit,
    onCheckoutPurchase: (Double) -> Unit,
    onCheckoutReturn: (Double) -> Unit
) {
    val showUrdu = profile.showRomanUrdu

    // Categories derived
    val categories = remember(products) {
        listOf("All Items") + products.map { it.category }.distinct()
    }

    // Filtered products for grid
    val filteredProducts = remember(products, selectedCategory, searchQuery) {
        products.filter { p ->
            val matchCat = selectedCategory == "All Items" || p.category.equals(selectedCategory, ignoreCase = true)
            val matchSearch = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true)
            matchCat && matchSearch
        }
    }

    val totalCartQty = cartItems.sumOf { it.qty }
    val totalCartAmount = cartItems.sumOf { it.lineTotal }

    // External Scanner / Typed Barcode Match
    fun handleSearchInput(input: String) {
        onSearchChange(input)
        // Instant barcode matching for handheld USB/Bluetooth scanners
        val trimmed = input.trim()
        val exactMatch = products.find { it.barcode.equals(trimmed, ignoreCase = true) }
        if (exactMatch != null && trimmed.length >= 6) {
            onAddToCart(exactMatch)
            onSearchChange("")
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 700.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top Mode Tabs: Sale, Purchase, Return
            TabRow(
                selectedTabIndex = posMode.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ZiaGreenPrimary
            ) {
                PosMode.values().forEach { mode ->
                    Tab(
                        selected = posMode == mode,
                        onClick = { onPosModeChange(mode) },
                        text = {
                            Text(
                                text = if (showUrdu) "${mode.label} (${mode.urduLabel})" else mode.label,
                                fontWeight = if (posMode == mode) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            if (isWideScreen) {
                // Wide Screen Tablet / Landscape Layout: 2-Pane Split View
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // Left Pane: Catalog & Grid
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                    ) {
                        PosSearchAndCategoryHeader(
                            searchQuery = searchQuery,
                            onSearchChange = { handleSearchInput(it) },
                            onSearchDone = {
                                val match = products.find { it.barcode.equals(searchQuery.trim(), ignoreCase = true) }
                                if (match != null) {
                                    onAddToCart(match)
                                    onSearchChange("")
                                }
                            },
                            onOpenScanner = onOpenScanner,
                            posMode = posMode,
                            isWholesale = isWholesale,
                            onToggleWholesale = onToggleWholesale,
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategoryChange = onCategoryChange
                        )

                        ProductGrid(
                            filteredProducts = filteredProducts,
                            cartItems = cartItems,
                            posMode = posMode,
                            isWholesale = isWholesale,
                            onAddToCart = onAddToCart,
                            minGridSize = 110.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Right Pane: Permanent Side Cart & Review Panel
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        CartReviewContent(
                            cartItems = cartItems,
                            posMode = posMode,
                            totalCartAmount = totalCartAmount,
                            onUpdateCartQty = onUpdateCartQty,
                            onRemoveCartItem = onRemoveCartItem,
                            onClearCart = onClearCart,
                            onCheckoutCash = onCheckoutCash,
                            onCheckoutWhatsApp = onCheckoutWhatsApp,
                            onCheckoutUdhaar = onCheckoutUdhaar,
                            onCheckoutPurchase = onCheckoutPurchase,
                            onCheckoutReturn = onCheckoutReturn,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                // Mobile / Portrait Layout: Grid with Sticky Bottom Bar
                Column(modifier = Modifier.fillMaxSize()) {
                    PosSearchAndCategoryHeader(
                        searchQuery = searchQuery,
                        onSearchChange = { handleSearchInput(it) },
                        onSearchDone = {
                            val match = products.find { it.barcode.equals(searchQuery.trim(), ignoreCase = true) }
                            if (match != null) {
                                onAddToCart(match)
                                onSearchChange("")
                            }
                        },
                        onOpenScanner = onOpenScanner,
                        posMode = posMode,
                        isWholesale = isWholesale,
                        onToggleWholesale = onToggleWholesale,
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategoryChange = onCategoryChange
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        ProductGrid(
                            filteredProducts = filteredProducts,
                            cartItems = cartItems,
                            posMode = posMode,
                            isWholesale = isWholesale,
                            onAddToCart = onAddToCart,
                            minGridSize = 100.dp,
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
                            modifier = Modifier.fillMaxSize()
                        )

                        // Sticky Bottom Checkout Bar
                        if (cartItems.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp,
                                shadowElevation = 10.dp,
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Total Qty: ${totalCartQty.toInt()} items",
                                            style = MaterialTheme.typography.labelMedium.copy(color = Color.DarkGray)
                                        )
                                        Text(
                                            text = "Rs. ${totalCartAmount.toInt()}",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ZiaGreenPrimary
                                            )
                                        )
                                    }

                                    Button(
                                        onClick = { onOpenReviewSheet(true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ZiaGreenPrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("pos_checkout_button")
                                    ) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Checkout (${cartItems.size})", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Review Items Modal Bottom Sheet on Mobile
                if (showReviewSheet) {
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                    ModalBottomSheet(
                        onDismissRequest = { onOpenReviewSheet(false) },
                        sheetState = sheetState,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    ) {
                        CartReviewContent(
                            cartItems = cartItems,
                            posMode = posMode,
                            totalCartAmount = totalCartAmount,
                            onUpdateCartQty = onUpdateCartQty,
                            onRemoveCartItem = onRemoveCartItem,
                            onClearCart = onClearCart,
                            onCheckoutCash = onCheckoutCash,
                            onCheckoutWhatsApp = onCheckoutWhatsApp,
                            onCheckoutUdhaar = onCheckoutUdhaar,
                            onCheckoutPurchase = onCheckoutPurchase,
                            onCheckoutReturn = onCheckoutReturn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PosSearchAndCategoryHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearchDone: () -> Unit,
    onOpenScanner: () -> Unit,
    posMode: PosMode,
    isWholesale: Boolean,
    onToggleWholesale: () -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search item or scan barcode...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(
                        onClick = onOpenScanner,
                        modifier = Modifier.testTag("pos_barcode_scan_icon")
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Camera Barcode Scanner",
                            tint = ZiaGreenPrimary
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchDone() }),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pos_search_input")
            )

            if (posMode == PosMode.SALE) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isWholesale) ZiaWholesaleCrimson.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isWholesale) androidx.compose.foundation.BorderStroke(1.dp, ZiaWholesaleCrimson) else null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToggleWholesale() }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("wholesale_mode_toggle")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Wholesale",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWholesale) ZiaWholesaleCrimson else Color.DarkGray
                        )
                        Text(
                            text = if (isWholesale) "ON (Thok)" else "OFF",
                            fontSize = 9.sp,
                            color = if (isWholesale) ZiaWholesaleCrimson else Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Category Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(category) },
                    label = { Text(category, fontSize = 12.sp) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ProductGrid(
    filteredProducts: List<ProductEntity>,
    cartItems: List<CartItem>,
    posMode: PosMode,
    isWholesale: Boolean,
    onAddToCart: (ProductEntity) -> Unit,
    minGridSize: androidx.compose.ui.unit.Dp,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    modifier: Modifier = Modifier
) {
    if (filteredProducts.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Koi item nahi mila.", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minGridSize),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
        ) {
            items(filteredProducts, key = { it.id }) { product ->
                val priceToDisplay = when {
                    posMode == PosMode.PURCHASE -> product.costPrice
                    isWholesale -> product.wholesalePrice
                    else -> product.salePrice
                }
                val inCartQty = cartItems.find { it.product.id == product.id }?.qty ?: 0.0

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddToCart(product) }
                        .testTag("product_grid_item_${product.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ZiaGreenPrimary.copy(alpha = 0.09f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = product.name.take(2).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = ZiaGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "Rs. ${priceToDisplay.toInt()}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isWholesale) ZiaWholesaleCrimson else ZiaGreenPrimary
                            )
                        )

                        val (stockText, stockColor) = when {
                            product.stockQty <= 0 -> Pair("Out of stock", ZiaUdhaarRed)
                            product.stockQty <= product.lowStockThreshold -> Pair("Low (${product.stockQty.toInt()})", Color(0xFFEA580C))
                            else -> Pair("Stock: ${product.stockQty.toInt()}", Color.Gray)
                        }

                        Text(
                            text = stockText,
                            fontSize = 10.sp,
                            color = stockColor
                        )

                        if (inCartQty > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ZiaGreenPrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "In Cart: ${inCartQty.toInt()}",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartReviewContent(
    cartItems: List<CartItem>,
    posMode: PosMode,
    totalCartAmount: Double,
    onUpdateCartQty: (Long, Double) -> Unit,
    onRemoveCartItem: (Long) -> Unit,
    onClearCart: () -> Unit,
    onCheckoutCash: (Double) -> Unit,
    onCheckoutWhatsApp: () -> Unit,
    onCheckoutUdhaar: () -> Unit,
    onCheckoutPurchase: (Double) -> Unit,
    onCheckoutReturn: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var discountInput by remember { mutableStateOf("0") }
    var isDiscountPercent by remember { mutableStateOf(false) }

    val discountAmount = remember(discountInput, isDiscountPercent, totalCartAmount) {
        val raw = discountInput.toDoubleOrNull() ?: 0.0
        if (isDiscountPercent) {
            (totalCartAmount * (raw / 100.0)).coerceIn(0.0, totalCartAmount)
        } else {
            raw.coerceIn(0.0, totalCartAmount)
        }
    }
    val netPayable = (totalCartAmount - discountAmount).coerceAtLeast(0.0)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cart Items (${cartItems.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (cartItems.isNotEmpty()) {
                TextButton(onClick = onClearCart) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ZiaUdhaarRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear", color = ZiaUdhaarRed, fontSize = 12.sp)
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 6.dp))

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Cart khali hai. Item tap karke add karein.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .height(220.dp)
            ) {
                items(cartItems, key = { it.product.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.product.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Rs. ${item.customPrice.toInt()} / ${item.product.unit}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onUpdateCartQty(item.product.id, -1.0) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(15.dp))
                            }
                            Text(
                                text = "${item.qty.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = { onUpdateCartQty(item.product.id, 1.0) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(15.dp))
                            }
                        }

                        Text(
                            text = "Rs. ${item.lineTotal.toInt()}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.width(60.dp),
                            textAlign = TextAlign.End
                        )

                        IconButton(
                            onClick = { onRemoveCartItem(item.product.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Discount Controls
        if (posMode == PosMode.SALE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Discount:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = !isDiscountPercent,
                    onClick = { isDiscountPercent = false },
                    label = { Text("Rs.", fontSize = 11.sp) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                FilterChip(
                    selected = isDiscountPercent,
                    onClick = { isDiscountPercent = true },
                    label = { Text("%", fontSize = 11.sp) }
                )
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedTextField(
                    value = discountInput,
                    onValueChange = { discountInput = it },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("discount_input")
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Net Amount Summary
        Surface(
            color = ZiaGreenPrimary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Net Bill", style = MaterialTheme.typography.labelSmall)
                    if (discountAmount > 0) {
                        Text("Discount: -Rs. ${discountAmount.toInt()}", color = ZiaUdhaarRed, fontSize = 10.sp)
                    }
                }
                Text(
                    text = "Rs. ${netPayable.toInt()}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ZiaGreenPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (posMode == PosMode.SALE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onCheckoutCash(discountAmount) },
                    enabled = cartItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = ZiaCashGreen),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("checkout_full_cash_button")
                ) {
                    Text("Full Cash", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onCheckoutWhatsApp,
                    enabled = cartItems.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("checkout_whatsapp_button")
                ) {
                    Text("WhatsApp", fontSize = 12.sp)
                }

                Button(
                    onClick = onCheckoutUdhaar,
                    enabled = cartItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = ZiaUdhaarPurple),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("checkout_udhaar_button")
                ) {
                    Text("Udhaar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        } else if (posMode == PosMode.PURCHASE) {
            Button(
                onClick = { onCheckoutPurchase(totalCartAmount) },
                enabled = cartItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = ZiaGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_purchase_button")
            ) {
                Text("Record Purchase & Add to Stock", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = { onCheckoutReturn(totalCartAmount) },
                enabled = cartItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = ZiaUdhaarRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_return_button")
            ) {
                Text("Process Return & Adjust Balance", fontWeight = FontWeight.Bold)
            }
        }
    }
}
