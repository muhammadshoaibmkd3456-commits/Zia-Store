package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PartyEntity
import com.example.ui.theme.ZiaCashGreen
import com.example.ui.theme.ZiaUdhaarPurple
import com.example.ui.theme.ZiaUdhaarRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UdhaarAccountPickerSheet(
    customers: List<PartyEntity>,
    onSelectCustomer: (PartyEntity, Double) -> Unit,
    onAddNewCustomer: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var cashPaidTodayInput by remember { mutableStateOf("0") }
    var selectedCustomer by remember { mutableStateOf<PartyEntity?>(null) }

    val filteredCustomers = customers.filter {
        searchQuery.isBlank() ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery, ignoreCase = true)
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
                Text(
                    text = "Select Udhaar Account",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cash Paid Today (Optional)
            OutlinedTextField(
                value = cashPaidTodayInput,
                onValueChange = { cashPaidTodayInput = it },
                label = { Text("Cash Paid Today (Optional)") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cash_paid_today_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Customer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search customer name or phone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("udhaar_search_customer_input")
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onAddNewCustomer,
                    modifier = Modifier.testTag("add_new_customer_shortcut")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Customer", tint = ZiaUdhaarPurple)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                if (filteredCustomers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Koi grahak nahi mila. Naya grahak shamil karein.", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        val isSelected = selectedCustomer?.id == customer.id
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) ZiaUdhaarPurple.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, ZiaUdhaarPurple) else CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCustomer = customer }
                                .testTag("udhaar_customer_item_${customer.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(ZiaUdhaarPurple.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = ZiaUdhaarPurple
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = customer.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (customer.phone.isNotEmpty()) {
                                        Text(
                                            text = customer.phone,
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val (balColor, balText) = if (customer.currentBalance > 0) {
                                        Pair(ZiaUdhaarRed, "Lena Hai: Rs. ${customer.currentBalance.toInt()}")
                                    } else if (customer.currentBalance < 0) {
                                        Pair(ZiaUdhaarPurple, "Dena Hai: Rs. ${kotlin.math.abs(customer.currentBalance).toInt()}")
                                    } else {
                                        Pair(ZiaCashGreen, "Clear (0)")
                                    }
                                    Text(
                                        text = balText,
                                        color = balColor,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Confirm Udhaar Sale Button
            Button(
                onClick = {
                    selectedCustomer?.let { cust ->
                        val cashPaid = cashPaidTodayInput.toDoubleOrNull() ?: 0.0
                        onSelectCustomer(cust, cashPaid)
                    }
                },
                enabled = selectedCustomer != null,
                colors = ButtonDefaults.buttonColors(containerColor = ZiaUdhaarPurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_udhaar_sale_button")
            ) {
                Text(
                    text = if (selectedCustomer != null) "Confirm Udhaar to ${selectedCustomer?.name}" else "Select a Customer",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
