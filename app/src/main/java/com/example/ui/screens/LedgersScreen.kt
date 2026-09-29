package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PartyEntity
import com.example.data.model.PartyType
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.ZiaCashGreen
import com.example.ui.theme.ZiaGreenPrimary
import com.example.ui.theme.ZiaUdhaarPurple
import com.example.ui.theme.ZiaUdhaarPurpleBg
import com.example.ui.theme.ZiaUdhaarRed
import com.example.ui.theme.ZiaUdhaarRedBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgersScreen(
    profile: StoreProfile,
    customers: List<PartyEntity>,
    suppliers: List<PartyEntity>,
    selectedTab: PartyType,
    searchQuery: String,
    activePartyId: Long?,
    transactions: List<TransactionEntity>,
    showAddPartySheet: Boolean,
    partyToEdit: PartyEntity?,
    showPartyRecoveryDialog: Boolean, // Maine Liye
    showPartyPaymentDialog: Boolean,  // Maine Diye
    showStatementDialog: Boolean,
    onTabChange: (PartyType) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectParty: (Long?) -> Unit,
    onOpenAddParty: (Boolean, PartyEntity?) -> Unit,
    onSaveParty: (PartyEntity) -> Unit,
    onDeleteParty: (PartyEntity) -> Unit,
    onOpenRecoveryDialog: (Boolean) -> Unit,
    onOpenPaymentDialog: (Boolean) -> Unit,
    onOpenStatementDialog: (Boolean) -> Unit,
    onRecordMaineLiye: (PartyEntity, Double, String) -> Unit,
    onRecordMaineDiye: (PartyEntity, Double, String) -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    val context = LocalContext.current
    val showUrdu = profile.showRomanUrdu

    // If an active party is selected, render Party Detail Screen with BackHandler
    if (activePartyId != null) {
        val allParties = if (selectedTab == PartyType.CUSTOMER) customers else suppliers
        val party = allParties.find { it.id == activePartyId } ?: customers.find { it.id == activePartyId } ?: suppliers.find { it.id == activePartyId }

        if (party != null) {
            BackHandler { onSelectParty(null) }

            val partyTransactions = remember(transactions, party.id) {
                transactions.filter { it.partyId == party.id }
            }

            PartyDetailView(
                profile = profile,
                party = party,
                transactions = partyTransactions,
                onBack = { onSelectParty(null) },
                onMaineLiyeClick = { onOpenRecoveryDialog(true) },
                onMaineDiyeClick = { onOpenPaymentDialog(true) },
                onStatementClick = { onOpenStatementDialog(true) },
                onDeleteTransaction = onDeleteTransaction
            )

            // Maine Liye Dialog (Recovery)
            if (showPartyRecoveryDialog) {
                var amountInput by remember { mutableStateOf("") }
                var notesInput by remember { mutableStateOf("") }

                Dialog(onDismissRequest = { onOpenRecoveryDialog(false) }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "+ Maine Liye (Wasooli)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ZiaCashGreen)
                            )
                            Text(
                                text = "${party.name} se raqam wasool hui",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = amountInput,
                                onValueChange = { amountInput = it },
                                label = { Text("Wasool Shuda Raqam (Rs.)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("maine_liye_amount_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = notesInput,
                                onValueChange = { notesInput = it },
                                label = { Text("Tafseel / Notes (Optional)") },
                                placeholder = { Text("Cash wasooli") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onOpenRecoveryDialog(false) }) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val amt = amountInput.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            onRecordMaineLiye(party, amt, notesInput)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ZiaCashGreen),
                                    modifier = Modifier.testTag("confirm_maine_liye_button")
                                ) {
                                    Text("Wasool Karein")
                                }
                            }
                        }
                    }
                }
            }

            // Maine Diye Dialog (Payment / Credit)
            if (showPartyPaymentDialog) {
                var amountInput by remember { mutableStateOf("") }
                var notesInput by remember { mutableStateOf("") }

                Dialog(onDismissRequest = { onOpenPaymentDialog(false) }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "- Maine Diye (Adaigi / Udhaar)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ZiaUdhaarRed)
                            )
                            Text(
                                text = "${party.name} ko raqam di ya udhaar diya",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = amountInput,
                                onValueChange = { amountInput = it },
                                label = { Text("Di Gayi Raqam (Rs.)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("maine_diye_amount_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = notesInput,
                                onValueChange = { notesInput = it },
                                label = { Text("Tafseel / Notes (Optional)") },
                                placeholder = { Text("Payment / loan") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onOpenPaymentDialog(false) }) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val amt = amountInput.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            onRecordMaineDiye(party, amt, notesInput)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ZiaUdhaarRed),
                                    modifier = Modifier.testTag("confirm_maine_diye_button")
                                ) {
                                    Text("Ada Karein")
                                }
                            }
                        }
                    }
                }
            }

            // Statement Modal
            if (showStatementDialog) {
                PartyStatementDialog(
                    profile = profile,
                    party = party,
                    transactions = partyTransactions,
                    onDismiss = { onOpenStatementDialog(false) }
                )
            }

            return
        }
    }

    // Party List View (Customers or Suppliers)
    val partiesList = if (selectedTab == PartyType.CUSTOMER) customers else suppliers
    val filteredParties = remember(partiesList, searchQuery) {
        partiesList.filter {
            searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ledgers (Khata)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (showUrdu) {
                    Text(
                        text = "Grahak aur Suppliers ka hisaab",
                        style = MaterialTheme.typography.bodySmall.copy(color = ZiaGreenPrimary, fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Button(
                onClick = { onOpenAddParty(true, null) },
                colors = ButtonDefaults.buttonColors(containerColor = ZiaGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_party_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (showUrdu) "+ Naya Khata" else "+ Add Party", fontSize = 12.sp)
            }
        }

        // Segmented Tabs: Customers & Suppliers
        TabRow(
            selectedTabIndex = if (selectedTab == PartyType.CUSTOMER) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = ZiaGreenPrimary
        ) {
            Tab(
                selected = selectedTab == PartyType.CUSTOMER,
                onClick = { onTabChange(PartyType.CUSTOMER) },
                text = { Text("Customers (${customers.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == PartyType.SUPPLIER,
                onClick = { onTabChange(PartyType.SUPPLIER) },
                text = { Text("Suppliers (${suppliers.size})", fontWeight = FontWeight.Bold) }
            )
        }

        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by party name or phone...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ledger_search_input")
            )
        }

        // Parties List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredParties.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Koi party nahi mili. Nayi party shamil karein.", color = Color.Gray)
                    }
                }
            } else {
                items(filteredParties, key = { it.id }) { party ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectParty(party.id) }
                            .testTag("party_row_${party.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedTab == PartyType.CUSTOMER) ZiaUdhaarPurple.copy(alpha = 0.15f) else Color(0xFF2563EB).copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = party.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == PartyType.CUSTOMER) ZiaUdhaarPurple else Color(0xFF2563EB),
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = party.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (party.phone.isNotEmpty()) {
                                    Text(
                                        text = party.phone,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                                    )
                                }
                            }

                            // Balance label
                            Column(horizontalAlignment = Alignment.End) {
                                val (balColor, balLabel, amt) = if (party.currentBalance > 0) {
                                    Triple(ZiaUdhaarRed, "Lena Hai", party.currentBalance.toInt())
                                } else if (party.currentBalance < 0) {
                                    Triple(ZiaUdhaarPurple, "Dena Hai", kotlin.math.abs(party.currentBalance).toInt())
                                } else {
                                    Triple(ZiaCashGreen, "Barabar", 0)
                                }

                                Text(
                                    text = "Rs. $amt",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = balColor
                                    )
                                )
                                Text(
                                    text = balLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = balColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Party Sheet
    if (showAddPartySheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var name by remember { mutableStateOf(partyToEdit?.name ?: "") }
        var phone by remember { mutableStateOf(partyToEdit?.phone ?: "") }
        var address by remember { mutableStateOf(partyToEdit?.address ?: "") }
        var openingBalInput by remember { mutableStateOf(partyToEdit?.openingBalance?.toInt()?.toString() ?: "0") }
        var partyType by remember { mutableStateOf(selectedTab) }

        ModalBottomSheet(
            onDismissRequest = { onOpenAddParty(false, null) },
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
                    text = if (partyToEdit != null) "Edit Party" else "Add New Party (Naya Khata)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = partyType == PartyType.CUSTOMER,
                        onClick = { partyType = PartyType.CUSTOMER },
                        label = { Text("Customer (Grahak)") }
                    )
                    FilterChip(
                        selected = partyType == PartyType.SUPPLIER,
                        onClick = { partyType = PartyType.SUPPLIER },
                        label = { Text("Supplier (Vendor)") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Party Name (Naam)") },
                    placeholder = { Text("e.g. Haji Abdul Rasheed") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("0300-1234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_phone_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = openingBalInput,
                    onValueChange = { openingBalInput = it },
                    label = { Text("Opening Balance (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val ob = openingBalInput.toDoubleOrNull() ?: 0.0
                        val party = PartyEntity(
                            id = partyToEdit?.id ?: 0L,
                            type = partyType,
                            name = name.ifBlank { "Party ${System.currentTimeMillis() % 1000}" },
                            phone = phone.trim(),
                            address = address.trim(),
                            openingBalance = ob,
                            currentBalance = if (partyToEdit == null) ob else partyToEdit.currentBalance
                        )
                        onSaveParty(party)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZiaGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_party_button")
                ) {
                    Text("Save Party (Khata Kholein)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PartyDetailView(
    profile: StoreProfile,
    party: PartyEntity,
    transactions: List<TransactionEntity>,
    onBack: () -> Unit,
    onMaineLiyeClick: () -> Unit,
    onMaineDiyeClick: () -> Unit,
    onStatementClick: () -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Surface(
            color = ZiaGreenPrimary,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("party_detail_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = party.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (party.phone.isNotEmpty()) {
                        Text(
                            text = party.phone,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f))
                        )
                    }
                }

                // Quick Call
                if (party.phone.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${party.phone}"))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                    }

                    // Quick WhatsApp
                    IconButton(
                        onClick = {
                            val clean = party.phone.replace("-", "").replace(" ", "")
                            val p = if (clean.startsWith("0")) "92" + clean.substring(1) else clean
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$p"))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color.White)
                    }
                }
            }
        }

        // Outstanding Balance Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (party.currentBalance > 0) ZiaUdhaarRedBg else ZiaUdhaarPurpleBg
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Outstanding Balance",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        )
                        Text(
                            text = if (party.currentBalance > 0) "Aap ne lena hai" else if (party.currentBalance < 0) "Aap ne dena hai" else "Hisaab barabar hai",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (party.currentBalance > 0) ZiaUdhaarRed else ZiaUdhaarPurple
                            )
                        )
                    }

                    Text(
                        text = "Rs. ${kotlin.math.abs(party.currentBalance).toInt()}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (party.currentBalance > 0) ZiaUdhaarRed else ZiaUdhaarPurple
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onStatementClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("statement_button")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Statement", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val msg = "Assalam-o-Alaikum ${party.name}! ${profile.storeName} ke hisaab ke mutabiq aap ki taraf Rs. ${kotlin.math.abs(party.currentBalance).toInt()} baaqi hain. Baraye meharbani ada farmayein."
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, msg)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Reminder"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Reminder", fontSize = 12.sp)
                    }
                }
            }
        }

        // Transaction Timeline List
        Text(
            text = "Transaction Timeline",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Koi transaction record nahi hai.", color = Color.Gray)
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    val formatted = dateFormat.format(Date(tx.timestamp))

                    val isPositive = tx.type == TransactionType.RECOVERY || tx.type == TransactionType.PURCHASE_RETURN
                    val color = if (isPositive) ZiaCashGreen else ZiaUdhaarRed

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${tx.type.name} #${tx.invoiceNo}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = formatted,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                                )
                                if (tx.notes.isNotEmpty()) {
                                    Text(
                                        text = tx.notes,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray, fontSize = 11.sp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isPositive) "+" else "-"} Rs. ${tx.totalAmount.toInt()}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                )
                                IconButton(
                                    onClick = { onDeleteTransaction(tx.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fixed Bottom Action Buttons: "+ Maine Liye" & "- Maine Diye"
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // + Maine Liye (Green)
                Button(
                    onClick = onMaineLiyeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ZiaCashGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("maine_liye_button")
                ) {
                    Text("+ Maine Liye", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // - Maine Diye (Red)
                Button(
                    onClick = onMaineDiyeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ZiaUdhaarRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("maine_diye_button")
                ) {
                    Text("- Maine Diye", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun PartyStatementDialog(
    profile: StoreProfile,
    party: PartyEntity,
    transactions: List<TransactionEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    fun generateStatementText(): String {
        val sb = StringBuilder()
        sb.append("================================\n")
        sb.append("      ${profile.storeName}\n")
        sb.append("      ACCOUNT STATEMENT\n")
        sb.append("================================\n")
        sb.append("Party: ${party.name}\n")
        if (party.phone.isNotEmpty()) sb.append("Phone: ${party.phone}\n")
        sb.append("Current Balance: Rs. ${party.currentBalance.toInt()}\n")
        sb.append("Status: ${if (party.currentBalance > 0) "Aap ne lena hai" else "Aap ne dena hai"}\n")
        sb.append("--------------------------------\n")
        for (tx in transactions) {
            sb.append("${tx.invoiceNo} | ${tx.type.name}: Rs. ${tx.totalAmount.toInt()}\n")
        }
        sb.append("================================\n")
        sb.append("   Powered by ZiaStore\n")
        return sb.toString()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "58mm Account Statement",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F6)),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(12.dp)) {
                        item {
                            Text(
                                text = generateStatementText(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val text = generateStatementText()
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Statement"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZiaGreenPrimary)
                    ) {
                        Text("Share Statement")
                    }
                }
            }
        }
    }
}
